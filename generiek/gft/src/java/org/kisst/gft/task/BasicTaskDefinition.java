package org.kisst.gft.task;

import java.lang.reflect.Constructor;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTaskDefinition extends AbstractTaskDefinition implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 
	protected final Action action;

	public BasicTaskDefinition(GftContainer gft, Props props, String defaultActions) {
		super(gft, props);
		this.action=new ActionList(this, props, defaultActions);
	}
	public SimpleProps getContext() { throw new RuntimeException("Deprecated method called in task "+getName()); }

	@Override protected void executeTask(Task task) { this.action.execute(task); }
	
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
			throw new RuntimeException("No action class provided "+props);
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
	
	@Override protected String getLogDetails(Task task) {
		if (task instanceof FileTransferTask) {
			FileTransferTask ft = (FileTransferTask) task;
			return  "bestand: "+ft.srcpath+ ", van: "+ft.channel.src+"/"+ft.srcpath+" naar: "+ft.channel.dest+"/"+ft.destpath;
		}
		else
			return task.toString();
	}
}