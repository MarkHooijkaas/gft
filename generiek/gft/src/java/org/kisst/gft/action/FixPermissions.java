package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;

public class FixPermissions implements Action {

	private final String otherUser;
	
	public FixPermissions (GftContainer gft, Props props) {
		otherUser =props.getString("otherUser");
	}


	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		ft.channel.dest.call("system chgaut \"obj('"+ft.channel.destdir+"/"+ft.file+"') user("+otherUser+") DTAAUT(*RWX)\"");
		return null;
	}

}
