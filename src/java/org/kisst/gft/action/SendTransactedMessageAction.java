/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kisst.gft.action;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FoundFileTask;
import org.kisst.gft.task.Task;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.JmsUtil;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is a base class for sending messages transactionally, specifically from the PollerJob, using a FoundFileTask.
 * The Pollerjob/FoundFileTask will use the following sequence:
 * 	1. call prepareTransaction
 * 	2. move the found file to a in-progress directory (this is what the pollerjob does)
 * 	3. call the regular Action.execute
 * 	4. call the commitTransaction or rollbackTransaction depending if an error occurred or not
 * 
 * Typically one wants to override the prepareTransaction, to already send a message, but not commit this, to detect if 
 * message can be sent correctly.
 * In this case the execute could be overridden to do nothing.
 * The default implementation of execute is to do all steps, but with a safeguard that if transaction is already running, 
 * or message is already sent it will not start over again.
 *
 */
public abstract class SendTransactedMessageAction implements Action, Transaction {

    private static final String VAR_JMS_SESSION = "_jms_session";
    private static final String VAR_MESSAGE_SENT = "_message_sent";

	private final static Logger logger = LoggerFactory.getLogger(SendTransactedMessageAction.class);

    private final JmsSystem qmgr;
    private final String queueName;

    public SendTransactedMessageAction(GftContainer gft, Props props) {
        String queueSystemName = TemplateUtil.processTemplate(props.getString("queueSystem", "main"), gft.getContext());
        this.qmgr = gft.getQueueSystem(queueSystemName);

        this.queueName = props.getString("queue", gft.getMainQueue());
        if (queueName == null)
            throw new RuntimeException("No queue defined for action " + props.getLocalName());
    }

    abstract String getMessageContent(Task task);
    
    @Override public boolean safeToRetry() { return false; }

    private Session getSession(Task task) { return (Session) task.getVar(VAR_JMS_SESSION); }
    
	@Override public void prepareTransaction(Task task) {
        try {
        	Session session = qmgr.getConnection().createSession(true, Session.SESSION_TRANSACTED);
        	task.setVar(VAR_JMS_SESSION, session);
        }
        catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
	}
	@Override public void execute(Task task) {
    	FoundFileTask fftask=(FoundFileTask) task;
    	boolean succesfull=false;
		try {
	    	if (getSession(fftask)==null)
	    		prepareTransaction(fftask); // the session is not yet prepared
    		sendMessage(fftask);
        	succesfull=true;
    	}
    	finally {
    		if (succesfull)
    			commitTransaction(fftask);
    		else
    			rollbackTransaction(fftask);
    	}
    }

    @Override public void commitTransaction(Task task) {
        try {
        	Session sess=getSession(task);
        	if (sess!=null) {
        		sess.commit();
        		closeSession(task);
        	}
        }
        catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
	}
	
    @Override public void rollbackTransaction(Task task) {
        try {
        	Session sess=getSession(task);
        	if (sess!=null) {
        		sess.rollback();
        		closeSession(task);
        	}
        }
        catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
	}
	
	private void closeSession(Task task) {
        Session session = getSession(task);
        try {
            if (session != null) {
                session.close();
        		task.setVar(VAR_JMS_SESSION, null);
            }
        }
        catch (JMSException e) { throw JmsUtil.wrapJMSException(e); } 
	}
	
    public void sendMessage(Task task) {
        if ("true".equals(task.getVar(VAR_MESSAGE_SENT)))
        	return;

        Session session = getSession(task);
        try {
            Destination destination = session.createQueue(queueName + qmgr.sendParams);
            MessageProducer producer = session.createProducer(destination);
            String content=getMessageContent(task);
            logger.info("Sending message to queue {}", queueName);
            TextMessage message = session.createTextMessage();
			message.setText(content);
            producer.send(message);
            task.setVar(VAR_MESSAGE_SENT, "true");
            logger.info("verzonden bericht \n {}", content);
        }
        catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
    }
}
