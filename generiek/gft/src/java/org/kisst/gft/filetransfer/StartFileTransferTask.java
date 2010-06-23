package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartFileTransferTask implements MessageHandler {
	final static Logger logger=LoggerFactory.getLogger(StartFileTransferTask.class); 

	private final GftContainer gft;
	
	public StartFileTransferTask(GftContainer gft) { this.gft=gft; }
	public void handle(MqMessage msg) {
		FileTransferTask task=new FileTransferTask(gft, msg.getData(), msg.getReplyTo(), msg.getCorrelationId());
		if (logger.isInfoEnabled())
			logger.info("file "+task.srcpath+" transfer task started");
		
		task.run();
		//if (! t.isDone())
		//	t.save();
	}

}
