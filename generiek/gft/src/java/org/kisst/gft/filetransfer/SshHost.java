package org.kisst.gft.filetransfer;

import java.io.File;

import org.kisst.cfg4j.Props;
import org.kisst.gft.FunctionalException;
import org.kisst.gft.admin.rest.Representable;
import org.kisst.gft.filetransfer.Ssh.ExecResult;
import org.kisst.util.FileUtil;
import org.kisst.util.TimeWindowList;

import com.jcraft.jsch.HostKey;


public class SshHost implements Representable {
	public final String user;
	public final String host;
	public final int port;
	public final HostKey hostKey=null;
	public final String known_hosts;
	private final Ssh.Credentials cred;
	private final String keyfile;
	private final TimeWindowList forbiddenTimes;
	public final String basePath;

	
	public SshHost(Props props) {
		this.host=props.getString("host");
		this.user=props.getString("user");
		this.port=props.getInt("port",22);
		this.basePath=props.getString("basePath","").trim();
		if (this.basePath.length()>0 && ! this.basePath.endsWith("/"))
			throw new FunctionalException("basePath "+basePath+" should end with a / when it is defined");
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
		String timewindow=props.getString("forbiddenTimes", null);
		if (timewindow==null)
			this.forbiddenTimes=null;
		else
			this.forbiddenTimes=new TimeWindowList(timewindow);
	}
	public String toString() { return "ssh:"+user+"@"+host+(port==22? "" : ":"+port); }
	
	public boolean isAvailable() { return forbiddenTimes==null || ! forbiddenTimes.isTimeInWindow(); }
	
	public ExecResult exec(String command) { return Ssh.exec(this, cred, command); }
	public String call(String command) { return Ssh.ssh(this, cred, command); }
	public String convertPath(String path) { return path; }
	public boolean fileExists(String path) {
		path=convertPath(path);
		String file=path.substring(path.lastIndexOf('/')+1);
		ExecResult result=exec("ls -l "+path);
		return (result.stdout.indexOf(FileUtil.filename(file))>0);
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
