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

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferTask;


public class LogStart  extends LogCompleted {
	public LogStart(GftContainer gft, Props props) {
		super(gft, props);
	}
	
	@Override protected void fillContext(HashMap<String,Object> context, FileTransferTask ft) {
		context.put("details", "Start GFT filetransfer, kanaal: "+ft.channel.name+
				", van: "+ft.channel.src.host+":"+ft.srcpath
				+", naar:"+ft.channel.dest.host+":"+ft.destpath);
		context.put("niveau", "info");
		context.put("event", "started");
		context.put("tech", "start");
	}
}
