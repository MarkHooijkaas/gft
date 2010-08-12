package org.kisst.gft.mq.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.mq.MqQueue;

public class JmsQueue implements MqQueue {
	public static class JmsMessage implements MqMessage {
		private final Message msg;
		public JmsMessage(Message msg) { this.msg=msg; } 
		public void lock() {}
		public void done() { 
			try {
				msg.acknowledge();
			} catch (JMSException e) { throw new RuntimeException(e);}
		}
		public String getData() {
			try {
				return ((TextMessage) msg).getText();
			} catch (JMSException e) { throw new RuntimeException(e); }
		}
		public String getReplyTo() { 
			try {
				Destination dest = msg.getJMSReplyTo();
				if (dest==null)
					return null;
				else
					return msg.getJMSReplyTo().toString();
			} catch (JMSException e) { throw new RuntimeException(e); } 
		}
		public String getMessageId() { 
			try {
				return msg.getJMSMessageID();
			} catch (JMSException e) { throw new RuntimeException(e); } 
		}
		public String getCorrelationId() { 
			try {
				return msg.getJMSCorrelationID();
			} catch (JMSException e) { throw new RuntimeException(e); } 
		}
	}
	
	private final JmsSystem system;
	//private final Props props;
	private final String queue;

	public JmsQueue(JmsSystem system, String name) {
		this.system=system;
		//this.props=props;
		this.queue=name;
	}

	public JmsQueue(JmsSystem system, Props props) {
		this.system=system;
		//this.props=props;
		this.queue=props.getString("name");
	}
	
	public String getName() {return null; }

	public void send(String data) { send(data,null,null); }

	public void send(String data, String replyTo, String correlationId) {
		Session session=null;
		try {
			session = system.getConnection().createSession(true, Session.SESSION_TRANSACTED);

			Destination destination = session.createQueue(queue+system.sendParams);
			MessageProducer producer = session.createProducer(destination);
			TextMessage message = session.createTextMessage();
			message.setText(data);
			if (replyTo!=null)
				message.setJMSReplyTo(session.createQueue(replyTo));
			if (correlationId!=null)
				message.setJMSCorrelationID(correlationId);
			//message.setJMSType("mijntype");
			producer.send(message);
			session.commit();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
		finally {
			try {
				if (session!=null)
					session.close();
			}
			catch (JMSException e) {throw new RuntimeException(e); }
		}
	}
}
