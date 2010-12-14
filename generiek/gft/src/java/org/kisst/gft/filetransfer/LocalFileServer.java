package org.kisst.gft.filetransfer;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalFileServer implements FileServer {
	private static final Logger logger = LoggerFactory.getLogger(LocalFileServer.class);

	
	private File file(String path) { return new File(path); }
	
	public boolean fileExists(String path) { return file(path).exists(); }
	public void deleteFile(String path) { file(path).delete(); }
	public long fileSize(String path) { return file(path).length(); }
	public long lastModified(String path) { return file(path).lastModified(); }
	public boolean isDirectory(String path) { return file(path).isDirectory(); }
	public String[] ls(String path) { return file(path).list();	}
	public boolean move(String path, String newpath) {
		File dest = new File(newpath);
		logger.info("moving {} to {}",path, dest);
		return file(path).renameTo(dest); 
	}

}