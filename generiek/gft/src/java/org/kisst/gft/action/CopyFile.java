package org.kisst.gft.action;

import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;

public class CopyFile implements Action {

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		Channel chan=ft.channel;
		SshHost src=chan.src;
		SshHost dest=chan.dest;
		String srcdir=chan.srcdir;
		String destdir=chan.destdir;
		String mode=chan.mode;
		
		if ("push".equals(mode))
			src.copyFileTo(srcdir+"/"+ft.file, dest, destdir);
		else if ("pull".equals(mode))
			dest.copyFileFrom(src, srcdir,ft.file, destdir);
		return null;
	}

}
