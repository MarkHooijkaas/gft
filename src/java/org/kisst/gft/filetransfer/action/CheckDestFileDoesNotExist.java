package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.exception.BasicFunctionalException;

public class CheckDestFileDoesNotExist extends BaseAction {
	public CheckDestFileDoesNotExist(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }

	public class Problem extends BasicFunctionalException {
		private static final long serialVersionUID = 1L;
		public Problem(FileServer fileServer, String path) { super("On host "+fileServer+" there already is a file "+path); }
	}

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		if (ft.channel.dest.fileExists(ft.destpath))
				throw new Problem(ft.channel.dest, ft.destpath);
		return null;
	}

}
