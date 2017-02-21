package org.kisst.util.exception;

public class BasicFunctionalException extends RuntimeException implements FunctionalException {
	private static final long serialVersionUID = 1L;

	public BasicFunctionalException(String msg) { super(msg); }
	public BasicFunctionalException(String msg, Exception e) { super(msg, e); }
}
