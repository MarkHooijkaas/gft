package org.kisst.gft.task;

import java.lang.reflect.Constructor;

import org.kisst.gft.GftContainer;
import org.kisst.gft.LogService;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTaskDefinition implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 

	public final GftContainer gft;
	public final String name;
	protected final Action action;

	public final Props props;

	private long totalCount=0;
	private long errorCount=0;

	// This constructor has a bit bogus defaultActions parameter that is needed for the other constructor
	// In future this parameter might be removed
	public BasicTaskDefinition(GftContainer gft, Props props, Action flow, String defaultActions) {
		this.gft=gft;
		this.props=props;
		this.name=props.getLocalName();
		if (flow!=null)
			this.action=flow;
		else
			this.action=new ActionList(this, props, defaultActions);
	}

	// This constructor is for backward compatibility 
	@Deprecated
	public BasicTaskDefinition(GftContainer gft, Props props, String defaultActions) {
		this(gft,props,null,defaultActions);
	}
	// This method is for backward compatibility
	@Deprecated
	public SimpleProps getContext() { throw new RuntimeException("Deprecated method called in task "+getName()); }

	public String getName() { return name; }
	public long getTotalCount() { return totalCount; }
	public long getErrorCount() { return errorCount; }
	
	public void run(Task task) {
		try {
			totalCount++;
			logStart(task);
			action.execute(task);
			logCompleted(task);
		}
		catch (RuntimeException e) {
			errorCount++;
			task.setLastError(e);
			try {
				logError(task, e);
			}
			catch(RuntimeException e2) { 
				logger.error("Could not perform the error actions ",e);
				// ignore this error which occurred 
			}
			throw e;
		}
	}
	
	public Action createAction(Props props) {
		try {
			return myCreateAction(props);
		}
		catch (RuntimeException e) {
			throw new RuntimeException("Error when creating action in channel "+getName(),e);
		}
	}
	
	private Action myCreateAction(Props props) {
		String classname=props.getString("class",null);
		if (classname==null)
			return null;
		if (classname.indexOf('.')<0)
			classname="org.kisst.gft.action."+classname;
		if (classname.startsWith(".")) // Prefix a class in the default package with a .
			classname=classname.substring(1);
		
		Class<?> clz;
		try {
			clz= gft.getSpecialClassLoader().loadClass(classname);
		} catch (ClassNotFoundException e) { throw new RuntimeException(e); }
		
		
		Constructor<?> c=ReflectionUtil.getConstructor(clz, new Class<?>[] {Channel.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {this, props} );

		c=ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c==null)
			return (Action) ReflectionUtil.createObject(classname);
		else
			return (Action) ReflectionUtil.createObject(c, new Object[] {gft, props} );
		
	}
	
	
	private void logStart(Task task) {
		LogService.log("info", "start", task.getTaskDefinition().getName(), "started", "Started"+getLogDetails(task)); 
	}
	
	private void logCompleted(Task task) {
		LogService.log("info", "done", task.getTaskDefinition().getName(), "completed","Completed"+getLogDetails(task));
	}
	private void logError(Task task, RuntimeException e) {
		String details = "Fout bij actie:"+task.getLastAction()+" fout:"+e.getMessage()+getLogDetails(task);
		LogService.log("error", task.getLastAction(), task.getTaskDefinition().getName(), "error", details);
	}
	
	private String getLogDetails(Task task) {
		if (task instanceof FileTransferTask) {
			FileTransferTask ft = (FileTransferTask) task;
			return  "bestand: "+ft.srcpath+ ", van: "+ft.channel.src+"/"+ft.srcpath+" naar: "+ft.channel.dest+"/"+ft.destpath;
		}
		else
			return task.toString();
	}
}