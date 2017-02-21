package org.kisst.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class JmsMessage {
	private final Message msg;
	public JmsMessage(Message msg) { this.msg=msg; } 
	public void lock() {}
	public void done() { 
		try {
			msg.acknowledge();
		}
		catch (JMSException e) { throw JmsUtil.wrapJMSException(e); } 
	}
	public String getData() {
		try {
			return ((TextMessage) msg).getText();
		}
		catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
	}
	public String getReplyTo() { 
		try {
			Destination dest = msg.getJMSReplyTo();
			if (dest==null)
				return null;
			else
				return msg.getJMSReplyTo().toString();
		}
		catch (JMSException e) { throw JmsUtil.wrapJMSException(e); } 
	}
	public String getMessageId() { 
		try {
			return msg.getJMSMessageID();
		}
		catch (JMSException e) { throw JmsUtil.wrapJMSException(e); } 
	}
	public String getCorrelationId() { 
		try {
			return msg.getJMSCorrelationID();
		}
		catch (JMSException e) { throw JmsUtil.wrapJMSException(e); } 
	}
}
