package org.kisst.gft.filetransfer;

import java.io.File;

import org.kisst.cfg4j.Props;

import com.jcraft.jsch.HostKey;


public class SshHost {
	public final String user;
	public final String host;
	public final int port;
	public final HostKey hostKey=null;
	public final String known_hosts;
	private final Ssh.Credentials cred;
	
	public SshHost(Props props) {
		this.host=props.getString("host");
		this.user=props.getString("user");
		this.port=props.getInt("port",22);
		String password=props.getString("password",null);
		Object keyfile=props.get("keyfile",null);
		String f=null;
		if (keyfile instanceof File)
			f=((File) keyfile).getAbsolutePath();
		else
			f=(String) keyfile;
		this.cred=new Ssh.Credentials(user, password, f); // TODO: use port
		this.known_hosts=props.getString("known_hosts", null);
	}
	public String toString() { return "ssh:"+user+"@"+host+(port==22? "" : ":"+port); }
	
	public String call(String command) { return Ssh.ssh(this, cred, command); }
	public String convertPath(String path) { return path; }
	public boolean fileExists(String dir, String file) {
		String path=dir+"/"+file;
		try {
			String result=call("ls -l "+path);
			return (result.trim().endsWith(path));
		}
		catch (Ssh.ExitCodeException e) { return false; } // TODO: exitcode could be due to something else 
	}
	public void deleteFile(String path) { call("rm "+path); }
	public void copyFileTo(String srcpath, SshHost dest, String destdir)  {
		call("scp "+srcpath+" "+dest.host+":"+dest.convertPath(destdir));
	}
	public void copyFileFrom(SshHost src, String srcpath, String destdir)  {
		call("scp "+src.host+":"+src.convertPath(srcpath)+" "+destdir);
	}

}
