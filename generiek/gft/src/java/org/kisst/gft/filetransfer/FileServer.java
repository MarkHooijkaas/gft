package org.kisst.gft.filetransfer;



public interface FileServer {
	public FileServerConnection openConnection();
	public boolean isAvailable();
	public String getBasePath();
	public boolean fileExists(String destpath);
	public void deleteFile(String srcpath);
	
}
