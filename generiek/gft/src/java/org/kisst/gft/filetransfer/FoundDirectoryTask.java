package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.TaskDefinition;

public class FoundDirectoryTask extends BasicTask {
	public final FileServerConnection fsconn;
	public final String filename;
	
	public FoundDirectoryTask(GftContainer gft, TaskDefinition taskdef, FileServerConnection fsconn, String filename) {
		super(gft, taskdef);
		this.fsconn=fsconn;
		this.filename = filename;
	}

}
