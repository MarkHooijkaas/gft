package org.kisst.gft.mq;

import java.util.List;

public class QueuePoller {
	private final MqQueue queue;
	private final MessageHandler handler;
	
	public QueuePoller(MqQueue queue, MessageHandler handler) {
		this.queue=queue;
		this.handler=handler;
	}
	
	public void pollOneItem() {
		handle (queue.getOneMessage());
	}

	public void pollOnce() {
		List<MqMessage> messages= queue.getAllMessages();
		for (MqMessage msg:messages)
			handle(msg);
	}
	public void pollTillEmpty() {
		List<MqMessage> messages;
		do {
			messages= queue.getSomeMessages();
			for (MqMessage msg:messages)
				handle(msg);
		}
		while (messages!=null && messages.size()>0);
	}

	
	private void handle(MqMessage msg) {
		try {
			msg.lock();
		} catch (LockedBySomeoneElseException e) {
			System.out.println("Could not lock "+msg);
			e.printStackTrace();
			return;
		}
		System.out.println("handling "+msg);
		handler.handle(msg);
		msg.done();
	}
}
