package org.kisst.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kisst.cfg4j.Props;

public class JmsQueue {
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
