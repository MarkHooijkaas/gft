package org.kisst.gft;

import java.util.ArrayList;

import org.kisst.gft.task.JmsTask;
import org.kisst.jms.JmsMessage;
import org.kisst.jms.MessageHandler;
import org.kisst.util.exception.MappedStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class TaskStarter implements MessageHandler {
	final static Logger logger=LoggerFactory.getLogger(TaskStarter.class); 

	public static interface JmsTaskCreator {
		public JmsTask createJmsTask(JmsMessage msg);
	}
	
	private final ArrayList<JmsTaskCreator> creators = new ArrayList<JmsTaskCreator>();
	
	public void appendCreator(JmsTaskCreator creator) { creators.add(creator); }
	
	public boolean handle(JmsMessage msg) {
		JmsTask task=createJmsTask(msg);
		
		if (logger.isInfoEnabled())
			logger.info(task+" started");
		
		Monitor mon1 = MonitorFactory.start("channel:"+task.getTaskDefinition().getName());
		try {
			task.run();
		}
		catch (Exception e) {
			MappedStateException mse = new MappedStateException(e);
			try {
				task.addState(mse);
			}
			catch (RuntimeException e2) { logger.error("Error when adding state info to Exception for task "+task,e2); }
			throw(mse);
		}
		finally {
			mon1.stop();
		}
		//if (! t.isDone())
		//	t.save();
		return true;
	}

	protected JmsTask createJmsTask(JmsMessage msg) {
		for (JmsTaskCreator c : creators) {
			try {
				JmsTask task = c.createJmsTask(msg);
				if (task !=null)
					return task;
			}
			catch (RuntimeException e) {
				LogService.log("error", "jms", "handle", "exception", e.getMessage());
				throw e;
			}
		}	
		LogService.log("error", "jms", "handle", "exception", "could not determine task type of message "+msg);
		throw new RuntimeException("could not determine task type of message "+msg);
	}
}
