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
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.jms.JmsSystem;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendMessageAction  implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SendMessageAction.class);

	private final GftContainer gft;
	public final Props props;
	private final JmsSystem qmgr;
	private final String queue;
	private final String templateName;
	private final boolean safeToRetry;
	
	public SendMessageAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		this.qmgr=gft.getQueueSystem();
		this.queue=props.getString("queue");
		this.templateName=props.getString("template");
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		logger.info("Sending message to queue {}",queue);
		
		String body=gft.processTemplate(templateName, ft.getActionContext(this));
		qmgr.getQueue(queue).send(body);
		return null;
	}
}
