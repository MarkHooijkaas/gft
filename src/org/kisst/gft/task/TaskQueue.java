package org.kisst.gft.task;

import java.util.Date;
import java.util.List;

public interface TaskQueue {
	public TaskSystem getSystem();
	public String getName();
	public int size();
	
	public Task getOneOpenTask();
	public List<Task> getAllOpenTasks();
	public List<Task> getSomeOpenTasks();

	public void sendTask(TaskType type, String data);
	public void sendTask(TaskType type, String data, Date scheduledTime);
	public void sendTask(TaskType type, String data, long delay);
}
