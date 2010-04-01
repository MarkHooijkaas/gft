package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.task.FileBasedTask;
import org.kisst.gft.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartFileTransferTask implements MessageHandler {
	final static Logger logger=LoggerFactory.getLogger(StartFileTransferTask.class); 

	private final GftContainer gft;
	
	public StartFileTransferTask(GftContainer gft) { this.gft=gft; }
	public void handle(MqMessage msg) {
		FileTransferData obj =new FileTransferData(gft, msg.getData(), msg.getReplyTo(), msg.getCorrelationId());
		if (logger.isInfoEnabled())
			logger.info("file "+obj.file+" transfer task started");
		
		Task t=new FileBasedTask(obj.channel, obj);
		t.run();
		//if (! t.isDone())
		//	t.save();
	}

}
