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
import org.kisst.props4j.Props;


public class HttpAction extends HttpCaller implements Action {
	private final String templateName;
	private final boolean safeToRetry;

	public HttpAction(GftContainer gft, Props props) {
		super(gft,props);
		templateName=props.getString("template",null);
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	@Override public boolean safeToRetry() { return safeToRetry; }

	@Override public void execute(Task task) {
		BasicTask basictask= (BasicTask) task;
		String body= gft.processTemplate(templateName, basictask.getActionContext(this));
		httpCall(body); // TODO: do something with response?????
	}
}
