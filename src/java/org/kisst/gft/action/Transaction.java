package org.kisst.gft.action;

import org.kisst.gft.task.Task;

public interface Transaction {
	public void prepareTransaction(Task task);
	public void commitTransaction(Task task);
	public void rollbackTransaction(Task task);
}