package org.kisst.gft;

import java.io.PrintWriter;

import org.kisst.gft.action.Action;
import org.kisst.gft.filetransfer.DummyFileServer;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.action.DestinationFile;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.props4j.Props;

public class BrokenChannel implements TaskDefinition, SourceFile, DestinationFile {
	private final Exception error;
	private final String name;
	private long totalCount=0;
	private final Props props;
	private FileLocation errorloc;
	
	public BrokenChannel(GftContainer gft, Props props, Exception e) {
		//super(gft, props);
		this.props=props;
		this.name=props.getLocalName();
		this.error=e;
		this.errorloc=new FileLocation(new DummyFileServer("ERROR"),"");
	}
	
	@Override public void writeHtml(PrintWriter out) { out.println(error.getMessage());}
	@Override public String getName() { return name;}

	@Override public void run(Task task) {
		totalCount++;
		throw new RuntimeException("Channel "+name+" was not configured correctly");
	}

	public long getTotalCount() { return totalCount; }
	public long getErrorCount() { return totalCount; }

	@Override public Props getProps() { return props; }
	@Override public Action getFlow() { return null; }

	@Override public FileLocation getDestinationFile() { return errorloc; }
	@Override public FileLocation getFinalDestinationFile() { return errorloc; }
	@Override public FileLocation getSourceFile() {return errorloc; }
}
