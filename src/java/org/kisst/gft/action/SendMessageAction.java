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
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.mq.QueueSystem;
import org.kisst.gft.task.Task;
import org.kisst.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendMessageAction  implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SendMessageAction.class);

	private final Props actionProps;
	private final QueueSystem qmgr;
	private final String queue;
	private final String template;
	
	public SendMessageAction(GftContainer gft, Props props) {
		this.actionProps=props;
		this.qmgr=gft.queuemngrs.get(props.getString("qmgr"));
		this.queue=props.getString("queue");
		this.template=props.getString("template");
	}
        
	public Object execute(Task t) {
		FileTransferData ftdata = (FileTransferData) t.getData();
		SimpleProps props=new SimpleProps();
		props.put("action", actionProps);
		props.put("file", ftdata.file);
		props.put("channel", ftdata.channel.props);
		logger.info("Sending message to queue {}",queue);
		
		String body=StringUtil.substitute(template, props);
		qmgr.getQueue(queue).send(body);
		return null;
	}
}
