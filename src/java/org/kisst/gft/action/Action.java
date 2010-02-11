package org.kisst.gft.action;

import org.kisst.gft.task.Task;

public interface Action {
	Object execute(Task task);
}
