package org.kisst.gft.filetransfer;

import java.lang.reflect.Constructor;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTaskDefinition implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 

	public final GftContainer gft;
	public final String name;
	protected final Action action;
	protected Action startAction=null;
	protected Action endAction=null;
	protected Action errorAction=null;
	public final Props props;
	private final SimpleProps context;

	private long totalCount=0;
	private long errorCount=0;

	public BasicTaskDefinition(GftContainer gft, Props props, String defaultActions) {
		this.gft=gft;
		context=gft.getContext().shallowClone();

		this.props=props;
		this.name=props.getLocalName();
		this.action=new ActionList(this, props, defaultActions);
		
	}

	public String getName() { return name; }
	public SimpleProps getContext() { return context;}
	public long getTotalCount() { return totalCount; }
	public long getErrorCount() { return errorCount; }
	
	public void run(Task task) {
		try {
			totalCount++;
			if (startAction!=null)
				startAction.execute(task);
			action.execute(task);
			if (endAction!=null)
				endAction.execute(task);
			task.setStatus(Task.DONE);
		}
		catch (RuntimeException e) {
			errorCount++;
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