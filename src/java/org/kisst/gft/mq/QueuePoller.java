package org.kisst.gft.mq;

import java.util.List;

import org.kisst.cfg4j.Props;

public class QueuePoller implements Runnable {
	private final String name;
	private final MqQueue queue;
	private final MessageHandler handler;
	private long delay;
	private boolean running=false;
	
	public QueuePoller(String name, MqSystem sys, MessageHandler handler, Props props) {
		this.name=name;
		this.queue=sys.getQueue(props.getString("queue"));
		this.handler=handler;
		this.delay=props.getLong("delay");
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
		System.out.println(name+" handling "+msg);
		handler.handle(msg);
		msg.done();
	}
	
	public void stop() { running=false; }
	public void run() {
		running=true;
		while (running) {
			//System.out.println("polling");
			pollTillEmpty();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

}
