package org.kisst.gft.poller;

import java.util.List;

import org.kisst.gft.task.LockedBySomeoneElseException;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.Action;
import org.kisst.gft.task.TaskQueue;

public class QueuePoller {
	private final TaskQueue queue;
	private final Action handler;
	
	public QueuePoller(TaskQueue queue, Action handler) {
		this.queue=queue;
		this.handler=handler;
	}
	
	public void pollOneItem() {
		handle (queue.getOneOpenTask());
	}

	public void pollOnce() {
		List<Task> tasks= queue.getAllOpenTasks();
		for (Task t:tasks)
			handle(t);
	}
	public void pollTillEmpty() {
		List<Task> tasks;
		do {
			tasks= queue.getSomeOpenTasks();
			for (Task t:tasks)
				handle(t);
		}
		while (tasks!=null && tasks.size()>0);
	}

	
	private void handle(Task t) {
		try {
			queue.lock(t);
		} catch (LockedBySomeoneElseException e) {
			System.out.println("Could not lock "+t);
			e.printStackTrace();
			return;
		}
		System.out.println("handling "+t);
		handler.execute(t);
		queue.done(t);
	}
}
