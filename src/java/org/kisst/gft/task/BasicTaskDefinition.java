package org.kisst.gft.task;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.LogService;
import org.kisst.gft.action.Action;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTaskDefinition implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 
	//abstract protected void executeTask(Task task);
	protected String getLogDetails(Task task) { return task.toString(); }

	
	public final GftContainer gft;
	public final String name;


	public final Props props;

	private long totalCount=0;
	private long errorCount=0;

	public BasicTaskDefinition(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		this.name=props.getLocalName();
	}

	@Override public Props getProps() { return props; }
	public String getName() { return name; }
	public long getTotalCount() { return totalCount; }
	public long getErrorCount() { return errorCount; }
	abstract public Action getFlow();
	
	public void run(Task task) {
		try {
			totalCount++;
			logStart(task);
			executeTask(task);
			logCompleted(task);
		}
		catch (RuntimeException e) {
			errorCount++;
			task.setLastError(e);
			try {
				logError(task, e);
			}
			catch(RuntimeException e2) { 
				logger.error("Could not perform the error actions ",e);
				// ignore this error which occurred 
			}
			throw e;
		}
	}
	
	
	private void logStart(Task task) {
		LogService.log("info", "start", task.getTaskDefinition().getName(), "started", "Started "+getLogDetails(task)); 
	}
	
	private void logCompleted(Task task) {
		LogService.log("info", "done", task.getTaskDefinition().getName(), "completed","Completed "+getLogDetails(task));
	}
	private void logError(Task task, RuntimeException e) {
		String details = "Fout bij actie:"+task.getLastAction()+" fout:"+e.getMessage()+getLogDetails(task);
		LogService.log("error", task.getLastAction(), task.getTaskDefinition().getName(), "error", details);
	}

	protected void writeHtmlHeader(PrintWriter out) {
		out.println("<h1>Channel "+getName()+"</h1>");
		out.println("<h2>Logging</h2>");
		out.println("<ul>");
		out.println("<li><a href=\"/logging/hours=1&channel="+getName()+"\">ALL Logging</a>");
		out.println("<li><a href=\"/logging/hours=1&channel="+getName()+"&level=error\">ERROR Logging</a>");
		out.println("</ul>");
		if (getFlow() instanceof WritesHtml)
			((WritesHtml)getFlow()).writeHtml(out);
	}
	protected void writeHtmlFooter(PrintWriter out) {
		out.println("<h2>Config</h2>");
		out.println("<pre>");
		out.println(props);
		out.println("</pre>");
	}

	protected void executeTask(Task task) { getFlow().execute(task); }

	@Override public void writeHtml(PrintWriter out) {
		writeHtmlHeader(out);
		writeHtmlFooter(out);
	}
}