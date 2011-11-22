package org.kisst.gft.filetransfer;

public class FileCouldNotBeMovedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public FileCouldNotBeMovedException(String filename) {
		super("Could not move file "+filename);
	}

	public FileCouldNotBeMovedException(String filename, Exception e) {
		super("Could not move file "+filename,e);
	}

}
