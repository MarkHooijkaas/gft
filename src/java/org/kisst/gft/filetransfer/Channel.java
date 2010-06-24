package org.kisst.gft.filetransfer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.RetryableException;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.util.FileUtil;
import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Channel implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(Channel.class); 

	public final GftContainer gft;
	public final String name;
	private final Action action;
	private final Action startAction;
	private final Action endAction;
	private final Action errorAction;
	public final Props props;
	public final SshHost src;
	public final SshHost dest;
	private final String srcdir;
	private final String destdir;
	public final String mode;
	private final HashMap<String, Object> context;

	public Channel(GftContainer gft, Props props) {
		context=new HashMap<String, Object>(gft.getContext());
		context.put("channel", this);
		
		this.gft=gft;
		this.src=gft.sshhosts.get(props.getString("src.host"));
		this.dest=gft.sshhosts.get(props.getString("dest.host"));

		String dir=props.getString("src.dir",  "");
		if (dir.startsWith("dynamic:"))
			this.srcdir=dir;
		else
			this.srcdir =gft.processTemplate(dir, context); 

		dir=props.getString("dest.dir",  "");
		if (dir.startsWith("dynamic:"))
			this.destdir=dir;
		else
			this.destdir =gft.processTemplate(dir, context);

		this.mode=props.getString("mode", "push");
		if (!("pull".equals(mode) || "push".equals(mode)))
			throw new RuntimeException("mode should be push or pull, not "+mode);
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
	}
	
	public Map<String,Object> getContext() { return context;}
	public String toString() { return "Channel("+name+" from "+src+":"+srcdir+" to "+dest+":"+destdir+")";}
	public void checkSystemsAvailable(FileTransferTask ft) {
		if (! src.isAvailable())
			throw new RetryableException("Source system "+src+" is not available tot transfer file "+ft.srcpath+" for channel "+name);
		if (! dest.isAvailable())
			throw new RetryableException("Destination system "+dest+" is not available tot transfer file "+ft.destpath+" for channel "+name);
	}

	private String calcPath(String dir, String file, FileTransferTask ft) {
		while (file.startsWith("/"))
			file=file.substring(1);
		if (file.indexOf("..")>=0)
			throw new RuntimeException("filename ["+file+"] is not allowed to contain .. pattern");
		// TODO: check for more unsafe constructs
		if (dir.startsWith("dynamic:"))
			return gft.processTemplate(dir.substring(8)+"/"+file, ft.getContext());
		else
			return dir+"/"+file;
	}

	
	public String getSrcPath(String file, FileTransferTask ft) { return calcPath(srcdir, file, ft); }
	public String getDestPath(String file, FileTransferTask ft) {return FileUtil.filename(calcPath(destdir, file, ft));	}
	
	public void run(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		checkSystemsAvailable(ft);
		
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
