package org.kisst.gft.mq;


public interface MqQueue {
	public String getName();
	
	public void listen(MessageHandler handler);
	public void stopListening();
	
	public void send(String data);
}
