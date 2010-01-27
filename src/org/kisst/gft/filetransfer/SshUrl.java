package org.kisst.gft.filetransfer;


public class SshUrl {
	public final String url;
	public final String user;
	public final String host;
	public final String path;
	
	public SshUrl(String url) {
		if (url.startsWith("ssh:"))
			url=url.substring(4);
		this.url=url;
		int pos=url.indexOf(":");
		if (pos<0)
			throw new RuntimeException("ssh url ["+url+"] does not contain hostname");
		String fullhost=url.substring(0,pos);
		path=url.substring(pos+1);
		pos=fullhost.indexOf("@");
		if (pos<0)
			throw new RuntimeException("ssh url ["+url+"] does not contain username");
		host=fullhost.substring(pos+1);
		user=fullhost.substring(0,pos);
	}

}
