package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.Ssh;
import org.kisst.gft.task.Task;
import org.kisst.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SshAction.class);
	private final Props actionProps;
	private final String commandTemplate;
	
	public SshAction(GftContainer gft, Props props) {
		this.actionProps=props;
		commandTemplate =props.getString("command");
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String command=StringUtil.substitute(commandTemplate, ft.getProps(actionProps));

		String result=Ssh.ssh(ft.channel.cred, ft.channel.getHost(), command);
		logger.info("ssh result {}",result);
		return null;
	}

}
