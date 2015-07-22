package org.kisst.gft.task;

import java.io.File;

import org.kisst.gft.GftContainer;
import org.kisst.gft.LogService;
import org.kisst.gft.action.Action;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.props4j.MultiProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.TemplateUtil;
import org.kisst.util.exception.MappedStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BasicTask implements Task {
	private final static Logger logger=LoggerFactory.getLogger(BasicTask.class);

	public final GftContainer gft;
	private final TaskDefinition taskdef;
	private final String identification;
	
	private final SimpleProps vars=new SimpleProps();;
	private final SimpleProps context;

	private Exception lastError=null;
	private String currentAction="StartingTask";

	private final boolean logAllActions;
	private boolean taskCanBeRetried=true;
	
	public BasicTask(GftContainer gft, TaskDefinition taskdef, String id) {
		this.gft = gft;
		this.identification=id;
		this.context=gft.getContext().shallowClone();
		Object defaultVars = taskdef.getProps().get("defaultVars", null);
		if (defaultVars instanceof Props)
			this.context.put("var", new MultiProps(vars, (Props) defaultVars));
		else
			this.context.put("var", vars);
		this.context.put("task", this);
		this.taskdef = taskdef;
		this.logAllActions=taskdef.getProps().getBoolean("logAllActions", false);
	}

	@Override public String toString() { return toString(identification); }
	protected String toString(String details) {	return this.getClass().getSimpleName()+"("+this.taskdef.getName()+":"+details+")"; }
	
	public String toFullString() {
		StringBuilder result =new StringBuilder();
		result.append(taskdef.getName());
		result.append(vars);
		return result.toString();
	}
	
	@Override public void logError(String msg) { LogService.log("error",getCurrentAction(), taskdef.getName(), getIdentification(), prepend(msg)); }
	@Override public void logWarn(String msg)  { LogService.log("warn", getCurrentAction(), taskdef.getName(), getIdentification(), prepend(msg)); }
	@Override public void logInfo(String msg)  { LogService.log("info", getCurrentAction(), taskdef.getName(), getIdentification(), prepend(msg)); }
	public void logDebug(String msg) { LogService.log("debug", getCurrentAction(), taskdef.getName(), getIdentification(), prepend(msg)); }
	private String prepend(String msg) {
		if (taskCanBeRetried)
			return "RETRYABLE: "+msg;
		return msg;
	}
	
	
	public TaskDefinition getTaskDefinition() { return taskdef; }
	@Override final public String getIdentification() { return identification; }
	public void run() { taskdef.run(this); }
	
	public void save() {  throw new RuntimeException("save not implemented yet"); }

	public Exception getLastError() { return lastError; }
	public void setLastError(Exception e) {	this.lastError=e; }
	
	@Override public void setCompleted() {this.currentAction="CompletedTask";}
	@Override public String getCurrentAction() { return currentAction; }
	@Override public void setCurrentAction(Action act) {
		this.currentAction=act.getClass().getSimpleName();
		if (!act.safeToRetry())
			taskCanBeRetried=false;
		if (logAllActions)
			logDebug("starting action "+act);
	}

	@Override public Object getFieldValue(String name) { throw new RuntimeException("getFieldValue not implemented for "+this.getClass()); }
	public Object getVar(String name) { return vars.get(name,null); }
	public String getStringVar(String name) { 
		Object result = vars.get(name,null);
		if (result == null || result instanceof String)
			return (String) result;
		throw new RuntimeException("Value of BasicTask variable "+name+" is not a String but a "+result.getClass()+" with value"+result);
	}
	public void setVar(String name, Object value) {
		logger.info("setting var "+name+" to "+value );
		vars.put(name, value);
	}
	public SimpleProps getContext() { return context; }
	
	public String calcPath(String dir, String file) {
		while (file.startsWith("/"))
			file=file.substring(1);
		// TODO: check for more unsafe constructs
		return dir+"/"+file;
	}
	protected FileLocation subsituteDynamicPath(FileLocation loc) {
		String path = loc.getPath();
		if (!path.startsWith("dynamic:"))
			return loc;
		//System.out.print(getContext().toPropertiesString());
		path=TemplateUtil.processTemplate(path.substring(8), getContext());
		return new FileLocation(loc.getFileServer(),path);
	}
	public void addState(MappedStateException mse) {
		mse.addState("CHANNEL", getTaskDefinition().getName());
		try {
			mse.addState("ID", getIdentification());
		} catch (RuntimeException e) {} // ignore
		mse.addState("ACTION", getCurrentAction());
		mse.addState("ERROR", getLastError().getMessage());
		for (String key: vars.keys())
			mse.addState("VAR_"+key, ""+vars.get(key, null));
		if (tempFile!=null)
			mse.addState("TEMPFILE", tempFile.getAbsolutePath());
		mse.addState("RETRY_SAFELY", ""+taskCanBeRetried);
	}
	
	public SimpleProps getActionContext(Action action) {
		SimpleProps result=getContext().shallowClone();
		result.put("action", action);
		return result;
	}
	
	public File getTempFile() { return getTempFile("file.tmp"); }
	private File  tempFile=null;
	protected File getTempFile(String filename) {
		if (tempFile!=null)
			return tempFile;
		File nieuwTempDir = gft.createUniqueDir(taskdef.getName());
		tempFile = new File(nieuwTempDir,filename);
		return tempFile;
	}
}
