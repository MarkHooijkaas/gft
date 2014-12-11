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

package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpPutAction extends BaseAction {
	public SftpPutAction(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }

	private final static Logger logger=LoggerFactory.getLogger(SftpPutAction.class);
	
	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		
		FileServer fileserver= ft.getDestinationFile().getFileServer();
		FileServerConnection fsconn=fileserver.openConnection();
		try {
			String remotefile = ft.getDestinationFile().getPath();
			String localfile=ft.getTempFile().getPath();
			logger.info("sftp put localfile {} to {}", localfile, remotefile);
			fsconn.putFromLocalFile(localfile, remotefile);
		}
		finally {
			if (fsconn!=null)
				fsconn.close();
		}

		return null;
	}

}
