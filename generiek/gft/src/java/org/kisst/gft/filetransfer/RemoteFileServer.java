package org.kisst.gft.filetransfer;

import org.kisst.gft.ssh.SshHost;
import org.kisst.props4j.Props;



public class RemoteFileServer extends SshHost implements FileServer {

	public RemoteFileServer(Props props) {
		super(props);
	}
	@Override
	public FileServerConnection openConnection() {
		return new RemoteFileServerConnection(this);
	}
}