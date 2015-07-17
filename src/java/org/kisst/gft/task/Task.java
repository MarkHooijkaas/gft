package org.kisst.gft.task;



public interface Task {
	public TaskDefinition getTaskDefinition();
	public String getIdentification();
	
	public void run();

	public void logError(String msg);
	public void logWarn(String msg);
	public void logInfo(String msg);

	public String getCurrentAction();
	public void setCurrentAction(String name);

	public void setLastError(Exception e);
	public Exception getLastError();
	
	public void setVar(String name, Object value);
	public Object getVar(String name);
	public String getStringVar(String name);

	public Object getFieldValue(String name); // A field is different from a variable, and usually based on the input (thus not variable)
}
