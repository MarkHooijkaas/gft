package org.kisst.gft.filetransfer;


public interface FileServerConnection {
	public void close();
	public boolean fileExists(String path);
	public void deleteFile(String path);
	//public void copyFileTo(String srcpath, SshHost dest, String destpath);
	//public void copyFileFrom(SshHost src, String srcpath, String destpath);
	public String[] getDirectoryEntries(String path);
	public long fileSize(String path);
	public long lastModified(String path);
	public boolean isDirectory(String path);
	public boolean move(String path, String newpath);
}
