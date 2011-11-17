package org.kisst.gft.filetransfer;

import nl.duo.gft.LogUtil;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;
import org.kisst.jms.MessageHandler;
import org.kisst.util.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class StartFileTransferTask implements MessageHandler {
	final static Logger logger=LoggerFactory.getLogger(StartFileTransferTask.class); 

	private final GftContainer gft;
	
	public StartFileTransferTask(GftContainer gft) { this.gft=gft; }
	public boolean handle(JmsMessage msg) {
		FileTransferTask task;
		try {
			XmlNode message=new XmlNode(msg.getData());
			XmlNode content=message.getChild("Body").getChildren().get(0);
			Channel channel=gft.getChannel(content.getChildText("kanaal"));

			task=new FileTransferTask(channel, msg);
		}
		catch (RuntimeException e) {
			LogUtil.log("error", "jms", "handle", "exception", e.getMessage());
			throw e;
		}
		if (logger.isInfoEnabled())
			logger.info("file "+task.srcpath+" transfer task started");
		
		Monitor mon1 = MonitorFactory.start("channel:"+task.channel.name);
		try {
			task.run();
		}
		finally {
			mon1.stop();
		}
		//if (! t.isDone())
		//	t.save();
		return true;
	}

}
