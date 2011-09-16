package org.kisst.gft.ssh;

import org.kisst.gft.ssh.Ssh.ExecResult;
import org.kisst.props4j.Props;

public class WindowsSshHost extends SshFileServer {
	public WindowsSshHost(Props props) {
		super(props);
	}
	@Override public String nativePath(String path) { return path.replace('/','\\'); }
	@Override public String unixPath(String path) {
		if (path.charAt(1)==':')
			return '/'+path.charAt(0)+'/'+path.substring(2);
		else
			return path;
	}
	@Override public String escape(String str) { return str; }

	
	public void copyFile(String srcpath, String destpath) { 
		String command=host.createCommand(
			"copy", 
			nativePath(srcpath), 
			nativePath(destpath) 
		);
		ExecResult result = host.exec(command);
				// This is a hack, because the exitcode does not seem to be reliable. 
		if (result.exitcode==0)
			return;
		if (result.stderr!=null && result.stderr.trim().length()>0)
			throw new RuntimeException("Ssh call of command ["+command+"] returned exit code "+result.exitcode+" and stderr ["+result.stderr+"] and stdout "+result.stdout);
		if (result.stdout.indexOf("1 file(s) copied")>0)
			return;
		throw new RuntimeException("Ssh call of command ["+command+"] returned exit code "+result.exitcode+" and stderr ["+result.stderr+"] and stdout "+result.stdout);
	}
	

	@Override public String ls(String dir) {
		ExecResult result=host.exec("dir \""+nativePath(basePath+"/"+dir)+"\"");
		return result.stdout;
	}

}
