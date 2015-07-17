package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.exception.BasicFunctionalException;

public class CheckSourceFile extends BaseAction {
	public CheckSourceFile(Props props) { super(props); }

	@Override public boolean safeToRetry() { return true; }

	@Override public void execute(Task task) {
		SourceFile src= (SourceFile) task;
		// TODO: remember filesize
		if (! src.getSourceFile().fileExists())
				throw new BasicFunctionalException("Source file "+src.getSourceFile()+" does not exist or is not accessible");
	}

}
