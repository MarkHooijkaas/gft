package org.kisst.gft.filetransfer;

import org.kisst.gft.ssh.SshHost;



public class RemoteFileServer implements FileServer {
	private SshHost host;

	public RemoteFileServer(SshHost host) {
		this.host=host;
	}
	@Override
	public FileServerConnection openConnection() {
		return new RemoteFileServerConnection(host);
	}
	
	
}