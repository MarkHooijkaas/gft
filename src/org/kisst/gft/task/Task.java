package org.kisst.gft.task;

public interface Task {
	public TaskType getType();
	public void lock() throws CouldNotLockTaskException;
	public boolean isLocked();
	public void delete();
	public void move(TaskQueue dest);
	public void move(TaskQueue dest, long delay);
	public String getData();
	public TaskQueue getQueue();
}
