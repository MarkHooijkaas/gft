package org.kisst.gft.action;

//import org.kisst.gft.LogService;
import org.kisst.gft.RetryableException;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.ThreadUtil;
import org.kisst.util.exception.BasicFunctionalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ActionExecutor {
	final static Logger logger=LoggerFactory.getLogger(ActionExecutor.class); 
	private final int maxNrofTries;
	private final long retryDelay;
	private final boolean retryNonFunctionalExceptions;
	
	public ActionExecutor(Props props) {
		maxNrofTries = props.getInt("maxNrofTries", 3);
		retryDelay = props.getLong("retryDelay", 30000);
		retryNonFunctionalExceptions = props.getBoolean("retryNonFunctionalExceptions", false);
	}
	
	private Object tryToExecute(Action a, Task task) {
		boolean done=false;
		Transaction trans=null;
		if (a instanceof Transaction)
			trans=(Transaction) a;
		try {
			if (trans!=null)
				trans.prepareTransaction(task);
			a.execute(task);
			done=true;
			return null;
		}
		finally {
			if (trans!=null) {
				if (done)
					trans.commitTransaction(task);
				else
					trans.rollbackTransaction(task);
			}
		}
	}
	
	public Object execute(Action a, String name, Task task) {
		if (name==null)
			name=a.getClass().getSimpleName();
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
				task.setCurrentAction(name);
				tryToExecute(a, task);
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
			}
			if (done && logger.isInfoEnabled())
				logger.info("action "+name+" succesful");
		}
		return null;
	}
}
