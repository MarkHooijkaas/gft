package org.kisst.gft.filetransfer.action;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;

public class CheckSourceFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		SourceFile src= (SourceFile) task;
		// TODO: remember filesize
		if (! src.getSourceFileServer().fileExists(src.getSourceFilePath()))
				throw new FunctionalException("Source file "+src.getSourceFilePath()+" does not exist or is not accessible");
		return null;
	}

}
