package org.kisst.gft.mq;

public interface MessageHandler {
	public boolean handle(MqMessage msg);
}
