package org.kisst.gft.action;

import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;

public class CopyFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		Channel chan=ft.channel;
		SshHost src=chan.src;
		SshHost dest=chan.dest;
		String srcdir=chan.srcdir;
		String destdir=chan.destdir;
		String mode=chan.mode;
		
		if ("push".equals(mode))
			src.copyFileTo(srcdir+"/"+ft.srcpath, dest, destdir+"/"+ft.destpath);
		else if ("pull".equals(mode))
			dest.copyFileFrom(src, srcdir+"/"+ft.srcpath, destdir+"/"+ft.destpath);
		return null;
	}

}
