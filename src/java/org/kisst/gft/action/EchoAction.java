package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;


public class EchoAction implements Action {
	private final GftContainer gft;
	private final Props actionProps;
	private final String template;
	
	public EchoAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.actionProps=props;
		template =props.getString("template");
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String result=gft.processTemplate(template, ft.getProps(actionProps));

		System.out.println(result);
		return null;
	}

}
