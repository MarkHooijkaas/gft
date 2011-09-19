package org.kisst.gft.ssh;

import java.util.LinkedHashMap;
import java.util.Vector;

import org.kisst.gft.filetransfer.FileCouldNotBeMovedException;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;


public class SshFileServerConnection implements FileServerConnection {
	private static final Logger logger = LoggerFactory.getLogger(SshFileServerConnection.class);
	private final Session session;
	private final ChannelSftp sftp;
	private final SshFileServer fileserver;
	private final SshHost host;

	public SshFileServerConnection(SshFileServer fileserver) {
		this.fileserver = fileserver;
		this.host=fileserver.getSshHost();
		session = Ssh.openSession(host);
		logger.info("Opening session on host: {}",host);
		try {
			sftp = (ChannelSftp) session.openChannel("sftp");
			sftp.connect();
		} catch (JSchException e) { throw new RuntimeException(e);}
	}

	@Override
	public void close() {
		logger.info("Closing session on host: {}",host);
		sftp.disconnect();
		session.disconnect();
	}

	public boolean fileExists(String path) { 
		path=fileserver.unixPath(path);
		try {
			System.out.println(path);
			SftpATTRS result = sftp.lstat(path);
			return result != null;
		}
		catch (SftpException e) {
			if (e.id==4 && e.getMessage().equals("SfsStatusCode.NoSuchPath")) {
				return false;
			}
			throw new RuntimeException(e); }
	}

	public void deleteFile(String path) { 
		path=fileserver.unixPath(path);
		try {
			sftp.rm(path);
		} catch (SftpException e) { throw new RuntimeException(e); }
	}
	public long fileSize(String path) { return getFileAttributes(path).size; }
	public long lastModified(String path) { return getFileAttributes(path).modifyTime; }
	public boolean isDirectory(String path) { return getFileAttributes(path).isDirectory; }
	
	public FileAttributes getFileAttributes(String path) {
		path=fileserver.unixPath(path);
		try {
			SftpATTRS attr = sftp.lstat(path);
			return new FileAttributes(attr.getATime(), attr.getMTime(), attr.isDir(), attr.getSize());
		} catch (SftpException e) { throw new RuntimeException(e); }
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, FileAttributes> getDirectoryEntries(String path) {
		try {
			path=fileserver.unixPath(path);
			logger.info("getting remote diretory: {}",path);
			Vector<LsEntry> vv = sftp.ls(path);
			logger.info("found {} entries",vv.size());
			LinkedHashMap<String,FileAttributes> result = new LinkedHashMap<String,FileAttributes>();
			for (LsEntry entry: vv) {
				logger.debug("found entry {} - {}",entry.getFilename(), entry.getLongname());
				SftpATTRS attr = entry.getAttrs();
                result.put(entry.getFilename(),
                	new FileAttributes(attr.getATime(), attr.getMTime(), attr.isDir(), attr.getSize()));
			}
			return result;
		} 
		catch (SftpException e) { throw new RuntimeException(e); }
	}

	public void move(String path, String newpath) {
		path=fileserver.unixPath(path);
		newpath=fileserver.unixPath(newpath);
		try {
			sftp.rename(path, newpath);
		}
		catch (SftpException e) { throw new FileCouldNotBeMovedException(path, e); }
	}

	public void getToLocalFile(String remotepath, String localpath) {
		remotepath=fileserver.unixPath(remotepath);
		try {
			logger.info("copy file from remote {} to local {}",remotepath,localpath);
			sftp.get(remotepath, localpath);
		} 
		catch (SftpException e) { throw new RuntimeException(e); }
	}

	@Override
	public void putFromLocalFile(String localpath, String remotepath) {
		remotepath=fileserver.unixPath(remotepath);
		try {
			logger.info("copy file from local {} to remote {}",localpath,remotepath);
			sftp.put(localpath, remotepath);
		} 
		catch (SftpException e) { throw new RuntimeException(e); }
	}

}