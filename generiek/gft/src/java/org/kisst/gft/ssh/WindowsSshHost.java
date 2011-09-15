package org.kisst.gft.ssh;

import org.kisst.gft.ssh.Ssh.ExecResult;
import org.kisst.props4j.Props;
import org.kisst.util.FileUtil;

public class WindowsSshHost extends SshFileServer {
	private final String scpCommand;

	public WindowsSshHost(Props props) {
		super(props);
		this.scpCommand=props.getString("scpcommand","PSCP.exe");
	}

	@Override public String convertPath(String path) { return path.replace('/','\\'); }
	@Override public boolean fileExists(String path) {
		path=convertPath(path);
		String file=path.substring(path.lastIndexOf('\\')+1);
		String command="dir /b \""+path+"\"";
		ExecResult result=exec(command);
		return (result.stdout.indexOf(FileUtil.filename(file))>=0);
	}

	@Override public void deleteFile(String path) { 
		String command="del \""+convertPath(path)+"\"";
		ExecResult result = exec(command);
		// This is a hack, because the exitcode does not seem to be reliable. 
		if (result.exitcode==0)
			return;
		if (result.stderr!=null && result.stderr.trim().length()>0)
			throw new RuntimeException("Ssh call of command ["+command+"] returned exit code "+result.exitcode+" and stderr ["+result.stderr+"] and stdout "+result.stdout);
		if (result.stdout==null || result.stdout.trim().length()==0) // Successful delete command will give no output
			return;
		throw new RuntimeException("Ssh call of command ["+command+"] returned exit code "+result.exitcode+" and stderr ["+result.stderr+"] and stdout "+result.stdout);
	}
	
	@Override public void copyFileTo(String srcpath, SshFileServer dest, String destdir)  {
		String command=scpCommand+" \""+convertPath(srcpath)+"\" \""+dest.host+":"+dest.convertPath(destdir)+"\"";
		if (dest==this) // special case,: a local copy
			command="copy "+convertPath(srcpath)+" "+dest.convertPath(destdir);
		//command=command.replace("\\","\\\\");
		ExecResult result = exec(command);
		// This is a hack, because the exitcode does not seem to be reliable. 
		if (result.exitcode==0)
			return;
		if (result.stderr!=null && result.stderr.trim().length()>0)
			throw new RuntimeException("Ssh call of command ["+command+"] returned exit code "+result.exitcode+" and stderr ["+result.stderr+"] and stdout "+result.stdout);
		if (result.stdout.indexOf("1 file(s) copied")>0)
			return;
		throw new RuntimeException("Ssh call of command ["+command+"] returned exit code "+result.exitcode+" and stderr ["+result.stderr+"] and stoput "+result.stdout);
	}
	@Override public void copyFileFrom(SshFileServer src, String srcpath, String destpath)  {
		call(scpCommand+" \""+src.host+":"+src.convertPath(srcpath)+"\" \""+convertPath(destpath)+"\"");
	}
	@Override public String ls(String dir) {
		ExecResult result=exec("dir \""+convertPath(basePath+"/"+dir)+"\"");
		return result.stdout;
	}

}
