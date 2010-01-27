package org.kisst.gft.task;

import java.util.Date;
import java.util.List;

public interface TaskQueue {
	public String getName();
	public int size();
	
	public Task getOneOpenTask();
	public List<Task> getAllOpenTasks();
	public List<Task> getSomeOpenTasks();

	public void sendTask(Action type, String data);
	public void sendTask(Action type, String data, Date scheduledTime);
	public void sendTask(Action type, String data, long delay);
	public void lock(Task t) throws LockedBySomeoneElseException;
	public void done(Task t);
}
