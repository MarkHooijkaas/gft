package org.kisst.gft.filetransfer;


/*
 * Dummy class to be shown in admin screen as destination
 */

public class DummyFileServer implements FileServer {
	private final String name;
	public DummyFileServer(String name) { this.name=name; }
	@Override public FileServerConnection openConnection() { throw new RuntimeException("not implemented"); }
	@Override public boolean isAvailable() { return true; }
	@Override public boolean fileExists(String destpath) { return false; }
	@Override public void deleteFile(String srcpath) { throw new RuntimeException("not implemented"); }
	@Override public String getName() { return name; }
	@Override public String unixPath(String path) { return path; }

}
