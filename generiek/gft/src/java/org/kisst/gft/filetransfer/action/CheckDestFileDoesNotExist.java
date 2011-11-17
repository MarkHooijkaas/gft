package org.kisst.gft.filetransfer.action;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.action.Action;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;

public class CheckDestFileDoesNotExist implements Action {
	public class Problem extends FunctionalException {
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
