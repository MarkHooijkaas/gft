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

import org.kisst.gft.filetransfer.action.DestinationFile;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;

public abstract class BaseAction implements Action {
	private final boolean safeToRetry;
	private final BasicTaskDefinition taskdef;
	private final String[] stringFields;
	
	public BaseAction(BasicTaskDefinition taskdef, Props props, String... fields) {
		this.taskdef=taskdef;
		this.safeToRetry = props.getBoolean("safeToRetry", true);
		this.stringFields=fields;
		
	}
	public BasicTaskDefinition getTaskDef() { return taskdef; }
	public boolean safeToRetry() { return safeToRetry; }
	
	protected String[] getStringFields() { return stringFields;}
	@Override public String toString() { return ReflectionUtil.toString(this,getStringFields());}
	
	static protected String getSourceField(BasicTaskDefinition taskdef) {
		if (taskdef instanceof SourceFile) {
			SourceFile src=(SourceFile) taskdef;
			return "from:"+src.getSourceFileServer()+"/"+src.getSourceFilePath();
		}
		return null;
	}

	static protected String getDestField(BasicTaskDefinition taskdef) {
		if (taskdef instanceof DestinationFile) {
			DestinationFile dest=(DestinationFile) taskdef;
			return "to:"+dest.getDestinationFileServer()+"/"+dest.getDestinationFilePath();
		}
		return null;
	}
}
