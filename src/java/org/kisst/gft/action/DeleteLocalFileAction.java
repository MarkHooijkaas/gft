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

import java.io.File;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteLocalFileAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(DeleteLocalFileAction.class);
	private final boolean safeToRetry;

	
	public DeleteLocalFileAction(GftContainer gft, Props props) {
		// TODO: we willen channel weten
		//Channel chan=null;
		//if (chan instanceof ArchiveerChannel)
		//	throw new RuntimeException("ArchiveAction moet in een ArchiveerChannel zitten, en dat is "+chan.name+" niet");
		safeToRetry = props.getBoolean("safeToRetry", false);
			}

	public boolean safeToRetry() { return safeToRetry; }
        
	public Object execute(Task task) {
		
		BasicTask ft= (BasicTask) task;
	
		logger.info("deleteLocalFileAction is ruim localfile op!");
		
		// Deletes all files and subdirectories under dir.
		// Returns true if all deletions were successful.
		// If a deletion fails, the method stops attempting to delete and returns false.

		boolean gelukt = deleteDir(ft.getTempFile().getParentFile());
		if (gelukt){
			logger.info("verwijderen van directorie {}, inclusief bestanden, is gelukt", ft.getTempFile().getPath());
			}
		else {
			logger.error("verwijderen van directorie {} is niet gelukt", ft.getTempFile().getPath());	
			}
	
		return null;
	}
	
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }
	    // The directory is now empty so delete it
	    return dir.delete();
	}


}
