package org.kisst.gft.filetransfer.action;

import org.kisst.gft.filetransfer.FileServer;

public interface DestinationFile {
	public FileServer getDestinationFileServer();
	public String getDestinationFilePath();
}
