package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.exception.BasicFunctionalException;

public class CheckDestFileDoesNotExist extends BaseAction {
	public CheckDestFileDoesNotExist(Props props) { super(props); }

	public class Problem extends BasicFunctionalException {
		private static final long serialVersionUID = 1L;
		public Problem(FileLocation loc) { super("On host "+loc.getFileServer()+" there already is a file "+loc.getPath()); }
	}

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		FileLocation dest = ft.getDestinationFile();
		FileLocation finaldest = ft.getFinalDestinationFile();
		if (dest.fileExists())
			throw new Problem(dest);
		if (finaldest!=null && finaldest.fileExists())
			throw new Problem(finaldest);
		return null;
	}

}
