package org.kisst.gft.action;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.ssh.SshHost;
import org.kisst.gft.task.Task;

public class CheckDestFileDoesNotExist implements Action {
	public class Problem extends FunctionalException {
		private static final long serialVersionUID = 1L;
		public Problem(SshHost host, String path) { super("On host "+host.host+" there already is a file "+path); }
	}

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		if (ft.channel.dest.fileExists(ft.destpath))
				throw new Problem(ft.channel.dest, ft.destpath);
		return null;
	}

}
