package org.kisst.gft.action;

import java.util.LinkedHashMap;

import org.kisst.gft.RetryableException;
import org.kisst.gft.filetransfer.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionList  implements Action {
	final static Logger logger=LoggerFactory.getLogger(ActionList.class); 
	
	private final LinkedHashMap<String,Action> actions=new LinkedHashMap<String,Action>();

	private final int maxNrofTries;
	private final long retryDelay;

	public ActionList(BasicTaskDefinition taskdef, Props props) {
		this(taskdef, props, null);
	}
	public ActionList(BasicTaskDefinition taskdef, Props props, String defaultActions) {
		maxNrofTries = props.getInt("maxNrofTries", 3);
		retryDelay = props.getLong("retryDelay", 30000);
		String actions=props.getString("actions",defaultActions);
		String[] parts=actions.split(",");
		//this.actions=new Action[parts.length];
		for (String name: parts) {
			name=name.trim();
			LayeredProps lprops=new LayeredProps(taskdef.gft.props.getProps("gft.global"));
			SimpleProps top=new SimpleProps();
			top.put("action",taskdef.gft.actions.get(name));
			top.put("channel",props);
			lprops.addLayer(top);
			if (props.get(name,null) instanceof Props)
				lprops.addLayer(props.getProps(name));
			lprops.addLayer(taskdef.gft.actions.get(name));
			lprops.addLayer(props);
			//lprops.addLayer(taskdef.gft.props.getProps("gft.global"));
				
			Action a=taskdef.createAction(lprops);
			if (a==null)
				throw new RuntimeException("Unknown action "+name);
			this.actions.put(name,a);
		}
	}
	public boolean safeToRetry() { return false; } // TODO: 

	public Object execute(Task task) {
		for (String name: actions.keySet()) {
			Action a=actions.get(name);
			if (logger.isDebugEnabled())
				logger.debug("action "+name+" started");
			boolean done=false;
			int nrofTries=0;
			while (! done){
				try {
					a.execute(task);
					done=true;
				}
				catch (RetryableException e) {
					if (a.safeToRetry() && nrofTries <= maxNrofTries) {
						logger.warn("Error during action "+name+", try number "+nrofTries+", will retry after "+retryDelay/1000+" seconds, error was ", e);
						nrofTries++;
						ThreadUtil.sleep(retryDelay);
					}
					else {
						logger.error("action "+name+" had error: "+e.getMessage());
						throw e;
					}
				}
				finally {
					// This needs to be done here, otherwise the log_error will always log the log_error action as last action.
					// TODO: a more elegant solution is desired
					task.setLastAction(name);
				}
				if (logger.isInfoEnabled())
					logger.info("action "+name+" succesful");
			}
		}
		return null;
	}
	
	public boolean contains(Class<?> cls) {
		for (Action a: actions.values()) {
			if (cls.isAssignableFrom(a.getClass()))
				return true;
		}
		return false;
	}
}
