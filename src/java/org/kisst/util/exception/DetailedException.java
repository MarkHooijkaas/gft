package org.kisst.util.exception;

public class DetailedException extends RuntimeException implements HasDetails {
	private static final long serialVersionUID = 1L;
	private final String details;

	public DetailedException(String msg, String details) { super(msg); this.details=details;}
	public DetailedException(String msg, Throwable err, String details) { super(msg, err); this.details=details;}
	
	public String getDetails() { return details; } 
}
