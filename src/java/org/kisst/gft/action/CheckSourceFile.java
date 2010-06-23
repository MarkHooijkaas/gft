package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;

public class CheckSourceFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		// TODO: remember filesize
		if (! ft.channel.src.fileExists(ft.srcpath))
				throw new RuntimeException("Source file "+ft.srcpath+" does not exist or is not accessible");
		return null;
	}

}
