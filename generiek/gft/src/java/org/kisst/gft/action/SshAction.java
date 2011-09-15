package org.kisst.gft.action;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.ssh.SshHost;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SshAction.class);
	private final GftContainer gft;
	private final String commandTemplate;
	private final SshHost host;
	private final boolean safeToRetry;
	
	public SshAction(GftContainer gft, Props props) {
		this.gft=gft;
		commandTemplate =props.getString("command");
		host=gft.sshhosts.get(props.getString("host"));
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	public boolean safeToRetry() { return safeToRetry; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		String command=gft.processTemplate(commandTemplate, ft.getActionContext(this));
		logger.info("ssh call to {} with command {}", host, command);
		String result=host.call(command);
		logger.info("ssh result {}",result);
		return null;
	}

}
