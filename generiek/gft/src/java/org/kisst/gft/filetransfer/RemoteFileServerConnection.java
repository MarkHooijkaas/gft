package org.kisst.gft.filetransfer;

import java.io.File;
import java.util.Vector;

import org.kisst.gft.filetransfer.Ssh.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;


public class RemoteFileServerConnection implements FileServerConnection {
	private static final Logger logger = LoggerFactory.getLogger(RemoteFileServerConnection.class);
	private final Session session;
	private final ChannelSftp sftp;

	public RemoteFileServerConnection(SshHost host, Credentials cred) {
		session = Ssh.openSession(host, cred);
		try {
			sftp = (ChannelSftp) session.openChannel("sftp");
		} catch (JSchException e) { throw new RuntimeException(e);}
	}

	@Override
	public void close() {
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
	
	@SuppressWarnings("unchecked")
	public String[] getDirectoryEntries(String path) {
		try {
			Vector<LsEntry> vv = sftp.ls(path);
			String[] result = new String[vv.size()];
			int i=0;
			for (LsEntry entry: vv) {
                result[i++] = entry.getFilename();
			}
			return result;
		} 
		catch (SftpException e) { throw new RuntimeException(e); }
	}

	public boolean move(String path, String newpath) {
		try {
			sftp.rename(path, newpath);
			return true; // TODO: ????
		}
		catch (SftpException e) { throw new RuntimeException(e); }
	}


}