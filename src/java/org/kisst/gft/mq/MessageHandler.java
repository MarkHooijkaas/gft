package org.kisst.gft.mq;

public interface MessageHandler {
	public void handle(MqMessage msg);
}
