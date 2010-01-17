package org.kisst.gft.task;

public class LockedBySomeoneElseException extends Exception{
	private static final long serialVersionUID = 1L;
	public final Task task;
	
	public LockedBySomeoneElseException(Task t) {
		super("Could not lock task "+t);
		this.task=t;
	}
}
