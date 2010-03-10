package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;

public class As400SshHost extends SshHost {
	private final String scpCommand;

	public As400SshHost(Props props) {
		super(props);
		this.scpCommand=props.getString("scp_command","scp");
	}

	@Override public String convertPath(String path) { return path.replace('/','\\'); }
	@Override public boolean fileExists(String dir, String file) {
		String path=convertPath(dir+"\\"+file);
		String result=call("dir "+path);
		return (result.indexOf(file)>0);
	}

	@Override public void deleteFile(String path) { call("del "+convertPath(path)); }
	
	@Override public void copyFileTo(String srcpath, SshHost dest, String destdir)  {
		String command=scpCommand+" "+srcpath+" "+dest.user+"@"+dest.host+":"+dest.convertPath(destdir);
		command=command.replace("\\","\\\\");
		call(command);
	}
	@Override public void copyFileFrom(SshHost src, String srcpath, String filename, String destdir)  {
		String command=scpCommand+" "+src.user+"@"+src.host+":"+src.convertPath(srcpath+"/"+filename)+" "+destdir+"/"+filename;
		command=command.replace("\\","\\\\");
		call(command);
	}
}
