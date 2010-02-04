package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.task.Task;

public class ActionList  implements Action {
	private final Action[] actions;
	
	public ActionList(GftContainer gft, Props props) {
		String actions=props.getString("actions");
		String[] parts=actions.split(",");
		this.actions=new Action[parts.length];
		int i=0;
		for (String s: parts) {
			Action a=gft.getAction(s.trim());
			this.actions[i++]=a;
			if (a==null)
				throw new RuntimeException("Unknown action "+s);
		}
	}

	public Object execute(Task task) {
		for (Action a: actions)
			a.execute(task);
		return null;
	}
}
