package org.kisst.gft.task;

import org.kisst.gft.admin.WritesHtml;

public interface TaskDefinition extends WritesHtml {
	public void run(Task task);

	public String getName();
}
