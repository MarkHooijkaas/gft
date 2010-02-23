package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.task.Task;

public class CopyFile implements Action {
	private final SshHost src;
	private final SshHost dest;
	private final String srcdir;
	private final String destdir;
	private final String mode;
	
	public CopyFile(GftContainer gft, Props props) {
		this.src=gft.sshhosts.get(props.getString("src.host"));
		this.dest=gft.sshhosts.get(props.getString("dest.host"));
		this.srcdir=props.getString("src.dir", "");
		this.destdir=props.getString("dest.dir", "");
		this.mode=props.getString("mode", "push");
		if (!("pull".equals(mode) || "push".equals(mode)))
			throw new RuntimeException("mode should be push or pull, not "+mode);
	}

	public Object execute(Task task) {
		FileTransferData ft= (FileTransferData) task.getData();
		if ("push".equals(mode))
			src.copyFileTo(srcdir+"/"+ft.bestand, dest, destdir);
		else if ("pull".equals(mode))
			dest.copyFileFrom(src, srcdir+"/"+ft.bestand, destdir);
		return null;
	}

}
