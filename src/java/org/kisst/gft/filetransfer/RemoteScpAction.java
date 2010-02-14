package org.kisst.gft.filetransfer;

import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteScpAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(RemoteScpAction.class);

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String result=Ssh.ssh(ft.channel.cred, ft.channel.getHost(), "ls -la "+ft.file);
		logger.info("ssh result: {} ",result);
		return null;
	}

}
