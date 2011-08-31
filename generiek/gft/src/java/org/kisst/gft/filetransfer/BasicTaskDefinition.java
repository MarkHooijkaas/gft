package org.kisst.gft.filetransfer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTaskDefinition implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 

	public final GftContainer gft;
	public final String name;
	private final Action action;
	private final Action startAction;
	private final Action endAction;
	private final Action errorAction;
	public final Props props;
	private final HashMap<String, Object> context;


	public BasicTaskDefinition(GftContainer gft, Props props) {
		this.gft=gft;
		context=new HashMap<String, Object>(gft.getContext());

		this.props=props;
		this.name=props.getLocalName();
		this.action=new ActionList(this, props);
		SimpleProps actprops=new SimpleProps();

		actprops.put("actions", "log_error");
		this.errorAction=new ActionList(this, actprops);

		actprops.put("actions", "log_start");
		this.startAction=new ActionList(this, actprops);
		
		actprops.put("actions", "log_completed");
		this.endAction=new ActionList(this, actprops);
		// TODO Auto-generated constructor stub
	}

	public Map<String,Object> getContext() { return context;}

	public void run(Task task) {
		try {
			startAction.execute(task);
			action.execute(task);
			endAction.execute(task);
			task.setStatus(Task.DONE);
		}
		catch (RuntimeException e) {
			task.setLastError(e);
			try {
				if (errorAction!=null)
					errorAction.execute(task);
			}
			catch(RuntimeException e2) { 
				logger.error("Could not perform the error actions ",e);
				// ignore this error which occurred 
			}
			throw e;
		}
	}
	
	public Action createAction(Props props) {
		String classname=props.getString("class",null);
		if (classname==null)
			return null;
		if (classname.indexOf('.')<0)
			classname="org.kisst.gft.action."+classname;
		if (classname.startsWith(".")) // Prefix a class in the default package with a .
			classname=classname.substring(1);
		Constructor<?> c=ReflectionUtil.getConstructor(classname, new Class<?>[] {Channel.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {this, props} );

		c=ReflectionUtil.getConstructor(classname, new Class<?>[] {GftContainer.class, Props.class} );
		if (c==null)
			return (Action) ReflectionUtil.createObject(classname);
		else
			return (Action) ReflectionUtil.createObject(c, new Object[] {gft, props} );
		
	}



}