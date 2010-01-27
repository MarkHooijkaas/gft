package org.kisst.gft.filetransfer;

import java.util.HashMap;

import org.kisst.cfg4j.Props;

public class Channel {
	private final static HashMap<String, Channel> channels= new HashMap<String, Channel>();

	private static Props props;
	public static void init(Props props) {
		Channel.props=props;
		channels.clear();
	}
	public static Channel getChannel(String name) {
		Channel ch=channels.get(name);
		if (ch==null) { 
			ch=new Channel(name,props);
			channels.put(name, ch);
		}
		return ch;		
	}

	public final SshUrl from;
	public final SshUrl to;
	public final Ssh.Credentials cred;
	public final boolean localToRemote=true;
	
	public Channel(String name, Props props) {
		this.from=new SshUrl(props.getString("gft.channel."+name+".from"));
		this.to=new SshUrl(props.getString("gft.channel."+name+".to"));
		this.cred=new Ssh.Credentials(getUser(), props.getString("gft.channel."+name+".keyfile"));
	}
	
	public String getUser() {
		if (localToRemote)
			return from.user;
		else
			return to.user;
	}

	public String getHost() {
		if (localToRemote)
			return from.host;
		else
			return to.host;
	}

	public String getFromUrl() {
		if (localToRemote)
			return from.path;
		else
			return from.url;
	}
	public String getToUrl() {
		if (localToRemote)
			return to.url;
		else
			return to.path;
	}
}
