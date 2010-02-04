package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.task.FileBasedTask;
import org.kisst.gft.task.Task;

public class StartFileTransferTask implements MessageHandler {
	private final GftContainer gft;
	
	public StartFileTransferTask(GftContainer gft) { this.gft=gft; }
	public void handle(MqMessage msg) {
		FileTransferData obj =new FileTransferData(gft, msg.getData());
		Task t=new FileBasedTask(obj.channel, obj);
		try {
			t.run();
			if (! t.isDone())
				t.save();
		}
		catch(Exception e) { t.save(); }
	}

}
