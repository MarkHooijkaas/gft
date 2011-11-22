package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.TaskDefinition;

public class FoundDirectoryTask extends BasicTask {
	public final String filename;
	
	public FoundDirectoryTask(GftContainer gft, TaskDefinition taskdef, String filename) {
		super(gft, taskdef);
		this.filename = filename;
	}

}
