package org.kisst.gft.mq;


public interface MqMessage {
	public String getData();
	public void lock() throws LockedBySomeoneElseException;
	public void done();
}
