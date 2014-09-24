package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;

public class DeleteSourceFile implements Action {
	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		SourceFile src= (SourceFile) task;
		src.getSourceFileServer().deleteFile(src.getSourceFilePath());
		return null;
	}

}
