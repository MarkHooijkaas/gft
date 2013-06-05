package org.kisst.gft.poller;

import org.kisst.gft.filetransfer.FileServerConnection;

public class DirectorySnapshot extends Snapshot {

	
	public DirectorySnapshot(FileServerConnection fs, String path) {
		super(getDirectorySnapshot(fs,path));
	}
	
	private static String getDirectorySnapshot(FileServerConnection fs, String path) {
		// TODO: added filesizes and modificationTimes of each file
		StringBuilder result= new StringBuilder();
		for (String s: fs.getDirectoryEntries(path).keySet())
			result.append(s).append('|');
		return result.toString();
	}
	


}
