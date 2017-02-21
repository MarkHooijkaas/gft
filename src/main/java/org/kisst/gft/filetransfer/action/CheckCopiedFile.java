package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class CheckCopiedFile extends BaseAction {
	public CheckCopiedFile(Props props) { super(props); }

	@Override public boolean safeToRetry() { return true; }

	@Override public void execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		// TODO: remember filesize
		if (! ft.getDestinationFile().fileExists())
				throw new RuntimeException("Copied file "+ft.getDestinationFile()+" does not seem to exist");
	}
}
