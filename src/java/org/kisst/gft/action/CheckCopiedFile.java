package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;

public class CheckCopiedFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		// TODO: remember filesize
		if (! ft.channel.dest.fileExists(ft.channel.destdir, ft.destpath))
				throw new RuntimeException("Copied file "+ft.channel.destdir+"/"+ft.destpath+" does not seem to exist");
		return null;
	}
}
