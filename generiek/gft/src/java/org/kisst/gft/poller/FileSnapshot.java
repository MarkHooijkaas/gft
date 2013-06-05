package org.kisst.gft.poller;

import org.kisst.gft.filetransfer.FileServerConnection;

public class FileSnapshot extends Snapshot {
	public FileSnapshot(FileServerConnection fs, String path) {
		super(Long.toString(fs.lastModified(path)) + "|" + Long.toString(fs.fileSize(path)));
	}
}
