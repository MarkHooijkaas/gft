package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;
import org.kisst.gft.filetransfer.Ssh.ExecResult;

public class WindowsSshHost extends SshHost {
	private final String scpCommand;

	public WindowsSshHost(Props props) {
		super(props);
		this.scpCommand=props.getString("scpcommand","PSCP.exe");
	}

	@Override public String convertPath(String path) { return path.replace('/','\\'); }
	@Override public boolean fileExists(String dir, String file) {
		String command="dir /b "+convertPath(dir+"\\"+file);
		//command=command.replace("\\","\\\\");
		ExecResult result=exec(command);
		return (result.stdout.indexOf(file)>=0);
	}

	@Override public void deleteFile(String path) { call("del "+convertPath(path)); }
	
	@Override public void copyFileTo(String srcpath, SshHost dest, String destdir)  {
		String command=scpCommand+" "+convertPath(srcpath)+" "+dest.host+":"+dest.convertPath(destdir);
		//command=command.replace("\\","\\\\");
		call(command);
	}
	@Override public void copyFileFrom(SshHost src, String srcpath, String filename, String destdir)  {
		call(scpCommand+" "+src.host+":"+src.convertPath(srcpath)+" "+convertPath(destdir));
	}
}
