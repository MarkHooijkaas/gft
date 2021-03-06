package org.kisst.jms;

import java.util.ArrayList;

public class MessageHandlerList implements MessageHandler{
	private final ArrayList<MessageHandler> list=new ArrayList<MessageHandler>();
	public void add(MessageHandler mh) { list.add(mh); }
	public boolean handle(JmsMessage msg) {
		for (MessageHandler mh: list) {
			boolean result = mh.handle(msg);
			if (result)
				return true;
		}
		return false;
	}
}
