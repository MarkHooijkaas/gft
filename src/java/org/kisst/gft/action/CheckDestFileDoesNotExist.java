package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;

public class CheckDestFileDoesNotExist implements Action {
	public class Problem extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public Problem(SshHost host, String path) { super("On host "+host.host+" there already is a file "+path); }
	}
	private final SshHost host;
	private final String directory;
	
	public CheckDestFileDoesNotExist(GftContainer gft, Props props) {
		this.host=gft.sshhosts.get(props.getString("dest.host"));
		this.directory=props.getString("dest.dir", "");
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		if (host.fileExists(directory, ft.bestand))
				throw new Problem(host, directory+"/"+ft.bestand);
		return null;
	}

}
