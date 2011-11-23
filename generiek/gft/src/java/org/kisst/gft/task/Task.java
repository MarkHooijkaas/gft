package org.kisst.gft.task;



public interface Task {
	public TaskDefinition getTaskDefinition();
	
	public void run();

	public void setLastError(Exception e);
	public Exception getLastError();
	public String getLastAction();
	public void setLastAction(String name);
}
