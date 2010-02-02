package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.task.Task;

public class FileTransferTask implements Task {
	public final GftContainer gft;
	public final Channel channel;
	public final String file;
	public final Props props;
	
	public FileTransferTask(GftContainer gft, String data) {
		this.gft=gft;
		String[] param=data.split("[\n]");
		this.channel=gft.getChannel(param[0]);
		this.file=param[1];
		SimpleProps p = new SimpleProps();
		p.put("file",file);
		props=p;
	}

}
