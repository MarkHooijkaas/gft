package org.kisst.gft.filetransfer;

import java.io.File;

import org.kisst.cfg4j.Props;
import org.kisst.gft.admin.rest.Representable;
import org.kisst.gft.filetransfer.Ssh.ExecResult;
import org.kisst.util.FileUtil;

import com.jcraft.jsch.HostKey;


public class SshHost implements Representable {
	public final String user;
	public final String host;
	public final int port;
	public final HostKey hostKey=null;
	public final String known_hosts;
	private final Ssh.Credentials cred;
	private final String keyfile;
	
	public SshHost(Props props) {
		this.host=props.getString("host");
		this.user=props.getString("user");
		this.port=props.getInt("port",22);
		String password=props.getString("password",null);
		Object tmpkeyfile=props.get("keyfile",null);
		if (tmpkeyfile instanceof File)
			keyfile=((File) tmpkeyfile).getAbsolutePath();
		else
			keyfile=(String) tmpkeyfile;
		this.cred=new Ssh.Credentials(user, password, keyfile); // TODO: use port
		Object tmp=props.getString("known_hosts", null);
		if (tmp instanceof File)
			this.known_hosts=((File) tmp).getAbsolutePath();
		else
			this.known_hosts=(String) tmp;
	}
	public String toString() { return "ssh:"+user+"@"+host+(port==22? "" : ":"+port); }
	

	public ExecResult exec(String command) { return Ssh.exec(this, cred, command); }
	public String call(String command) { return Ssh.ssh(this, cred, command); }
	public String convertPath(String path) { return path; }
	public boolean fileExists(String dir, String file) {
		String path=dir+"/"+file;
		try {
			String result=call("ls -l "+path);
			return (result.trim().endsWith(FileUtil.filename(file)));
		}
		catch (Ssh.ExitCodeException e) { return false; } // TODO: exitcode could be due to something else 
	}
	public void deleteFile(String path) { call("rm "+path); }
	public void copyFileTo(String srcpath, SshHost dest, String destpath)  {
		String command="scp "+srcpath+" "+dest.user+"@"+dest.host+":"+dest.convertPath(destpath);
		command=command.replace("\\","\\\\");
		call(command);
	}
	public void copyFileFrom(SshHost src, String srcpath, String destpath)  {
		call("scp "+src.host+":"+src.convertPath(srcpath)+" "+destpath);
	}
	public String getRepresentation() {
		StringBuilder result=new StringBuilder();
		result.append("SshHost {\n");
		result.append("\tuser="+user+"\n");
		result.append("\thost="+host+"\n");
		result.append("\tport="+port+"\n");
		result.append("\tkeyfile="+keyfile+"\n");
		result.append("}\n");
		return result.toString();
	}

}
