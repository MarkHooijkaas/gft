package org.kisst.gft.mq.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kisst.cfg4j.Props;
import org.kisst.gft.RetryableException;
import org.kisst.gft.admin.rest.Representable;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.QueueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsListener implements Runnable, QueueListener, Representable {

	private final static Logger logger=LoggerFactory.getLogger(JmsListener.class); 
	
	private final JmsSystem system;
	private final Props props;
	private final String queue;
	private final String errorqueue;
	private final String retryqueue;
	private final int receiveErrorRetries;
	private final int receiveErrorRetryDelay;
	
	private boolean running=false;
	private MessageHandler handler=null;
	private Thread thread=null;
	private final ExecutorService pool;
	
	public JmsListener(JmsSystem system, Props props) {
		this.system=system;
		this.props=props;
		this.queue=props.getString("queue");
		this.errorqueue=props.getString("errorqueue");
		this.retryqueue=props.getString("retryqueue");
		this.receiveErrorRetries = props.getInt("receiveErrorRetries", 1000);
		this.receiveErrorRetryDelay = props.getInt("receiveErrorRetryDelay", 60000);
		this.pool = Executors.newFixedThreadPool(props.getInt("threadPoolSize",10));
	}
	
	public String toString() { return "JmsListener("+queue+")"; }
	public String getRepresentation() { return props.toString(); }
	public void stop() {
		logger.info("Stopping listening to queue {}", queue);
		running=false;
	}
	public void run()  {
		if (thread!=null)
			throw new RuntimeException("Listener already running");
		thread=Thread.currentThread();
		running=true;
		try {
			listen();
		}
		catch (JMSException e) {
			logger.error("Unrecoverable error during listening, stopped listening", e);
			if (props.getBoolean("exitOnUnrecoverableListenerError", false))
				System.exit(1);
		}
		finally {
			thread=null;
			running=false;
			closeSession();
		}
	}

	private Session session = null;
	private Destination destination = null;
	private MessageConsumer consumer = null;

	private void listen() throws JMSException {
		logger.info("Opening queue {}",queue);
		while (running) {
			Message message=null;
			message = getMessage();
			if (message!=null) {
				handleMessage(session, message);
			}
		}
		logger.info("Stopped listening to queue {}", queue);
	}

	private Message getMessage() throws JMSException {
		long interval=props.getLong("interval",5000);
		if (session==null)
			openSession();
		int retryCount=0;
		try {
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

	private void handleMessage(final Session session, final Message message) throws JMSException {
		pool.execute(new MyMessageHandler(message));
		session.commit();
	}
	private final class MyMessageHandler implements Runnable {
		private final Message message;
		private MyMessageHandler(Message message) {	this.message = message;	}
		public void run() {
			try {
				handler.handle(new JmsQueue.JmsMessage(message)); 
			}
			catch (Exception e) {
				try {
					logger.error("Error when handling JMS message "+((TextMessage) message).getText(),e);
					String queue=errorqueue;
					if (e instanceof RetryableException)
						queue=retryqueue;
					Destination errordestination = session.createQueue(queue);
					MessageProducer producer = session.createProducer(errordestination);
					producer.send(message);
					session.commit();
					producer.close();
					logger.info("message send to queue {}",queue);
				}
				catch (JMSException e2) {throw new RuntimeException(e2); }
			}
		}
	}


	public boolean listening() { return thread!=null; }
	public void stopListening() { running=false; shutdownAndAwaitTermination(); } // TODO: shutdown might be separated from stopListening
	public void listen(MessageHandler handler) {
		this.handler=handler;
		running=true;
		new Thread(this).start();
	}
	
	void shutdownAndAwaitTermination() {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					logger.error("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
}
