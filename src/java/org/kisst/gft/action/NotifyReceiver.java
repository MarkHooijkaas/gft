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

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;
import org.kisst.util.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NotifyReceiver  implements Action {
	private final static Logger logger=LoggerFactory.getLogger(NotifyReceiver.class);

	private final String queue;
	
	public NotifyReceiver(GftContainer gft, Props props) {
		this.queue=props.getString("queue");
	}
        
	public Object execute(Task t) {
		FileTransferData ftdata = (FileTransferData) t.getData();
		XmlNode msg=ftdata.message.clone();
		msg.getChild("Body/transferFile").element.setName("transferFileNotification");
		
		logger.info("Sending message to queue {}",queue);
		
		String body=msg.toString();
		ftdata.channel.gft.getQueueSystem().getQueue(queue).send(body);
		return null;
	}
}
