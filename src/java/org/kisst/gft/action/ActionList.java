package org.kisst.gft.action;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

import org.kisst.gft.GftContainer;
import org.kisst.gft.RetryableException;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ReflectionUtil;
import org.kisst.util.ThreadUtil;
import org.kisst.util.exception.BasicFunctionalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class ActionList  implements Action, WritesHtml {
	final static Logger logger=LoggerFactory.getLogger(ActionList.class); 
	
	private final LinkedHashMap<String,Action> actions=new LinkedHashMap<String,Action>();

	private final BasicTaskDefinition taskdef;
	private final int maxNrofTries;
	private final long retryDelay;
	private final boolean retryNonFunctionalExceptions;

	

	public ActionList(BasicTaskDefinition taskdef, Props props) {
		this.taskdef=taskdef;
		maxNrofTries = props.getInt("maxNrofTries", 3);
		retryDelay = props.getLong("retryDelay", 30000);
		retryNonFunctionalExceptions = props.getBoolean("retryNonFunctionalExceptions", true);
		String actions=props.getString("actions");
		String[] parts=actions.split(",");
		//this.actions=new Action[parts.length];
		for (String name: parts) {
			name=name.trim();
			LayeredProps lprops=new LayeredProps(taskdef.gft.props.getProps("global"));
			SimpleProps top=new SimpleProps();
			//top.put("action",taskdef.gft.actions.get(name));
			top.put("channel",props);
			lprops.addLayer(top);
			if (props.get(name,null) instanceof Props)
				lprops.addLayer(props.getProps(name));
			// TODO: the action layer is only needed for the class which is the only property in it.
			// This could be simplified since no user defined actions are used any more
			lprops.addLayer(taskdef.gft.actions.get(name));
			lprops.addLayer(props);
			//lprops.addLayer(taskdef.gft.props.getProps("gft.global"));
				
			Action a=createAction(name, lprops);
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
	
	public Action createAction(String name, Props props) {
		try {
			return myCreateAction(props);
		}
		catch (RuntimeException e) {
			throw new RuntimeException("Error when creating action "+name+" in channel "+name,e);
		}
	}
	
	private  Action myCreateAction(Props props) {
		String classname=props.getString("class",null);
		if (classname==null)
			throw new RuntimeException("No action class provided "+props);
		if (classname.indexOf('.')<0)
			classname="org.kisst.gft.action."+classname;
		if (classname.startsWith(".")) // Prefix a class in the default package with a .
			classname=classname.substring(1);
		
		Class<?> clz;
		try {
			clz= taskdef.gft.getSpecialClassLoader().loadClass(classname);
		} catch (ClassNotFoundException e) { throw new RuntimeException(e); }
		
		
		Constructor<?> c = ReflectionUtil.getFirstCompatibleConstructor(clz, new Class<?>[] {BasicTaskDefinition.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef, props} );

		c = ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef.gft, props} );
		
		return (Action) ReflectionUtil.createObject(clz);		
	}
	@Override
	public void writeHtml(PrintWriter out) {
		out.println("<h2>Actions</h2>");
		out.println("<table>");
		//out.println("<tr><td>"+name+"</td><td>"+actions.get(name).toString()+"</td></tr>")
		for (String name: actions.keySet()) {
			out.println("<tr><td>"+name+"</td><td>"+actions.get(name).toString()+"</td></tr>");
		}
		out.println("</table>");
	}

}
