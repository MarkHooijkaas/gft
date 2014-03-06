package org.kisst.gft.action;

import java.util.LinkedHashMap;

import org.kisst.gft.RetryableException;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ThreadUtil;
import org.kisst.util.exception.BasicFunctionalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ActionList  implements Action {
	final static Logger logger=LoggerFactory.getLogger(ActionList.class); 
	
	private final LinkedHashMap<String,Action> actions=new LinkedHashMap<String,Action>();

	private final int maxNrofTries;
	private final long retryDelay;
	private final boolean retryNonFunctionalExceptions;

	public ActionList(BasicTaskDefinition taskdef, Props props) {
		this(taskdef, props, null);
	}
	public ActionList(BasicTaskDefinition taskdef, Props props, String defaultActions) {
		maxNrofTries = props.getInt("maxNrofTries", 3);
		retryDelay = props.getLong("retryDelay", 30000);
		retryNonFunctionalExceptions = props.getBoolean("retryNonFunctionalExceptions", true);
		String actions=props.getString("actions",defaultActions);
		String[] parts=actions.split(",");
		//this.actions=new Action[parts.length];
		for (String name: parts) {
			name=name.trim();
			LayeredProps lprops=new LayeredProps(taskdef.gft.props.getProps("global"));
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
			if (logger.isInfoEnabled())
				logger.info("action "+name+" started");
			boolean done=false;
			int nrofTries=0;
			while (! done){
				Monitor mon1=MonitorFactory.start("action:"+name);
				Monitor mon2=null;
				String channelName= task.getTaskDefinition().getName();
				mon2=MonitorFactory.start("channel:"+channelName+":action:"+name);
				try {
					a.execute(task);
					done=true;
				}
				catch (RuntimeException e) {
					if (e instanceof BasicFunctionalException) {
						logger.error("action "+name+" had functional error: "+e.getMessage());
						throw e;
					}
					if (! a.safeToRetry()) {
						logger.error("action "+name+" (which is not safe to retry) had error: "+e.getMessage());
						throw e;
					}
					if ( (!retryNonFunctionalExceptions) && ! (e instanceof RetryableException)) {
						logger.error("action "+name+" had non-functional error: "+e.getMessage());
						throw e;
					}
					if (nrofTries <= maxNrofTries) {
						logger.warn("Error during action "+name+", try number "+nrofTries+", will retry after "+retryDelay/1000+" seconds, error was ", e);
						nrofTries++;
						ThreadUtil.sleep(retryDelay);
					}
					else {
						logger.error("action "+name+" had "+(nrofTries+1)+" tries, last error: "+e.getMessage());
						throw e;
					}
				}
				finally {
					mon1.stop();
					if (mon2!=null) mon2.stop();
					// This needs to be done here, otherwise the log_error will always log the log_error action as last action.
					// TODO: a more elegant solution is desired
					task.setLastAction(name);
				}
				if (done && logger.isInfoEnabled())
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
