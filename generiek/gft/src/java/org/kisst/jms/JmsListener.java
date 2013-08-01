package org.kisst.jms;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kisst.gft.DetailedException;
import org.kisst.gft.FunctionalException;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.kisst.util.TimeWindowList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsListener implements Runnable {
	private final static Logger logger=LoggerFactory.getLogger(JmsListener.class); 

	private final JmsSystem system;
	private final MessageHandler handler;
	private final Props props;
	public final String queue;
	public final String errorqueue;
	//public final String retryqueue;
	private final int receiveErrorRetries;
	private final int receiveErrorRetryDelay;
	private final long interval;
	private final TimeWindowList forbiddenTimes;

	private Session session = null;
	private Queue destination = null;
	private MessageConsumer consumer = null;
	private boolean browseMode=true; 
	private boolean running=false;
	private String messageId=null;


	Thread thread;


	public JmsListener(JmsSystem system, MessageHandler handler, Props props, Object context) {
		this.system=system;
		this.props=props;
		this.handler=handler;
		this.interval=props.getLong("interval",5000);

		this.queue=TemplateUtil.processTemplate(props.getString("queue"), context);
		this.errorqueue=TemplateUtil.processTemplate(props.getString("errorqueue"), context);
		//this.retryqueue=TemplateUtil.processTemplate(props.getString("retryqueue"), context);
		this.receiveErrorRetries = props.getInt("receiveErrorRetries", 1000);
		this.receiveErrorRetryDelay = props.getInt("receiveErrorRetryDelay", 60000);
		String timewindow=props.getString("forbiddenTimes", null);
		if (timewindow==null)
			this.forbiddenTimes=null;
		else
			this.forbiddenTimes=new TimeWindowList(timewindow);
	}

	public String getStatus() {
		if (messageId!=null)
			return "WORKING "+messageId;
		if (! running)
			return "STOPPED";
		if (browseMode)
			return "PAUZED";
		return "LISTENING";
	}
	public boolean isActive() { return running && ! browseMode; }
	
	public String toString() { return "JmsListener("+queue+")"; }

	public boolean isForbiddenTime() {
		return forbiddenTimes!=null && forbiddenTimes.isTimeInWindow();
	} 

	public synchronized void start() {
		logger.info("Starting Listener {}",this);
		if (thread!=null)
			logger.warn("Listener already started");
		else {
			running=true;
			thread=new Thread(this);
			thread.start();
		}
	}
	public void stop() { 
		logger.info("Stopping Listener {}",this);
		running=false;
		Thread t=thread;
		if (t!=null) {
			try {
				t.join();
			} catch (InterruptedException e) { throw new RuntimeException(e);}
			logger.info("Stopped Listener {}",this);
		}
	}

	public void run() {
		// Always start in browse mode, to check for stop messages
		// If one would receive the stop message, other threads/machines might temporarily 
		// not see it, and pick-up the next message
		logger.info("Started Listener on queue {}",queue);
		enterBrowseMode(); 
		try {
			while (running) {
				Message message=null;
				message = getMessage();
				if (message!=null) {
					try { 
						this.messageId=message.getJMSMessageID();
						if (logger.isDebugEnabled())
							logger.debug("handling message {}",message.getJMSMessageID());
						handleMessage(message);
					}
					finally {
						this.messageId=null;
					}
				}
			}
		}
		catch (Throwable e) { // DLL link errors
			logger.error("Unrecoverable error during listening, stopped listening", e);
			if (props.getBoolean("exitOnUnrecoverableListenerError", false))
				System.exit(1);
		}
		finally {
			thread=null;
			logger.info("Stopped listening to queue {}", queue);
			closeSession();
		}
	}

	private Message getMessage() throws JMSException {
		if (isForbiddenTime()) {
			logger.debug("Sleeping for {} millisecs because of forbiddenTime", interval);
			sleepSomeTime(interval);
			return null;
		}
		int retryCount=0;
		try {
			if (checkBrowseMode())
				return null;
			openConsumer();
			Message message = consumer.receive(interval);
			if (message!=null) {
				if (ControlMessage.isStopMessage(message)) {
					logger.info("Received a stop message on queue {}, rolling back the stop message",queue);
					session.rollback(); // put the message back on the queue
					closeSession(); // recover the session so it will see the stop message
					enterBrowseMode();
					return null;
				}
				else if (ControlMessage.isStartMessage(message)) {
					logger.info("Ignoring a received a start message on queue {}, because is already started",queue);
					session.commit(); // remove the start message
					return null;
				}
				return message;
			}
			retryCount=0;
		}
		catch (Exception e) {
			logger.error("Error when receiving JMS message on queue "+queue, e);
			if (retryCount++ > receiveErrorRetries)
				throw new RuntimeException("too many receive retries for queue "+queue);
			closeSession();
			logger.info("sleeping for "+receiveErrorRetryDelay+" milliseconds for retrying listening to "+queue);
			sleepSomeTime(receiveErrorRetryDelay);
		}
		return null;
	}

	private void enterBrowseMode() {
		logger.info("Entering browse mode on queue {}",queue);
		browseMode=true;
	}
	/**
	 * Will browse the top of the queue for starting or stopping messages 
	 * If multiple starting/stopping message are available all but the last will be removed
	 * If the last message is a start message it will be removed as well
	 */
	private boolean checkBrowseMode() {
		if (! browseMode)
			return false;
		QueueBrowser browser=null;
		ArrayList<String> removeList=new ArrayList<String>();
		try {
			openSession();
			browser = session.createBrowser(destination);

			logger.debug("browsing for a message");
			Enumeration<?> e = browser.getEnumeration();
			Message lastMessage=null;
			while (e.hasMoreElements()) {
				Message msg = (Message) e.nextElement();
				if (logger.isDebugEnabled())
					logger.debug("browsing message with id {}, content {}",msg.getJMSMessageID(),((TextMessage) msg).getText());
				if (ControlMessage.isStopMessage(msg) || ControlMessage.isStartMessage(msg)) {
					if (lastMessage!=null) {
						logger.info("Removing control message from queue {} because there is a newer control messages",queue);
						removeList.add(lastMessage.getJMSMessageID());
					}
					lastMessage=msg;
				}
				else 
					break;
			}
			if (lastMessage==null) // there were no control message on queue, no reason to stay in browsemode
				browseMode=false;
			else if (ControlMessage.isStopMessage(lastMessage)) {
				sleepSomeTime(this.interval);
			}
			else if (ControlMessage.isStartMessage(lastMessage)) { // startMessage may be removed
				logger.info("Removing start message from queue {} because it is the last control message",queue);
				removeList.add(lastMessage.getJMSMessageID());
				browseMode=false;
			}
			closeSession();
			if (!removeMessages(removeList)) {
				logger.warn("Could not remove some messages, so staying in browse mode",queue);
				browseMode=true;
			}
			if (! browseMode)
				logger.info("Exiting browse mode on queue {}",queue);
			return browseMode;
		}
		catch (JMSException e) { throw new RuntimeException(e);}
		finally {
			try {
				if (browser!=null)
					browser.close();
			} catch (JMSException e) { throw new RuntimeException(e); }
		}
	}

	private boolean removeMessages(ArrayList<String> removeList) {
		try {
			closeSession();
			openSession();
			//consumer.close();
			for (String msgid:removeList) {
				String selector = "JMSMessageID='" +msgid+  "'";
				logger.debug("Removing message [{}] from queue {} ",selector, queue);
				MessageConsumer cons2=null;
				try {
					cons2 = session.createConsumer(destination, selector);
					Message msg = cons2.receive(2000);
					if (msg==null) {
						logger.warn("Could not find for removal a message with id {} ",msgid);
						return false;
					}
					if (logger.isInfoEnabled()) {
						String body = ((TextMessage)msg).getText();
						logger.debug("Removing message [{}] from queue {} with content: "+body,selector, queue);
					}
					session.commit();
				}
				finally {
					if (cons2!=null)
						cons2.close();
				}
			}
			closeSession();
		}
		catch (JMSException e) { throw new RuntimeException(e);}
		return true;
	}
	
	

	private void sleepSomeTime(long delay) {
		try {
			Thread.sleep(delay);
		}
		catch (InterruptedException e1) { throw new RuntimeException(e1); }
	}

	private void closeSession() {
		if (session==null)
			return;
		try {
			if (consumer!=null)
				consumer.close();
		}
		catch (Exception e) {
			logger.warn("Ignoring error when trying to close already suspicious consumer",e);
		}
		consumer=null;
		try {
			session.close();
		}
		catch (Exception e) {
			logger.warn("Ignoring error when trying to close already suspicious session",e);
		}
		session=null;
	}
	private void openSession() throws JMSException {
		if (session!=null)
			return;
		session = system.getConnection().createSession(true, Session.SESSION_TRANSACTED);
		destination = session.createQueue(queue);
		consumer=null;
	}
	private void openConsumer() throws JMSException {
		if (consumer!=null)
			return;
		openSession();
		consumer = session.createConsumer(destination);
	}


	private void handleMessage(Message message) {
		boolean messageHandled = false;
		try {
			logger.debug("Handling {}",message.getJMSMessageID());
			handler.handle(new JmsMessage(message)); 
			messageHandled = true;
		}
		catch (Exception e) {
			try {
				String code="TECHERR";
				if (e instanceof FunctionalException)
					code="FUNCERR";
				String text=null;
				if (message instanceof TextMessage)
					text=code+": "+e.getMessage()+". When handling JMS message "+((TextMessage) message).getText();
				else 
					text=code+": "+e.getMessage()+". When handling JMS message of type "+ message.getClass();
				if (e instanceof DetailedException)
					text+=((DetailedException)e).getDetails();
				
				logger.error(text,e);
				String queue=errorqueue;
				//if (e instanceof RetryableException)
				//	queue=retryqueue;
				Destination errordestination = session.createQueue(queue+system.sendParams);
				MessageProducer producer = session.createProducer(errordestination);
				Message errmsg=JmsUtil.cloneMessage(session, message);
				producer.send(errmsg);

				producer.close();
				logger.info("message send to queue {}",queue);
				messageHandled=true;
			}
			catch (JMSException e2) {throw new RuntimeException(e2); }
		}
		finally {
			// The check for messageHandled is necessary because a Throwable error will not put the message on the error queue
			// so the message should not be committed in this case.
			if (message!=null && messageHandled)
				try {
					logger.debug("committing session with message {}", message.getJMSMessageID());
					session.commit();
				} catch (JMSException e) { throw new RuntimeException(e); }
		}
	}

}

