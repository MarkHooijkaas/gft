package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.task.Task;


public class EchoAction implements Action {
	public EchoAction(GftContainer gft, Props p) {}
	public Object execute(Task t) {
		System.out.println(t);
		return null;
	}

}
