package org.kisst.gft.action;

import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;

public class FixPermissions implements Action {

	public boolean safeToRetry() { return true; }

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		String s=ft.channel.dest.call("system dspaut \"obj('"+ft.channel.destdir+"/')\"");
		int pos=s.indexOf("Lijst van machtigingen");
		if (pos<=0)
			throw new RuntimeException("Kan geen lijst van machtingen vinden voor directory "+ft.channel.destdir);
		pos=s.indexOf(":", pos);
		int pos2 = s.indexOf("\n",pos);
		if (pos<=0 || pos2<=0 || pos>pos2)
			throw new RuntimeException("Problem parsing dspaut output: "+s);
		String autlist=s.substring(pos+1, pos2).trim();
		ft.channel.dest.call("system chgaut \"obj('"+ft.channel.destdir+"/"+ft.file+"') autl("+autlist+")\"");
		return null;
	}

}
