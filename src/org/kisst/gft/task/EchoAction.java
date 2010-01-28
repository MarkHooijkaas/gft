package org.kisst.gft.task;


public class EchoAction implements Action {
	public Object execute(Task t) {
		System.out.println(t);
		return null;
	}

}
