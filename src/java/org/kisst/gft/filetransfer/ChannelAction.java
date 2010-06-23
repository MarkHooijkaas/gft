package org.kisst.gft.filetransfer;

import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;

public class ChannelAction implements Action {

	public boolean safeToRetry() { return false; } // TODO: 

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		return ft.channel.execute(task);
	}

}
