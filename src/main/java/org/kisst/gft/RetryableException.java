package org.kisst.gft;

public class RetryableException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public RetryableException(Throwable err) { super(err); }
	public RetryableException(String msg) { super(msg); }
	public RetryableException(String msg, Throwable err) { super(msg, err); }
}
