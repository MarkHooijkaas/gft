package org.kisst.gft.task;

import java.util.HashMap;
import java.util.Map;

import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;


public class BasicTask implements Task {
	public final GftContainer gft;
	private final SimpleProps vars=new SimpleProps();;
	private final HashMap<String, Object> context;

	private Task.Status status=null;
	private Exception lastError=null;
	private String lastAction=null;
	
	public BasicTask(GftContainer gft) {
		this.gft = gft;
		this.context=new HashMap<String, Object>();
		this.context.put("var", vars);
		this.context.put("task", this);
	}
	public void save() {  throw new RuntimeException("save not implemented yet"); }
	public Status getStatus() { return status; }
	public boolean isDone() { return status==DONE; }
	public void setStatus(Status status) { this.status=status;}

	public Exception getLastError() { return lastError; }
	public void setLastError(Exception e) {	this.lastError=e; }
	
	public String getLastAction() { return lastAction; }
	public void setLastAction(String act) {	this.lastAction=act; }

	public void setVar(String name, Object value) { vars.put(name, value); }
	public Map<String, Object> getContext() { return context; }
	
	public Map<String, Object> getActionContext(Action action) {
		Map<String, Object> result=new HashMap<String, Object>(getContext());
		result.put("action", action);
		return result;
	}
}
