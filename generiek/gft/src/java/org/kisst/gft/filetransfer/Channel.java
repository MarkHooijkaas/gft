package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskDefinition;

public class Channel implements TaskDefinition {
	public final String name;
	public final Action action;
	public final boolean localToRemote=true;
	public final Props props;
	
	public Channel(GftContainer gft, Props props) {
		this.action=new ActionList(gft, props);
		this.props=props;
		this.name=props.getLocalName();
	}
	public String toString() { return "Channel("+name+")";}
	public Object execute(Task task) { action.execute(task); return null; }
	
	public void run(Task task) {
		action.execute(task);
		task.setStatus(Task.DONE);
	}
}
