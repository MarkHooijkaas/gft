package org.kisst.gft.task;

import java.io.PrintWriter;

public interface TaskDefinition {
	public void run(Task task);

	public String getName();
	public void writeHtml(PrintWriter out);
}
