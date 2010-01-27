package org.kisst.gft.tasks;

import org.kisst.gft.task.Task;
import org.kisst.gft.task.Action;

public class EchoHandler implements Action {
	public Object execute(Task t) {
		System.out.println(t);
		return null;
	}

}
