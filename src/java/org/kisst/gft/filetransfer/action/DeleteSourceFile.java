package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class DeleteSourceFile extends BaseAction {
	public DeleteSourceFile(Props props) { super(props); }

	@Override public boolean safeToRetry() { return true; }

	@Override public void execute(Task task) {
		((SourceFile) task).getSourceFile().deleteFile();
	}

}
