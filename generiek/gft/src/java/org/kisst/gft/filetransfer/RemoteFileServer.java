package org.kisst.gft.filetransfer;

import java.io.File;

import org.kisst.gft.filetransfer.Ssh.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;


public class RemoteFileServer implements FileServer {
	private static final Logger logger = LoggerFactory.getLogger(RemoteFileServer.class);

	private SshHost host;
	private Credentials cred;
	@Override
	public FileServerConnection openConnection() {
		// TODO Auto-generated method stub
		return new RemoteFileServerConnection(host,cred);
	}
	
	
}