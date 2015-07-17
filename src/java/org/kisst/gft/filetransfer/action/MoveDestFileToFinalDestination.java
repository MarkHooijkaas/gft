package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.exception.BasicFunctionalException;

public class MoveDestFileToFinalDestination extends BaseAction {
	public MoveDestFileToFinalDestination(Props props) { super(props); }

	public class Problem extends BasicFunctionalException {
		private static final long serialVersionUID = 1L;
		public Problem(FileLocation loc) { super("On host "+loc.getFileServer()+" there already is a file "+loc.getPath()); }
	}

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		DestinationFile desttask= (DestinationFile) task;
		FileLocation dest = desttask.getDestinationFile();
		FileLocation finaldest = desttask.getFinalDestinationFile();
		if (finaldest==null || finaldest.getPath().equals(dest.getPath()))
			return null; // nothing to do
		// TODO: check if fileservers are the same
		FileServerConnection fsconn=dest.getFileServer().openConnection();
		try {
			fsconn.move(dest.getPath(), finaldest.getPath());
		}
		finally {
			if (fsconn!=null)
				fsconn.close();
		}
		return null;
	}

}
