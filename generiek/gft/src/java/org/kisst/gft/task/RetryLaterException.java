package org.kisst.gft.task;

import java.util.Date;

public class RetryLaterException extends Exception{
	private static final long serialVersionUID = 1L;
	public final Date date;
	
	public RetryLaterException(String msg, Date date) {
		super(msg);
		this.date=date;
	}
	public RetryLaterException(String msg) {
		this(msg,null);
	}

}
