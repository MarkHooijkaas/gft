package org.kisst.gft.mq;


public interface MqQueue {
	public String getName();
	public void send(String data);
	public void send(String data, String replyTo, String correlationId);
}
