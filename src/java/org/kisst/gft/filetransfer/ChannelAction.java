package org.kisst.gft.filetransfer;

import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;

public class ChannelAction implements Action {

	public Object execute(Task task) {
		FileTransferData t= (FileTransferData) task;
		return t.channel.execute(task);
	}

}
