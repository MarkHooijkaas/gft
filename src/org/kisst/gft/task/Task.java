package org.kisst.gft.task;

import java.util.Date;

public interface Task {
	public void lock() throws LockedBySomeoneElseException;
	public boolean isLocked();
	public void done();
	public void retry();
	public void retry(Date date);
	public void retry(long delay);
	public void error();
	
	//public void move(TaskQueue dest);
	//public void move(TaskQueue dest, long delay);
	public String getData();
	public TaskQueue getQueue();
}
