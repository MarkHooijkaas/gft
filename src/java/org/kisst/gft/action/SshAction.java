package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;
import org.kisst.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SshAction.class);
	private final Props actionProps;
	private final String commandTemplate;
	private final SshHost host;
	
	public SshAction(GftContainer gft, Props props) {
		this.actionProps=props;
		commandTemplate =props.getString("command");
		host=gft.sshhosts.get(props.getString("host"));
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String command=StringUtil.substitute(commandTemplate, ft.getProps(actionProps));
		logger.info("ssh call to {} with command {}", host, command);
		String result=host.call(command);
		logger.info("ssh result {}",result);
		return null;
	}

}
