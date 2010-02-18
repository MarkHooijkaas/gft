package org.kisst.gft.filetransfer;

import java.io.File;

import org.kisst.cfg4j.Props;


public class SshHost {
	public final String user;
	public final String host;
	public final int port;
	private final Ssh.Credentials cred;
	
	public SshHost(Props props) {
		this.host=props.getString("host");
		this.user=props.getString("user");
		this.port=props.getInt("port",22);
		Object keyfile=props.get("keyfile");
		String f=null;
		if (keyfile instanceof File)
			f=((File) keyfile).getAbsolutePath();
		else
			f=(String) keyfile;
		this.cred=new Ssh.Credentials(host, f); // TODO: use port
	}
	
	public String call(String command) {
		return Ssh.ssh(cred, host, command);
	}
	
	public String toString() { return "ssh:"+user+"@"+host+(port==22? "" : ":"+port); }

}
