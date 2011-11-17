package org.kisst.gft.task;

import java.io.File;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.props4j.SimpleProps;


public class BasicTask implements Task {
	public final GftContainer gft;
	private final TaskDefinition taskdef;
	
	private final SimpleProps vars=new SimpleProps();;
	private final SimpleProps context;

	private Task.Status status=null;
	private Exception lastError=null;
	private String lastAction=null;
	
	public BasicTask(GftContainer gft, TaskDefinition taskdef) {
		this.gft = gft;
		this.context=gft.getContext().shallowClone();
		this.context.put("var", vars);
		this.context.put("task", this);
		this.taskdef = taskdef;
	}
	public TaskDefinition getTaskDefinition() { return taskdef; }
	
	public void save() {  throw new RuntimeException("save not implemented yet"); }
	public Status getStatus() { return status; }
	public boolean isDone() { return status==DONE; }
	public void setStatus(Status status) { this.status=status;}

	public Exception getLastError() { return lastError; }
	public void setLastError(Exception e) {	this.lastError=e; }
	
	public String getLastAction() { return lastAction; }
	public void setLastAction(String act) {	this.lastAction=act; }

	public void setVar(String name, Object value) { vars.put(name, value); }
	public SimpleProps getContext() { return context; }
	
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
