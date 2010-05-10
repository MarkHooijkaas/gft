package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;

public class CheckDestFileDoesNotExist implements Action {
	public class Problem extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public Problem(SshHost host, String path) { super("On host "+host.host+" there already is a file "+path); }
	}

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		if (ft.channel.dest.fileExists(ft.channel.destdir, ft.destpath))
				throw new Problem(ft.channel.dest, ft.channel.destdir+"/"+ft.destpath);
		return null;
	}

}
