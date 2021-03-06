package org.kisst.jms;

import java.util.Enumeration;

import javax.jms.*;

import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsUtil {
	private final static Logger logger=LoggerFactory.getLogger(JmsUtil.class); 

	public static Message cloneMessage(Session session, Message src ) {
		try {
			Message dest;
			if( src instanceof BytesMessage ) {
				dest = session.createBytesMessage();
				int len = (int) ((BytesMessage) src ).getBodyLength();
				byte[] msg = new byte[ len ];
				( (BytesMessage) src ).readBytes( msg );
				( (BytesMessage) dest ).writeBytes( msg );

			} 
			else if( src instanceof TextMessage ) {
				dest = session.createTextMessage();
				( (TextMessage) dest).setText( ( (TextMessage) src).getText() );
			}
			else if( src.getClass().getName().equals("com.ibm.jms.JMSNullMessage") ) {
				dest = session.createTextMessage();
				( (TextMessage) dest).setText( "");
			}
			else  
				throw new RuntimeException( "Unsupported message format: "+ src.getClass().getName() );

			if( src.getJMSMessageID() != null ) dest.setJMSMessageID( src.getJMSMessageID() );
			if( src.getJMSCorrelationID() != null ) dest.setJMSCorrelationID( src.getJMSCorrelationID() );
			if( src.getJMSReplyTo() != null ) dest.setJMSReplyTo( src.getJMSReplyTo() );
			if( src.getJMSType() != null ) 	dest.setJMSType( src.getJMSType() );
			dest.setJMSDeliveryMode( src.getJMSDeliveryMode() );
			dest.setJMSExpiration( src.getJMSExpiration() );
			dest.setJMSPriority( src.getJMSPriority() );
			dest.setJMSRedelivered( src.getJMSRedelivered() );
			dest.setJMSTimestamp( src.getJMSTimestamp() );

			Enumeration<?> properties = src.getPropertyNames();
			while( properties.hasMoreElements() ) {
				String key = (String) properties.nextElement();
				if( key.startsWith( "JMSX" ) ) continue;

				// don't clone the JMS_IBM_Character_Set property since this won't work in MQ7.5
				// Because we get a DetailedJMSException: JMSCMQ1006: The value for 'JMS_IBM_Character_Set':'IBM01140' is not valid.
				// see http://pic.dhe.ibm.com/infocenter/wmqv7/v7r5/index.jsp?topic=%2Fcom.ibm.mq.mig.doc%2Fq001810_.htm
				if (key.startsWith("JMS_IBM_Character_Set")) continue;
				try {
					dest.setObjectProperty( key, src.getObjectProperty( key ) );
				}
				catch (Exception e2) { logger.warn("could not set JMS property ["+key+"] to value ["+src.getObjectProperty(key)+"]",e2); }

			} 
			return dest;
		}
		catch (JMSException e) { throw wrapJMSException(e);}
	}
	
	public static RuntimeException wrapJMSException(JMSException e) {
		Exception linked = e.getLinkedException();
		if (linked!=null && linked!=e)
			return new RuntimeException(e.getMessage()+"\nLinkedException: "+linked.getMessage(),e);
		return new RuntimeException(e);
	}

	public static void prepareDestinationForJmsHeaders(Destination dest) {
		if (! dest.getClass().getName().equals("com.ibm.mq.jms.MQQueue"))
			return;
		try {
			ReflectionUtil.invoke(dest, "setTargetClient", new Object[]{0});
		}
		catch (Exception e) { /* ignore */ }
	}
}
