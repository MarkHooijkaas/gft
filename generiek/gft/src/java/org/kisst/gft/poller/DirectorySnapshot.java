package org.kisst.gft.poller;

import org.kisst.gft.filetransfer.FileServerConnection;

public class DirectorySnapshot implements Snapshot {
	private final long timestamp;
	private final String snapshot;
	
	public DirectorySnapshot(FileServerConnection fs, String path) {
		timestamp=new java.util.Date().getTime();
		
		// TODO: added filesizes and modificationTimes of each file
		StringBuilder result= new StringBuilder();
		for (String s: fs.getDirectoryEntries(path).keySet())
			result.append(s).append('|');
		snapshot=result.toString();
	}
	
	public boolean equals(Snapshot other) {
		if (other instanceof DirectorySnapshot)
			return snapshot.equals(((DirectorySnapshot)other).snapshot);
		return false;
	}
	public long getTimestamp() { return timestamp; }
}
