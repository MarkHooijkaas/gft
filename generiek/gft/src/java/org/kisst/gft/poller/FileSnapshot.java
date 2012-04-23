package org.kisst.gft.poller;

import org.kisst.gft.filetransfer.FileServerConnection;

public class FileSnapshot {
	private final long timestamp;
	private final long lastModified;
	private final long fileSize; 
	private final String fileSnapshot;

	
	public FileSnapshot(FileServerConnection fs, String path) {
		timestamp=new java.util.Date().getTime();

		lastModified = fs.lastModified(path); 
		fileSize = fs.fileSize(path);
		fileSnapshot=Long.toString(lastModified) + "|" + Long.toString(fileSize);
		
	}
	
	public boolean equals(FileSnapshot other) { return fileSnapshot.equals(other.fileSnapshot); }
	public long getTimestamp() { return timestamp; }
}
