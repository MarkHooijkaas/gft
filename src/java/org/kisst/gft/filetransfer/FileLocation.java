package org.kisst.gft.filetransfer;

import java.util.regex.Pattern;

import org.kisst.util.exception.BasicFunctionalException;

public class FileLocation {
	private final FileServer fileServer;
	private final String path;
	
	public FileLocation(FileLocation loc, String path) { this(loc.fileServer, join(loc.path,path)); }
	public FileLocation(FileServer fileServer, String path) {
		if (fileServer==null)
			throw new IllegalArgumentException("Empty fileserver provided");
		this.fileServer=fileServer;
		this.path=path;
	}

	@Override public String toString() { return "FileLocation("+fileServer+":"+getFullPath()+")"; }
	public String getShortString() { return fileServer.getName()+":"+path; }

	public FileServer getFileServer() { return fileServer; }
	public String getPath() { return path; }
	public String getFullPath() { return fileServer.unixPath(path); }
	public FileLocation getParentDirectory() { 
		int pos=path.lastIndexOf('/');
		if (pos>0)
			return new FileLocation(fileServer, path.substring(0,pos-1));
		throw new RuntimeException(path+" has no parentDirectory");
	} 
	
	public boolean fileExists() { return this.fileServer.fileExists(this.path); }
	public void deleteFile() { this.fileServer.deleteFile(this.path); }


	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");
	private static String join(String path, String filename) {
		if (filename==null)
			return path;
		if ( filename.length()>1024)
			throw new BasicFunctionalException("Filename length should not exceed 1024 characters"+filename);
		if (! validCharacters.matcher(filename).matches())
			throw new BasicFunctionalException("Filename should only contain alphanumeric characters / . - or _  ["+filename+"]");
		if (filename.indexOf("..")>=0)
			throw new BasicFunctionalException("Filename ["+filename+"] is not allowed to contain .. pattern");

		boolean slash=(path.endsWith("/"));
		if (filename.startsWith("/")) {
			if (slash)
				filename=filename.substring(1);
			slash=true;
		}
		if (slash)
			return path+filename;
		else
			return path+"/"+filename;
	}
	

}
