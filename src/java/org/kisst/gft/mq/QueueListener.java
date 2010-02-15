package org.kisst.gft.mq;

public interface QueueListener {
	public void listen(MessageHandler handler);
	public void stopListening();
}
