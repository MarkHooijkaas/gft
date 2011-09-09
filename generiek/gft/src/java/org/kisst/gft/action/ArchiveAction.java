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
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ArchiveAction  implements Action {
	private final static Logger logger=LoggerFactory.getLogger(ArchiveAction.class);

	private final boolean safeToRetry;
	
	public ArchiveAction(GftContainer gft, Props props) {
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		if (logger.isInfoEnabled()){
			String melding1 = "de melding1";
			String melding = "de melding";
			logger.info("{} archiveAction is aangeroepen! {}", melding1 , melding);
			logger.info("archiveAction Stap haal op!");
			logger.info("archiveAction Stap Archiveer!");
			logger.info("archiveAction is ruim localfile op!");
		}
		return null;

	}
}
