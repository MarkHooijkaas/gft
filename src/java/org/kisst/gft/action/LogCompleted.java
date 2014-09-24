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
import org.kisst.gft.LogService;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;


public class LogCompleted implements Action {

	public LogCompleted(GftContainer gft, Props props) {}

	public boolean safeToRetry() { return true; }


	public Object execute(Task t) {
		String details="";
		if (t instanceof FileTransferTask) {
			FileTransferTask ft = (FileTransferTask) t;
			details= "GFT geslaagd, kanaal: "+ft.channel.name+", bestand: "+ft.srcpath+ ", van: "+ft.channel.src+"/"+ft.srcpath+" naar: "+ft.channel.dest+"/"+ft.destpath;
		}
		LogService.log("info", "done", t.getTaskDefinition().getName(), "completed",details); 
				
		return null;
	}
}
