package org.kisst.gft.mq;

import java.util.List;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;

public class QueuePoller {
	private final GftContainer gft;
	private final MqQueue queue;
	private final Action handler;
	
	public QueuePoller(GftContainer gft, MqQueue queue, Action handler) {
		this.gft=gft;
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
		Task tsk=new FileTransferTask(gft, msg.getData());
		handler.execute(tsk);
		msg.done();
	}
}
