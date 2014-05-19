package org.kisst.flow4j;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.kisst.gft.RetryableException;
import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;
import org.kisst.util.ThreadUtil;
import org.kisst.util.exception.BasicFunctionalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class BasicLinearFlow {
	final static Logger logger=LoggerFactory.getLogger(BasicLinearFlow.class); 

	protected final LinkedHashMap<String, Action> actions= new LinkedHashMap<String, Action>();;
	private final HashSet<String> skippedActions = new HashSet<String>();

	private final int maxNrofTries;
	private final long retryDelay;
	private final boolean retryNonFunctionalExceptions;
	private final Props props;

	public BasicLinearFlow(Props props) {
		this.props=props;
		maxNrofTries = props.getInt("maxNrofTries", 3);
		retryDelay = props.getLong("retryDelay", 30000);
		retryNonFunctionalExceptions = props.getBoolean("retryNonFunctionalExceptions", true);
	}
	public boolean safeToRetry() { return false; } // TODO:
	/*
	protected void initFlow() {
		LinkedHashMap<String, Action> result = ReflectionUtil.findFieldsImplementing(this, Action.class);
		for (String name: result.keySet()) {
			addActions.put(name, result.get(name));
		}
	}*/

	protected boolean isSkippedAction(String name) { return skippedActions .contains(name); }
	
	private<T> T addAction(String name, T act) {
		if (! (act instanceof Action))
			throw new IllegalArgumentException("Trying to add action "+act+" of type"+act.getClass().getName()+", which does not implement the Action interface");
		Action act2 = actions.get(name);
		if (act2!=null)
			throw new RuntimeException("Action name "+name+" is already in use with "+act2+" when trying to add action "+act+" to Flow "+this);
		actions.put(name, (Action) act);
		return act;
	}
	
	@SuppressWarnings("unchecked")
	protected<T> T addAction(String name, Class<T> cls) {
		Props props = getActionConstructorProps((Class<? extends Action>) cls);
		if (props.getBoolean("skip", false))
			skippedActions.add(name);
		T act=(T) myCreateAction(cls, props);
		return addAction(name, act);
	}
	protected<T> T addAction(Class<T> cls) { return this.addAction(cls.getSimpleName(), cls); }
	
	private Props getActionConstructorProps(Class<? extends Action> cls) {
		String actionName= cls.getSimpleName();
		Props actionprops = props.getProps(actionName,null);
		if (actionprops==null)
			return props;
		LayeredProps lprops=new LayeredProps(props);
		lprops.addLayer(actionprops);
		return lprops;
	}
	
	public Object execute(Task task) {
		for (String name: actions.keySet()) {
			if (isSkippedAction(name)) {
				logger.info("action {} skipped",name);
				continue;
			}
			Action a=actions.get(name);
			logger.info("action {} started",name);
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
						logger.error("action "+name+" had functional error: ",e);
						throw e;
					}
					if (! a.safeToRetry()) {
						logger.error("action "+name+" (which is not safe to retry) had error: ",e);
						throw e;
					}
					if ( (!retryNonFunctionalExceptions) && ! (e instanceof RetryableException)) {
						logger.error("action "+name+" had non-functional error: ",e);
						throw e;
					}
					if (nrofTries <= maxNrofTries) {
						logger.warn("Error during action "+name+", try number "+nrofTries+", will retry after "+retryDelay/1000+" seconds, error was ", e);
						nrofTries++;
						ThreadUtil.sleep(retryDelay);
					}
					else {
						logger.error("action "+name+" had "+(nrofTries+1)+" tries, last error: ",e);
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
	protected Action myCreateAction(Class<?> clz, Props props) {
		Constructor<?> c=ReflectionUtil.getConstructor(clz, new Class<?>[] {BasicLinearFlow.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {this, props} );
		return (Action) ReflectionUtil.createObject(clz);
	}

	
}
