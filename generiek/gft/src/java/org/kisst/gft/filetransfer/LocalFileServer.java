package org.kisst.gft.filetransfer;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalFileServer implements FileServer, FileServerConnection {
	private static final Logger logger = LoggerFactory.getLogger(LocalFileServer.class);

	
	private File file(String path) { return new File(path); }
	
	public boolean fileExists(String path) { return file(path).exists(); }
	public void deleteFile(String path) { file(path).delete(); }
	public long fileSize(String path) { return file(path).length(); }
	public long lastModified(String path) { return file(path).lastModified(); }
	public boolean isDirectory(String path) { return file(path).isDirectory(); }
	public String[] getDirectoryEntries(String path) { return file(path).list();	}
	public void move(String path, String newpath) {
		File dest = new File(newpath);
		logger.info("moving {} to {}",path, dest);
		boolean result = file(path).renameTo(dest);
		if (! result)
			throw new FileCouldNotBeMovedException(path);
	}

	@Override
	public FileServerConnection openConnection() { return this;}

	@Override
	public void close() {}

	@Override
	public void getToLocalFile(String remotepath, String localpath) { throw new RuntimeException("not implemented yet"); } // TODO: implement

	@Override
	public String getBasePath() {
		throw new RuntimeException("not implemented yet");  // TODO: implement
	}

	@Override
	public boolean isAvailable() {
		throw new RuntimeException("not implemented yet");  // TODO: implement
	}
}