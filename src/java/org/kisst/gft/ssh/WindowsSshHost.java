package org.kisst.gft.ssh;

import org.kisst.gft.ssh.Ssh.ExecResult;
import org.kisst.props4j.Props;
import org.kisst.util.FileUtil;

public class WindowsSshHost extends SshFileServer {
	public WindowsSshHost(Props props) {
		super(props);
	}
	@Override public String nativePath(String path) { return FileUtil.joinPaths(basePath, path).replace('/','\\'); }
	@Override public String unixPath(String path) {
		path=basePath+path;
		if (path.length()>1 && path.charAt(1)==':') // check for drive letter, e.g. E:
			// this should replace E:/temp by /E/temp
			return "/"+path.charAt(0)+path.substring(2);
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
}
