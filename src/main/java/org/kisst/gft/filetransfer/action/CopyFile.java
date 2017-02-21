package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class CopyFile extends BaseAction {
	public CopyFile(Props props) { super(props); }

	@Override public boolean safeToRetry() { return true; }

	@Override public void execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		Channel chan=ft.channel;
		FileLocation src=ft.getSourceFile();
		FileLocation dest=ft.getDestinationFile();
		SshFileServer srcsrv= (SshFileServer) src.getFileServer();
		SshFileServer destsrv= (SshFileServer) dest.getFileServer();
		String mode=chan.mode;
		
		if ("push".equals(mode))
			srcsrv.copyFileTo(src.getPath(), destsrv, dest.getPath());
		else if ("pull".equals(mode))
			destsrv.copyFileFrom(srcsrv, src.getPath(), dest.getPath());
	}

}
