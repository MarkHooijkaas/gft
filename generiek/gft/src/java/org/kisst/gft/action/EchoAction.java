package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;
import org.kisst.util.StringUtil;


public class EchoAction implements Action {
	private final Props actionProps;
	private final String template;
	
	public EchoAction(GftContainer gft, Props props) {
		this.actionProps=props;
		template =props.getString("template");
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		SimpleProps props=new SimpleProps();
		props.put("action", actionProps);
		props.put("file", ft.file);
		props.put("channel", ft.channel.props);

		String result=StringUtil.substitute(template, props);

		System.out.println(result);
		return null;
	}

}
