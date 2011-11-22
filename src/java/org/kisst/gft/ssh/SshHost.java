package org.kisst.gft.ssh;

import java.io.File;

import org.kisst.gft.admin.rest.Representable;
import org.kisst.gft.ssh.Ssh.ExecResult;
import org.kisst.props4j.Props;
import org.kisst.util.TimeWindowList;

import com.jcraft.jsch.HostKey;


public class SshHost implements Representable {
	public final String name;
	public final String user;
	public final String host;
	public final int port;
	public final HostKey hostKey=null;
	public final String known_hosts;
	public final Ssh.Credentials cred;
	private final String keyfile;
	private final TimeWindowList forbiddenTimes;

	
	public SshHost(Props props) {
		this.name=props.getLocalName();
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
		String timewindow=props.getString("forbiddenTimes", null);
		if (timewindow==null)
			this.forbiddenTimes=null;
		else
			this.forbiddenTimes=new TimeWindowList(timewindow);
	}
	public String getUser() { return user; }

	public String toString() { return "ssh:"+user+"@"+host+(port==22? "" : ":"+port); }
	
	public boolean isAvailable() { return forbiddenTimes==null || ! forbiddenTimes.isTimeInWindow(); }
	public String createCommand(String cmd, String... args) {
		String result=cmd;
		for (String arg: args) {
			if (arg.contains(" "))
				result+=" \""+arg+"\"";
			else
				result+=" "+arg;
		}
		return result;
	}	
	public ExecResult exec(String command, String... args) { return Ssh.exec(this, cred, createCommand(command,args)); }
	public String call(String command, String... args) { return Ssh.ssh(this, cred, createCommand(command,args)); }
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
