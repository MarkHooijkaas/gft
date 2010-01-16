package org.kisst.gft.task;

public class CouldNotLockTaskException extends Exception{
	private static final long serialVersionUID = 1L;
	public final Task task;
	
	public CouldNotLockTaskException(Task t) {
		super("Could not lock task "+t);
		this.task=t;
	}
}
