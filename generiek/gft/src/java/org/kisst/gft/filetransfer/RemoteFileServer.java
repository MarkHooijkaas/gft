package org.kisst.gft.filetransfer;



public class RemoteFileServer implements FileServer {
	private SshHost host;

	public RemoteFileServer(SshHost host) {
		this.host=host;
	}
	@Override
	public FileServerConnection openConnection() {
		// TODO Auto-generated method stub
		return new RemoteFileServerConnection(host);
	}
	
	
}