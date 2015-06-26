package org.kisst.gft.task;

import org.kisst.gft.action.Action;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.props4j.Props;

public interface TaskDefinition extends WritesHtml {
	public void run(Task task);

	public String getName();
	public Props getProps();
	public Action getFlow();
	
	public long getTotalCount();
	public long getErrorCount();
}
