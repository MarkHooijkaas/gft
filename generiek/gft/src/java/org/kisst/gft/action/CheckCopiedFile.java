package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;

public class CheckCopiedFile implements Action {
	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		// TODO: remember filesize
		if (! ft.channel.dest.fileExists(ft.channel.destdir, ft.file))
				throw new RuntimeException("Copied file "+ft.channel.destdir+"/"+ft.file+" does not seem to exist");
		return null;
	}

}
