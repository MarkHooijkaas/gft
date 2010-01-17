package org.kisst.gft.tasks;

import org.kisst.gft.task.TaskHandler;

public class EchoHandler implements TaskHandler {
	public Object execute(Object data) {
		System.out.println(data);
		return null;
	}

}
