package org.kisst.gft.mq;


public interface MqMessage {
	public String getData();
	public String getReplyTo();
	public String getMessageId();
	public String getCorrelationId();
	
	public void lock() throws LockedBySomeoneElseException;
	public void done();
}
