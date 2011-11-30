package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.RetryableException;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.task.JmsTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Channel extends JmsTaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(Channel.class); 

	public final SshFileServer src;
	public final SshFileServer dest;
	public final String srcdir;
	public final String destdir;
	public final String mode;

	public Channel(GftContainer gft, Props props) {
		super(gft, props);
		getContext().put("channel", this);
		
		this.src=gft.sshhosts.get(props.getString("src.host"));
		this.dest=gft.sshhosts.get(props.getString("dest.host"));

		String dir=props.getString("src.dir",  "");
		if (dir.startsWith("dynamic:"))
			this.srcdir=dir;
		else
			this.srcdir =gft.processTemplate(dir, getContext()); 

		dir=props.getString("dest.dir",  "");
		if (dir.startsWith("dynamic:"))
			this.destdir=dir;
		else
			this.destdir =gft.processTemplate(dir, getContext());

		this.mode=props.getString("mode", "push");
		if (!("pull".equals(mode) || "push".equals(mode)))
			throw new RuntimeException("mode should be push or pull, not "+mode);
		
		SimpleProps actprops=new SimpleProps();

		actprops.put("actions", "log_error");
		this.errorAction=new ActionList(this, actprops);

		actprops.put("actions", "log_start");
		this.startAction=new ActionList(this, actprops);
		
		actprops.put("actions", "log_completed");
		this.endAction=new ActionList(this, actprops);
	}
	

	public String getSrcDescription() {	return src+":"+srcdir; }
	public String getDestDescription() { return dest+":"+destdir; }

	public String toString() { return this.getClass().getSimpleName()+"("+name+" from "+getSrcDescription()+" to "+getDestDescription()+")";}
	public void checkSystemsAvailable(FileTransferTask ft) {
		if (! src.isAvailable())
			throw new RetryableException("Source system "+src+" is not available tot transfer file "+ft.srcpath+" for channel "+name);
		if (! dest.isAvailable())
			throw new RetryableException("Destination system "+dest+" is not available tot transfer file "+ft.destpath+" for channel "+name);
	}

	private String calcPath(String dir, String file, FileTransferTask ft) {
		while (file.startsWith("/"))
			file=file.substring(1);
		// TODO: check for more unsafe constructs
		if (dir.startsWith("dynamic:"))
			return gft.processTemplate(dir.substring(8)+"/"+file, ft.getContext());
		else
			return dir+"/"+file;
	}

	
	public String getSrcPath(String file, FileTransferTask ft) { return calcPath(srcdir, file, ft); }
	public String getDestPath(String file, FileTransferTask ft) {return calcPath(destdir, file, ft);	}
	
	@Override
	public void run(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		checkSystemsAvailable(ft);
		super.run(task);
	}
	
}
