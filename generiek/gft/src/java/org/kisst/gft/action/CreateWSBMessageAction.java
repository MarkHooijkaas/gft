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

import java.util.HashMap;

import nl.duo.gft.util.PlexUtil;
import nl.duo.wsf.domain.Bericht;
import nl.duo.wsf.mapper.BerichtToPlexHashMapMapper;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.Task;
import org.kisst.jms.JmsSystem;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateWSBMessageAction implements Action {
	private final static String VAR_GASID = "gasid";
	private final static String VAR_BERICHT = "bericht";
	
	private final static Logger logger = LoggerFactory
			.getLogger(CreateWSBMessageAction.class);
	
	private final GftContainer gft;
	private final String templateName;
	private final JmsSystem qmgr;
	private final String queue;

	public CreateWSBMessageAction(GftContainer gft, Props props) {
		this.gft = gft;
		this.templateName = props.getString("template");
		this.qmgr=gft.getQueueSystem();
		this.queue=props.getString("queue");
	}

	@Override
	public Object execute(Task task) {
		FileTransferTask bt = (FileTransferTask) task;
		String gasId = (String)bt.getVar(VAR_GASID);
		Bericht bericht = (Bericht) bt.getVar(VAR_BERICHT);
		HashMap<String, Object> data = BerichtToPlexHashMapMapper.map(bericht, gasId);
		
		PlexUtil plexUtil = new PlexUtil();
		String plexBericht = plexUtil.maakPlexBericht(data);

		String text = gft.processTemplate(templateName, plexBericht);
		qmgr.getQueue(queue).send(text);
		logger.debug("executed CreateWSBMessageAction");
		return null;
	}
	
	@Override
	public boolean safeToRetry() {
		return false;
	}
}
