package org.kisst.gft;

import java.io.PrintWriter;

import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.props4j.Props;

public class BrokenChannel implements TaskDefinition {
	private final Exception error;
	private final String name;
	private long totalCount=0;
	
	public BrokenChannel(GftContainer gft, Props props, Exception e) {
		//super(gft, props);
		this.name=props.getLocalName();
		this.error=e;
	}
	
	@Override public void writeHtml(PrintWriter out) { out.println(error.getMessage());}
	@Override public String getName() { return name;}

	@Override public void run(Task task) {
		totalCount++;
		throw new RuntimeException("Channel "+name+" was not configured correctly");
	}

	public String getSrcDescription() {	return "<b><blink>ERROR</blink></b>"; } 
	public String getDestDescription() { return getSrcDescription(); }
	public long getTotalCount() { return totalCount; }
	public long getErrorCount() { return totalCount; }
}
