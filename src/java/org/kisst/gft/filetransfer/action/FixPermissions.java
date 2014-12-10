package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.gft.ssh.SshHost;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

public class FixPermissions extends BaseAction {
	public FixPermissions(BasicTaskDefinition taskdef, Props props) { super(taskdef, props); }


	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		String destdir=ft.destpath.substring(0,ft.destpath.lastIndexOf('/'));

		SshHost dest =  ft.channel.dest.getSshHost();
		String s=dest.call("system dspaut \"obj('"+destdir+"/')\"");
		int pos=s.indexOf("Lijst van machtigingen");
		if (pos<=0)
			throw new RuntimeException("Kan geen lijst van machtingen vinden voor directory "+destdir);
		pos=s.indexOf(":", pos);
		int pos2 = s.indexOf("\n",pos);
		if (pos<=0 || pos2<=0 || pos>pos2)
			throw new RuntimeException("Problem parsing dspaut output: "+s);
		String autlist=s.substring(pos+1, pos2).trim();
		dest.call("system chgaut \"obj('"+ft.destpath+"') autl("+autlist+")\"");
		return null;
	}

}
