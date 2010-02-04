package org.kisst.gft.mq;


public class LockedBySomeoneElseException extends Exception{
	private static final long serialVersionUID = 1L;
	public final MqMessage msg;
	
	public LockedBySomeoneElseException(MqMessage msg) {
		super("Could not lock message "+msg);
		this.msg=msg;
	}
}
