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
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FoundFileTask;
import org.kisst.gft.task.Task;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MultiListener;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessageFromFileAction extends HttpCaller implements Action {

    private final static Logger logger = LoggerFactory.getLogger(SendMessageFromFileAction.class);

    private final JmsSystem qmgr;
    private final String queueName;
    private final String basePath;

    public SendMessageFromFileAction(GftContainer gft, Props props) {
        super(gft, props);
        
        basePath = TemplateUtil.processTemplate(props.getString("moveToDirectory"),gft.getContext());
        
        String queueSystemName = TemplateUtil.processTemplate(props.getString("queueSystem", "main"), gft.getContext());

        this.qmgr = gft.getQueueSystem(queueSystemName);

        String firstListenerQueue = null;
        for (MultiListener l : gft.listeners.values()) {
            firstListenerQueue = l.getQueue();
            break;
        }
        this.queueName = props.getString("queue", firstListenerQueue);
        if (queueName == null)
            throw new RuntimeException("No queue defined for action " + props.getLocalName());
    }

    public boolean safeToRetry() {
        return false;
    }

    public Object execute(Task task) {
        FoundFileTask foundFileTask = (FoundFileTask) task;
        String filename = basePath + "/" + foundFileTask.filename;
        FileServerConnection fsconn = foundFileTask.fsconn;
        String content = fsconn.getFileContentAsString(filename);
        sendMessage(content);
        fsconn.deleteFile(filename);
        return null;
    }

    public void sendMessage(String content) {
        Session session = null;
        try {
            session = qmgr.getConnection().createSession(true, Session.SESSION_TRANSACTED);

            Destination destination = session.createQueue(queueName + qmgr.sendParams);
            MessageProducer producer = session.createProducer(destination);
            logger.info("Sending message to queue {}", queueName);
            TextMessage message = session.createTextMessage();
            message.setText(content);
            producer.send(message);
            session.commit();
            logger.info("verzonden bericht \n {}", content);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
