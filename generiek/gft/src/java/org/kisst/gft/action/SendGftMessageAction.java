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

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.Task;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MultiListener;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendGftMessageAction  implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SendGftMessageAction.class);

	private final GftContainer gft;
	public final Props props;
	private final JmsSystem qmgr;
	private final String queue;
	private final boolean safeToRetry;
	private final String template = 
		"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"+
		"	<Header>\n"+
		"		<herkomst xmlns=\"NL:OCW:ALG:SOAP\">\n"+
		"			<systeemcode xmlns=\"NL:OCW:GWB:BASIS\">${action.props.herkomst.systeemcode}</systeemcode>\n"+
		"			<omgevingscode xmlns=\"NL:OCW:GWB:BASIS\">${omgevingscode}</omgevingscode>\n"+
		"		</herkomst>\n"+
		"	</Header>\n"+
		"	<Body>\n"+
		"		<transferFile xmlns=\"gft:filetransfer-1.0\">\n"+
		"			<kanaal>${action.props.kanaal}</kanaal>\n"+
		"			<bestand>${task.filename}</bestand>\n"+
		"		</transferFile>\n"+
		"	</Body>\n"+
		"</Envelope>\n";
	
	public SendGftMessageAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		this.qmgr=gft.getQueueSystem();

		String firstListenerQueue=null;
		for (MultiListener l : gft.listeners.values()) {
			firstListenerQueue=l.getQueue();
			break;
		}
		this.queue=props.getString("queue",firstListenerQueue);
		if (queue==null)
			throw new RuntimeException("No queue defined for action "+props.getLocalName());
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		BasicTask basicTask= (BasicTask) task;
		logger.info("Sending message to queue {}",queue);
		SimpleProps context = basicTask.getActionContext(this);
		String omgevingscode = props.getString("herkomst.omgevingscode", context.getString("global.omgeving"));
		context.put("omgevingscode", omgevingscode);
		logger.debug("context is {}",context);

		String body=gft.processTemplate(template, context);
		qmgr.getQueue(queue).send(body);
		logger.info("verzonden bericht \n {}", body);
		return null;
	}
}
