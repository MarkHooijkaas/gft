package org.kisst.gft.filetransfer;

import java.io.File;
import java.util.LinkedHashMap;

import org.kisst.props4j.Props;
import org.kisst.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalFileServer implements FileServer, FileServerConnection {
	private static final Logger logger = LoggerFactory.getLogger(LocalFileServer.class);
	private final  String basePath;

	public LocalFileServer(Props props) {
		this.basePath=props.getString("basePath","").trim();
	}
	
	private File file(String path) { return new File(basePath+path); }
	
	@Override public boolean fileExists(String path) { return file(path).exists(); }
	@Override public void deleteFile(String path) { file(path).delete(); }
	@Override public long fileSize(String path) { return file(path).length(); }
	@Override public long lastModified(String path) { return file(path).lastModified(); }
	@Override public boolean isDirectory(String path) { return file(path).isDirectory(); }

	public LinkedHashMap<String, FileAttributes> getDirectoryEntries(String path) {
		LinkedHashMap<String, FileAttributes>  result= new LinkedHashMap<String,FileAttributes>();
		File dir = file(path);
		for (String filename : dir.list()) {
			File f=new File(dir,filename);
			FileAttributes attr=new FileAttributes(0,f.lastModified(),f.isDirectory(),f.length());
			result.put(filename, attr);
		}
		return result;
	}
	@Override public void move(String path, String newpath) {
		File dest = new File(newpath);
		logger.info("moving {} to {}",path, dest);
		boolean result = file(path).renameTo(dest);
		if (! result)
			throw new FileCouldNotBeMovedException(path);
	}

	@Override public FileServerConnection openConnection() { return this;}
	@Override public void close() {}
	@Override public boolean isAvailable() { return true; }

	@Override public FileAttributes getFileAttributes(String path) {
		File f=new File(path);
		return new FileAttributes(0,f.lastModified(),f.isDirectory(),f.length());
	}



	@Override
	public void getToLocalFile(String remotepath, String localpath) { 
		throw new RuntimeException("not implemented yet");  // TODO: implement
	}

	public String getFileContentAsString(String remotepath) {
		return FileUtil.loadString(remotepath);
	}
	@Override
	public void putStringAsFileContent(String remotepath, String content) {
		FileUtil.saveString(new File(remotepath), content);
	}

	
	@Override
	public void putFromLocalFile(String localpath, String remotepath) {
		throw new RuntimeException("not implemented yet"); //TODO implement
	}

	@Override
	public String getName() { return "localhost"; }
}