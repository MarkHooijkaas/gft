package org.kisst.gft.action;

import java.util.LinkedHashMap;

import org.kisst.cfg4j.LayeredProps;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.task.Task;
import org.kisst.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionList  implements Action {
	final static Logger logger=LoggerFactory.getLogger(ActionList.class); 
	
	private final LinkedHashMap<String,Action> actions=new LinkedHashMap<String,Action>();

	private final int maxNrofTries;
	private final long retryDelay;

	public ActionList(Channel chan, Props props) {
		maxNrofTries = props.getInt("maxNrofTries", 3);
		retryDelay = props.getLong("retryDelay", 30000);
		String actions=props.getString("actions");
		String[] parts=actions.split(",");
		//this.actions=new Action[parts.length];
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
			lprops.addLayer(chan.gft.props.getProps("gft.global"));
				
			Action a=chan.createAction(lprops);
			if (a==null)
				throw new RuntimeException("Unknown action "+name);
			this.actions.put(name,a);
		}
	}
	public boolean safeToRetry() { return false; } // TODO: 

	public Object execute(Task task) {
		for (String name: actions.keySet()) {
			Action a=actions.get(name);
			task.setLastAction(name);
			if (logger.isDebugEnabled())
				logger.debug("action "+name+" started");
			boolean done=false;
			int nrofTries=0;
			while (! done){
				try {
					a.execute(task);
					done=true;
				}
				catch (RuntimeException e) {
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
				if (logger.isInfoEnabled())
					logger.info("action "+name+" succesful");
			}
		}
		return null;
	}
}
