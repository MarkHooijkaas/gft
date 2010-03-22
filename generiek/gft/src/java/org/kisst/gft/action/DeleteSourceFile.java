package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;

public class DeleteSourceFile implements Action {

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		ft.channel.src.deleteFile(ft.channel.srcdir+"/"+ft.file);
		return null;
	}

}
