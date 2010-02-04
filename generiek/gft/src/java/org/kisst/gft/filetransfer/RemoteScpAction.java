package org.kisst.gft.filetransfer;

import org.kisst.gft.action.Action;
import org.kisst.gft.task.Task;

public class RemoteScpAction implements Action {
	
	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String result=Ssh.ssh(ft.channel.cred, ft.channel.getHost(), "ls -la "+ft.file);
		System.out.println(result);
		return null;
	}

}
