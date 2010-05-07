package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;


public class EchoAction implements Action {
	private final GftContainer gft;
	public final Props props;
	private final String template;
	
	public EchoAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		template =props.getString("template");
	}

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String result=gft.processTemplate(template, ft.getActionContext(this));

		System.out.println(result);
		return null;
	}

}
