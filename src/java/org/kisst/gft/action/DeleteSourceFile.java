package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;

public class DeleteSourceFile implements Action {
	private final SshHost host;
	private final String directory;
	
	public DeleteSourceFile(GftContainer gft, Props props) {
		this.host=gft.sshhosts.get(props.getString("src.host"));
		this.directory=props.getString("src.dir", "");
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		host.deleteFile(directory+"/"+ft.bestand);
		return null;
	}

}
