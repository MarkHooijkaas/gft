package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.task.Task;

public class Channel implements Action {
	public final ScpUrl from;
	public final ScpUrl to;
	public final Ssh.Credentials cred;
	public final Action action;
	public final boolean localToRemote=true;
	
	public Channel(GftContainer gft, Props props) {
		this.from=new ScpUrl(props.getString("from"));
		this.to=new ScpUrl(props.getString("to"));
		this.cred=new Ssh.Credentials(getUser(), props.getString("keyfile"));
		this.action=new ActionList(gft, props);
	}
	public String toString() { return "Channel(scp "+from+" "+to+")";}
	public Object execute(Task task) { action.execute(task); return null; }
	
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
