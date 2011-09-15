package org.kisst.gft.ssh;

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
	private final SshHost host;

	public SshFileServerConnection(SshHost host) {
		this.host=host;
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
		try {
			SftpATTRS result = sftp.lstat(path);
			return result != null;
		} catch (SftpException e) { throw new RuntimeException(e); }
	}

	public void deleteFile(String path) { 
		try {
			sftp.rm(path);
		} catch (SftpException e) { throw new RuntimeException(e); }
	}
	public long fileSize(String path) { return getFileAttributes(path).getSize(); }
	public long lastModified(String path) { return getFileAttributes(path).getMTime(); }
	public boolean isDirectory(String path) { return getFileAttributes(path).isDir(); }
	
	public SftpATTRS getFileAttributes(String path) { 
		try {
			return sftp.lstat(path);
		} catch (SftpException e) { throw new RuntimeException(e); }
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public String[] getDirectoryEntries(String path) {
		try {
			logger.info("getting remote diretory: {}",path);
			Vector<LsEntry> vv = sftp.ls(path);
			logger.info("found {} entries",vv.size());
			String[] result = new String[vv.size()];
			int i=0;
			for (LsEntry entry: vv) {
				logger.debug("found entry {} - {}",entry.getFilename(), entry.getLongname());
                result[i++] = entry.getFilename();
			}
			return result;
		} 
		catch (SftpException e) { throw new RuntimeException(e); }
	}

	public void move(String path, String newpath) {
		try {
			sftp.rename(path, newpath);
		}
		catch (SftpException e) { throw new FileCouldNotBeMovedException(path, e); }
	}

	public void getToLocalFile(String remotepath, String localpath) {
			try {
				logger.info("copy file from remote {} to local {}",remotepath,localpath);
				sftp.get(remotepath, localpath);

			} 
			catch (SftpException e) { throw new RuntimeException(e); }
	}
	
	@Override
	public void putFromLocalFile(String localpath, String remotepath) {
		try {
			logger.info("copy file from local {} to remote {}",localpath,remotepath);
			sftp.put(localpath, remotepath);

		} 
		catch (SftpException e) { throw new RuntimeException(e); }
	}

}