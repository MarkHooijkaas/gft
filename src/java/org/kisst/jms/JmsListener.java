package org.kisst.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kisst.cfg4j.Props;
import org.kisst.gft.FunctionalException;
import org.kisst.gft.RetryableException;
import org.kisst.util.TemplateUtil;
import org.kisst.util.TimeWindowList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsListener implements Runnable {
	private final static Logger logger=LoggerFactory.getLogger(JmsListener.class); 

	private final JmsSystem system;
	private final Props props;
	public final String queue;
	public final String errorqueue;
	public final String retryqueue;
	private final int receiveErrorRetries;
	private final int receiveErrorRetryDelay;
	private final long interval;
	private final TimeWindowList forbiddenTimes;

	private Session session = null;
	private Queue destination = null;
	private MessageConsumer consumer = null;
	private boolean running=false;

	private MessageHandler handler=null;

	public JmsListener(JmsSystem system, Props props, Object context) {
		this.system=system;
		this.props=props;
		this.interval=props.getLong("interval",5000);

		this.queue=TemplateUtil.processTemplate(props.getString("queue"), context);
		this.errorqueue=TemplateUtil.processTemplate(props.getString("errorqueue"), context);
		this.retryqueue=TemplateUtil.processTemplate(props.getString("retryqueue"), context);
		this.receiveErrorRetries = props.getInt("receiveErrorRetries", 1000);
		this.receiveErrorRetryDelay = props.getInt("receiveErrorRetryDelay", 60000);
		String timewindow=props.getString("forbiddenTimes", null);
		if (timewindow==null)
			this.forbiddenTimes=null;
		else
			this.forbiddenTimes=new TimeWindowList(timewindow);
	}

	public String toString() { return "JmsListener("+queue+")"; }

	public boolean isForbiddenTime() {
		return forbiddenTimes!=null && forbiddenTimes.isTimeInWindow();
	} 
	public void stop() { running=false; }

	public void run() {
		try {
			logger.info("Opening queue {}",queue);
			while (running) {
				Message message=null;
				message = getMessage();
				if (message!=null)
					handleMessage(message);
			}
		}
		catch (JMSException e) {
			logger.error("Unrecoverable error during listening, stopped listening", e);
			if (props.getBoolean("exitOnUnrecoverableListenerError", false))
				System.exit(1);
		}
		finally {
			try{
				logger.info("Stopped listening to queue {}", queue);
				closeSession();
			}
			finally {
				//TODO: notifyThreadStop(this);
			}
		}
	}

	private Message getMessage() throws JMSException {
		if (isForbiddenTime()) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {/* ignore */}
			return null;
		}
		int retryCount=0;
		try {
			if (session==null)
				openSession();
			Message message = consumer.receive(interval);
			if (message!=null)
				return message;
			retryCount=0;
		}
		catch (Exception e) {
			logger.error("Error when receiving JMS message on queue "+queue, e);
			if (retryCount++ > receiveErrorRetries)
				throw new RuntimeException("too many receive retries for queue "+queue);
			closeSession();
			sleepSomeTime();
		}
		return null;
	}

	private void sleepSomeTime() {
		logger.info("sleeping for "+receiveErrorRetryDelay/1000+" secs for retrying listening to "+queue);
		try {
			Thread.sleep(receiveErrorRetryDelay);
		}
		catch (InterruptedException e1) { throw new RuntimeException(e1); }
	}

	private void closeSession() {
		if (session==null)
			return;
		try {
			consumer.close();
		}
		catch (Exception e) {
			logger.warn("Ignoring error when trying to close already suspicious consumer",e);
		}
		try {
			session.close();
		}
		catch (Exception e) {
			logger.warn("Ignoring error when trying to close already suspicious session",e);
		}
		session=null;
	}

	private void openSession() throws JMSException {
		session = system.getConnection().createSession(true, Session.SESSION_TRANSACTED);
		destination = session.createQueue(queue);
		consumer = session.createConsumer(destination);
	}


	private void handleMessage(Message message) {
		try {
			logger.debug("Handling {}",message.getJMSMessageID());
			handler.handle(new JmsMessage(message)); 
		}
		catch (Exception e) {
			try {
				String code="TECHERR";
				if (e instanceof FunctionalException)
					code="FUNCERR";
				logger.error(code+": "+e.getMessage()+". When handling JMS message "+((TextMessage) message).getText(),e);
				String queue=errorqueue;
				if (e instanceof RetryableException)
					queue=retryqueue;
				Destination errordestination = session.createQueue(queue+system.sendParams);
				MessageProducer producer = session.createProducer(errordestination);
				Message errmsg=JmsUtil.cloneMessage(session, message);
				producer.send(errmsg);

				producer.close();
				logger.info("message send to queue {}",queue);
			}
			catch (JMSException e2) {throw new RuntimeException(e2); }
		}
		finally {
			if (message!=null)
				try {
					logger.debug("committing session with message {}", message.getJMSMessageID());
					session.commit();
				} catch (JMSException e) { throw new RuntimeException(e); }
		}
	}

}

