package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.exception.BasicFunctionalException;

public class CheckSourceFile extends BaseAction {
	public CheckSourceFile(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		SourceFile src= (SourceFile) task;
		// TODO: remember filesize
		if (! src.getSourceFileServer().fileExists(src.getSourceFilePath()))
				throw new BasicFunctionalException("Source file "+src.getSourceFilePath()+" does not exist or is not accessible");
		return null;
	}

}
