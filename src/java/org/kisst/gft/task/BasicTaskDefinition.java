package org.kisst.gft.task;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.props4j.Props;
import org.kisst.util.IndentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTaskDefinition implements TaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 
	//abstract protected void executeTask(Task task);
	protected String getLogDetails(Task task) { return task.toString(); }

	
	public final GftContainer gft;
	public final String name;
	public final String tags;
	public final String comment;


	public final Props props;

	private long totalCount=0;
	private long errorCount=0;

	public BasicTaskDefinition(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		this.name=props.getLocalName();
		this.tags=props.getString("tags","").trim();
		this.comment =props.getString("comment",null);
		gft.addTags(this, tags);
	}

	public boolean hasTag(String tag) {
		if (tag==null)
			return false;
		tag=tag.trim();
		if ("*".equals(tag))
			return true;
		return (","+tags+",").indexOf(","+tag+",")>=0;
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
	
	
	protected void logStart(Task task) {
		task.logInfo("Started: "+task); 
	}
	
	protected void logCompleted(Task task) {
		task.setCompleted();
		task.logInfo("Completed: "+task);
	}
	protected void logError(Task task, RuntimeException e) {
		task.logError("Fout: "+e.getMessage()+": "+task);
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

	protected void writeHtmlBody(PrintWriter out) {}

	protected void writeHtmlFooter(PrintWriter out) {
		out.println("<h2>Config</h2>");
		out.println("<pre>");
		out.println(IndentUtil.toIndentedString(props, ""));
		out.println("</pre>");
	}

	protected void executeTask(Task task) {
		Action action = getFlow();
		//task.setCurrentAction(action);
		action.execute(task);
	}

	@Override public void writeHtml(PrintWriter out) {
		writeHtmlHeader(out);
		writeHtmlBody(out);
		writeHtmlFooter(out);
	}
}