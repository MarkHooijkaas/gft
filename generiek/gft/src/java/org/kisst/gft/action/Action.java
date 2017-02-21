package org.kisst.gft.action;

import org.kisst.gft.task.Task;

public interface Action {
	public Object execute(Task task);
	public boolean safeToRetry();
}
