package org.kisst.jms;


public interface MessageHandler {
	public boolean handle(JmsMessage msg);
}
