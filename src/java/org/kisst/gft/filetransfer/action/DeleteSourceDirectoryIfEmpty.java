package org.kisst.gft.filetransfer.action;

import java.util.LinkedHashMap;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FileServerConnection.FileAttributes;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class DeleteSourceDirectoryIfEmpty extends BaseAction {
	public DeleteSourceDirectoryIfEmpty(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileLocation file = ((SourceFile) task).getSourceFile();
		FileLocation dir = file.getParentDirectory();
		FileServerConnection fsconn = dir.getFileServer().openConnection();
		LinkedHashMap<String, FileAttributes> entries = fsconn.getDirectoryEntries(dir.getPath());
		int count=0;
		for (String entry : entries.keySet()) {
			if (entry.equals(".") || entry.equals(".."))
				continue;
			count ++;
		}
		if (count==0)
			fsconn.deleteFile(dir.getPath());
		// TODO: throw an error if directory is not empty????
		return null;
	}

}
