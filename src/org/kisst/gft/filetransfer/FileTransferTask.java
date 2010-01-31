package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.Task;

public class FileTransferTask implements Task {
	public final GftContainer gft;
	public final Channel channel;
	public final String file;
	
	public FileTransferTask(GftContainer gft, String data) {
		this.gft=gft;
		String[] param=data.split("[\n]");
		this.channel=gft.getChannel(param[0]);
		this.file=param[1];
	}

}
