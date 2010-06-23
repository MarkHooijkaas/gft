package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;

public class DeleteSourceFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		ft.channel.src.deleteFile(ft.srcpath);
		return null;
	}

}
