package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;
import org.kisst.gft.filetransfer.Ssh.ExecResult;
import org.kisst.util.FileUtil;

public class As400SshHost extends SshHost {
	private final String scpCommand;

	public As400SshHost(Props props) {
		super(props);
		this.scpCommand=props.getString("scp_command","scp");
	}

	@Override public boolean fileExists(String dir, String file) {
		String path=convertPath(dir+"/"+file);
		ExecResult result=exec("ls -l "+path);
		return (result.stdout.indexOf(FileUtil.filename(file))>0);
	}

	@Override public void deleteFile(String path) { call("rm "+convertPath(path)); }
	
	@Override public void copyFileTo(String srcpath, SshHost dest, String destdir)  {
		String command=scpCommand+" "+srcpath+" "+dest.user+"@"+dest.host+":"+dest.convertPath(destdir);
		if (dest==this) // special case,: a local copy
			command="cp "+convertPath(srcpath)+" "+dest.convertPath(destdir);
		command=command.replace("\\","\\\\");
		call(command);
	}
	@Override public void copyFileFrom(SshHost src, String srcpath, String destpath)  {
		String command=scpCommand+" "+src.user+"@"+src.host+":"+src.convertPath(srcpath)+" "+destpath;
		command=command.replace("\\","\\\\");
		call(command);
	}
}
