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
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpGetAction extends BaseAction {
	private final static Logger logger=LoggerFactory.getLogger(SftpGetAction.class);

	public SftpGetAction(BasicTaskDefinition taskdef, Props props) { 
		super(taskdef, props, getSourceField(taskdef));
	}


	public Object execute(Task task) {
		SourceFile src= (SourceFile) task;

		FileServerConnection fsconn=src.getSourceFile().getFileServer().openConnection();
		try {
			String remotefile = src.getSourceFile().getPath();
			String localfile=((BasicTask)task).getTempFile().getPath();
			logger.info("sftp get {} to localfile {}",remotefile, localfile);
			fsconn.getToLocalFile(remotefile, localfile);
		}
		finally {
			if (fsconn!=null)
				fsconn.close();
		}

		return null;
	}

}
