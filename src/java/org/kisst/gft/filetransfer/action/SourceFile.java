package org.kisst.gft.filetransfer.action;

import org.kisst.gft.filetransfer.FileServer;

public interface SourceFile {
	public FileServer getSourceFileServer();
	public String getSourceFilePath();
}
