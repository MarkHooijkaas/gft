package org.kisst.gft.action;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;


public class EchoAction implements Action {
	private final GftContainer gft;
	public final Props props;
	private final String template;
	
	public EchoAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		template =props.getString("template");
	}

	@Override public boolean safeToRetry() { return true; }

	@Override public void execute(Task task) {
		BasicTask basictask= (BasicTask) task;
		String result=gft.processTemplate(template, basictask.getActionContext(this));

		System.out.println(result);
	}

}
