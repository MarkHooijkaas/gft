package org.kisst.gft;

import nl.duo.gft.LogUtil;

import org.kisst.gft.task.JmsTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.jms.JmsMessage;
import org.kisst.jms.MessageHandler;
import org.kisst.util.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class TaskStarter implements MessageHandler {
	final static Logger logger=LoggerFactory.getLogger(TaskStarter.class); 

	private final GftContainer gft;
	
	public TaskStarter(GftContainer gft) { this.gft=gft; }
	public boolean handle(JmsMessage msg) {
		Task task;
		try {
			XmlNode message=new XmlNode(msg.getData());
			XmlNode content=message.getChild("Body").getChildren().get(0);
			JmsTaskDefinition definition=(JmsTaskDefinition) gft.getTaskDefinition(content.getChildText("kanaal"));

			task=definition.createNewTask(msg);
		}
		catch (RuntimeException e) {
			LogUtil.log("error", "jms", "handle", "exception", e.getMessage());
			throw e;
		}
		if (logger.isInfoEnabled())
			logger.info(task+" started");
		
		Monitor mon1 = MonitorFactory.start("channel:"+task.getTaskDefinition().getName());
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
