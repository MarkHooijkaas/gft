package org.kisst.gft.ssh;

import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.props4j.Props;
import org.kisst.util.FileUtil;



public class SshFileServer implements FileServer {
	protected final SshHost host;
	protected final String basePath;
	protected final String scpCommand;
	
	public SshFileServer(Props props) {
		this.host=new SshHost(props);
		this.basePath=props.getString("basePath","").trim();
		this.scpCommand=props.getString("scpCommand","scp").trim();
	}
	@Override
	public FileServerConnection openConnection() {
		return new SshFileServerConnection(this);
	}
	public boolean isAvailable() { return host.isAvailable();}
	public SshHost getSshHost() { return host; }
	public String getName() { return host.name; }

	public String toString() { return host.toString(); }
	//public String getBasePath() { return basePath; }
	
	public String nativePath(String path) { return FileUtil.joinPaths(basePath, path); }
	public String unixPath(String path) { return FileUtil.joinPaths(basePath, path); }
	public String escape(String str) { return str.replace("\\","\\\\"); }

	
	public boolean fileExists(String path) {
		FileServerConnection conn=openConnection();
		try {
			return conn.fileExists(path);
		}
		finally {
			conn.close();
		}
	}
	public void deleteFile(String path) { 
		FileServerConnection conn=openConnection();
		try {
			conn.deleteFile(path);
		}
		finally {
			conn.close();
		}
	}

	public void copyFile(String srcpath, String destpath) { 
		host.call(
			"cp", 
			nativePath(srcpath), 
			nativePath(destpath) 
		);
	}

	public void copyFileTo(String srcpath, SshFileServer dest, String destpath)  {
		if (dest==this)
			copyFile(srcpath,destpath);
		else
			host.call(
					scpCommand, 
					nativePath(srcpath), 
					dest.host.user+"@"+dest.host.host+":"+escape(dest.nativePath(destpath))
			);
	}
	public void copyFileFrom(SshFileServer src, String srcpath, String destpath)  {
		if (src==this)
			copyFile(srcpath,destpath);
		else 
			host.call(
					scpCommand, 
					src.host.user+"@"+src.host.host+":"+escape(src.nativePath(srcpath)), 
					destpath
			);
	}
}