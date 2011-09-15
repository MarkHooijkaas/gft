package org.kisst.gft.action;

import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.filetransfer.RemoteFileServer;
import org.kisst.gft.task.Task;

public class CopyFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		Channel chan=ft.channel;
		RemoteFileServer src=chan.src;
		RemoteFileServer dest=chan.dest;
		String mode=chan.mode;
		
		if ("push".equals(mode))
			src.copyFileTo(ft.srcpath, dest, ft.destpath);
		else if ("pull".equals(mode))
			dest.copyFileFrom(src, ft.srcpath, ft.destpath);
		return null;
	}

}
