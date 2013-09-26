package org.kisst.gft.filetransfer;

import java.util.LinkedHashMap;


public interface FileServerConnection {
	public static class FileAttributes {
		public final long accessTime;
		public final long  modifyTime;
		public final boolean isDirectory;
		public final long size;
		public FileAttributes(long accessTime, long modifyTime, boolean isDirectory, long size) {
			this.accessTime=accessTime;
			this.modifyTime=modifyTime;
			this.isDirectory=isDirectory;
			this.size=size;
		}
	}
	public void close();
	public boolean fileExists(String path);
	public void deleteFile(String path);
	public LinkedHashMap<String, FileAttributes> getDirectoryEntries(String path);
	public FileAttributes getFileAttributes(String path);
	public long fileSize(String path);
	public long lastModified(String path);
	public boolean isDirectory(String path);
	public void move(String path, String newpath);
	public void getToLocalFile(String remotepath, String localpath); 
	public void putFromLocalFile(String localpath, String remotepath);
	public String getFileContentAsString(String remotepath);
	public void putStringAsFileContent(String remotepath, String content);
}
