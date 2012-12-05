package org.kisst.gft.task;

import java.io.File;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.props4j.SimpleProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BasicTask implements Task {
	private final static Logger logger=LoggerFactory.getLogger(BasicTask.class);

	public final GftContainer gft;
	private final TaskDefinition taskdef;
	
	private final SimpleProps vars=new SimpleProps();;
	private final SimpleProps context;

	private Exception lastError=null;
	private String lastAction=null;
	
	public BasicTask(GftContainer gft, TaskDefinition taskdef) {
		this.gft = gft;
		this.context=gft.getContext().shallowClone();
		this.context.put("var", vars);
		this.context.put("task", this);
		this.taskdef = taskdef;
	}
	
	public String toString() {
		StringBuilder result =new StringBuilder();
		result.append(taskdef.getName());
		result.append(vars);
		return result.toString();
	}
	
	public TaskDefinition getTaskDefinition() { return taskdef; }
	@Override	public String getIdentification() { return this.toString(); }
	public void run() { taskdef.run(this); }
	
	public void save() {  throw new RuntimeException("save not implemented yet"); }

	public Exception getLastError() { return lastError; }
	public void setLastError(Exception e) {	this.lastError=e; }
	
	public String getLastAction() { return lastAction; }
	public void setLastAction(String act) {	this.lastAction=act; }

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
		if (dir.startsWith("dynamic:"))
			return gft.processTemplate(dir.substring(8)+"/"+file, getContext());
		else
			return dir+"/"+file;
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
