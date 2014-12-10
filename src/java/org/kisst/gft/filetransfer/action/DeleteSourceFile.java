package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class DeleteSourceFile extends BaseAction {
	public DeleteSourceFile(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		SourceFile src= (SourceFile) task;
		src.getSourceFileServer().deleteFile(src.getSourceFilePath());
		return null;
	}

}
