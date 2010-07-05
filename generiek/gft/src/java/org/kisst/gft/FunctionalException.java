package org.kisst.gft;

public class FunctionalException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FunctionalException(String msg) { super(msg); }
	public FunctionalException(String msg, Exception e) { super(msg, e); }
}
