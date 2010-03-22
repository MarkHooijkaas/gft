package org.kisst.gft.action;

import org.kisst.cfg4j.LayeredProps;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.task.Task;

public class ActionList  implements Action {
	private final Action[] actions;
	
	public ActionList(Channel chan, Props props) {

		String actions=props.getString("actions");
		String[] parts=actions.split(",");
		this.actions=new Action[parts.length];
		int i=0;
		for (String name: parts) {
			name=name.trim();
			LayeredProps lprops=new LayeredProps();
			SimpleProps top=new SimpleProps();
			top.put("action",chan.gft.actions.get(name));
			top.put("channel",props);
			lprops.addLayer(top);
			if (props.get(name,null) instanceof Props)
				lprops.addLayer(props.getProps(name));
			lprops.addLayer(chan.gft.actions.get(name));
			lprops.addLayer(props);
				
			Action a=chan.createAction(lprops);
			if (a==null)
				throw new RuntimeException("Unknown action "+name);
			this.actions[i++]=a;
		}
	}

	public Object execute(Task task) {
		for (Action a: actions) {
			try {
				a.execute(task);
			}
			catch (RuntimeException e) {
				throw new RuntimeException("Error while executing "+a.toString()+": "+e.getMessage());
			}
		}
		return null;
	}
}
