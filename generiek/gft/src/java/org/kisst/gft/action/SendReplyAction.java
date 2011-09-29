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

import nl.duo.gft.odwek.OnDemandChannel;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferChannel;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendReplyAction  implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SendReplyAction.class);

	private final GftContainer gft;
	public final Props props;
	private final boolean safeToRetry;
	
	public SendReplyAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		String queue=ft.replyTo;
		if (queue==null)
			throw new RuntimeException("No replyTo address given for task "+ft);
		if (logger.isInfoEnabled())
			logger.info("Sending reply with correlationId {} to queue {}",ft.correlationId, queue);
		
		XmlNode msg=ft.message.clone();
		if (ft.channel instanceof FileTransferChannel)
			msg.getChild("Body/transferFile").element.setName("transferFileResponse");
		else if (ft.channel instanceof OnDemandChannel)
			msg.getChild("Body/archiveerBestand").element.setName("archiveerBestandResponse");
		else 
			throw new RuntimeException("channel "+ft.channel.name+" is of unknown type "+ft.channel.getClass());
		
		String body=msg.toString();
		gft.getQueueSystem().getQueue(queue).send(body, null, ft.correlationId);
		return null;
	}
}
