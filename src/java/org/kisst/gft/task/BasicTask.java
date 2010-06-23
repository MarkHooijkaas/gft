package org.kisst.gft.task;

import org.kisst.cfg4j.SimpleProps;

public class BasicTask implements Task {
	private final SimpleProps data=new SimpleProps();;
	private Task.Status status=null;
	private Exception lastError=null;
	private String lastAction=null;
	
	public SimpleProps getData() { return data; }
	public void save() {  throw new RuntimeException("save not implemented yet"); }
	public Status getStatus() { return status; }
	public boolean isDone() { return status==DONE; }
	public void setStatus(Status status) { this.status=status;}

	public Exception getLastError() { return lastError; }
	public void setLastError(Exception e) {	this.lastError=e; }
	
	public String getLastAction() { return lastAction; }
	public void setLastAction(String act) {	this.lastAction=act; }

}
