package org.kisst.gft.mq.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.QueueListener;

public class JmsListener implements Runnable, QueueListener {
	
	private final JmsSystem system;
	private final Props props;
	private final String queue;
	private final String errorqueue;
	private boolean running=false;
	private MessageHandler handler=null;
	
	public JmsListener(JmsSystem system, Props props) {
		this.system=system;
		this.props=props;
		this.queue=props.getString("queue");
		this.errorqueue=props.getString("errorqueue");
	}
	
	public void stop() { running=false; }
	public void run()  {
		long interval=props.getLong("interval",5000);
		try {
			Session session = system.getConnection().createSession(true, Session.SESSION_TRANSACTED);
			System.out.println("Opening queue "+queue);

			Destination destination = session.createQueue(queue);
			MessageConsumer consumer = session.createConsumer(destination);
			running=true;
			while (running) {
				Message message = consumer.receive(interval);
				try {
					if (message!=null)
						handler.handle(new JmsQueue.JmsMessage(message));
					session.commit();
				}
				catch (Exception e) {
					e.printStackTrace();
					Destination errordestination = session.createQueue(errorqueue);
					MessageProducer producer = session.createProducer(errordestination);
					producer.send(message);
					session.commit();
					producer.close();
				}
			}
			consumer.close();
			session.close();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}



	public void stopListening() { running=false; }
	public void listen(MessageHandler handler) {
		this.handler=handler;
		running=true;
		new Thread(this).start();
	}
}
