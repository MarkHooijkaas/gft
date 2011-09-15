package org.kisst.gft.ssh;

import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.ssh.Ssh.ExecResult;
import org.kisst.props4j.Props;
import org.kisst.util.FileUtil;



public class SshFileServer extends SshHost implements FileServer {
	public final String basePath;

	public SshFileServer(Props props) {
		super(props);
		this.basePath=props.getString("basePath","").trim();
	}
	@Override
	public FileServerConnection openConnection() {
		return new SshFileServerConnection(this);
	}

	public String getBasePath() { return basePath; }
	
	public String convertPath(String path) { return path; }
	
	public boolean fileExists(String path) {
		path=convertPath(path);
		String file=path.substring(path.lastIndexOf('/')+1);
		ExecResult result=exec("ls -l \""+path+"\"");
		return (result.stdout.indexOf(FileUtil.filename(file))>0);
	}
	public void deleteFile(String path) { call("rm \""+path+"\""); }
	public void copyFileTo(String srcpath, SshFileServer dest, String destpath)  {
		String command="scp \""+srcpath+"\" \""+dest.user+"@"+dest.host+":"+dest.convertPath(destpath)+"\"";
		command=command.replace("\\","\\\\");
		call(command);
	}
	public void copyFileFrom(SshFileServer src, String srcpath, String destpath)  {
		call("scp \""+src.host+":"+src.convertPath(srcpath)+"\" \""+destpath+"\"");
	}
	public String ls(String dir) {
		ExecResult result=exec("ls -l \""+basePath+"/"+dir+"\"");
		return result.stdout;
	}
}