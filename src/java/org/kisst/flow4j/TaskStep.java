package org.kisst.flow4j;

import org.kisst.cfg4j.Props;

public class TaskStep {
	public enum Status { OPEN, ABORTED, DONE }

	private final Task task; 
	private final History history;
	private Status status;

	public TaskStep(Task task, Props props) {
		this.task=task;
		this.history=new History(props.getSequence("history"));
		this.status=(Status) props.get("status");
		
	}
	public TaskStep(Task task) {
		this.task=task;
		this.history=new History();
		this.status=Status.OPEN;
	}
	public Task getTask() { return task; }
	public FlowStep getFlowStep() {return null; }
	public History getHistory() { return history; }
	public void logSucces(String msg) { history.trace(msg);}
	public void logError(String msg) { history.trace("ERROR: "+msg);}
	public void skip() { status=Status.DONE;}
	public void retry() { status=Status.OPEN;}
	public Status getStatus() { return status;}

}
