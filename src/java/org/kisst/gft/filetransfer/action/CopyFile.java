package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class CopyFile extends BaseAction {
	public CopyFile(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		Channel chan=ft.channel;
		SshFileServer src=chan.src;
		SshFileServer dest=chan.dest;
		String mode=chan.mode;
		
		if ("push".equals(mode))
			src.copyFileTo(ft.srcpath, dest, ft.destpath);
		else if ("pull".equals(mode))
			dest.copyFileFrom(src, ft.srcpath, ft.destpath);
		return null;
	}

}
