package org.kisst.gft.filetransfer;


public interface FileServerConnection {
	public void close();
	public boolean fileExists(String path);
	public void deleteFile(String path);
	public String[] getDirectoryEntries(String path);
	public long fileSize(String path);
	public long lastModified(String path);
	public boolean isDirectory(String path);
	public void move(String path, String newpath);
	public void getToLocalFile(String remotepath, String localpath); 
	public void putFromLocalFile(String localpath, String remotepath);
}
