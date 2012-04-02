package org.kisst.gft.filetransfer;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.RetryableException;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Channel extends BasicTaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(Channel.class); 

	public final SshFileServer src;
	public final SshFileServer dest;
	public final String srcdir;
	public final String destdir;
	public final String mode;

	public Channel(GftContainer gft, Props props) {
		super(gft, props, null);
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

	@Override
	public void run(Task task) {
		FileTransferTask ft= (FileTransferTask) task;
		checkSystemsAvailable(ft);
		super.run(task);
	}
	
	
	public void writeHtml(PrintWriter out) {
		out.println("<h1>Channel "+getName()+"</h1>");
		out.println("<h2>Directories</h2>");
		out.println("<ul>");
		out.println("<li>FROM: <a href=\"/dir/"+src.getSshHost().name+"/"+ srcdir +"\">"+src.getSshHost().name +"/"+src.getBasePath() + srcdir +"</a>");
		out.println("<li>TO:   <a href=\"/dir/"+dest.getSshHost().name+"/"+destdir+"\">"+dest.getSshHost().name+"/"+dest.getBasePath()+ destdir+"</a>");
		out.println("</ul>");

		out.println("<h2>Logging</h2>");
		out.println("<ul>");
		out.println("<li><a href=\"/logging/days=1&channel="+getName()+"\">ALL Logging</a>");
		out.println("<li><a href=\"/logging/days=1&channel="+getName()+"&level=error\">ERROR Logging</a>");
		out.println("</ul>");
		
		out.println("<h2>Config</h2>");
		out.println("<pre>");
		out.println(props);
		out.println("</pre>");
	}
}
