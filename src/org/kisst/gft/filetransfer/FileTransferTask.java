package org.kisst.gft.filetransfer;

import org.kisst.gft.task.Task;

public class FileTransferTask implements Task {
	public final Channel channel;
	public final String file;
	
	public FileTransferTask(String data) {
		String[] param=data.split("[\n]");
		this.channel=Channel.getChannel(param[0]);
		this.file=param[1];
	}

}
