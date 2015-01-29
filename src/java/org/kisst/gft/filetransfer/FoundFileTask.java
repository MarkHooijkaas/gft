package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.poller.PollerJob.Transaction;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.util.TemplateUtil;

public class FoundFileTask extends BasicTask  {
	
	public final FileServerConnection fsconn;
	public final String filename;
	private final String basePath;
	private final Transaction transaction;

	public FoundFileTask(GftContainer gft, TaskDefinition taskdef, FileServerConnection fsconn, String filename) {
		super(gft, taskdef);
		this.fsconn=fsconn;
		this.filename = filename;
        this.basePath = TemplateUtil.processTemplate(taskdef.getProps().getString("moveToDirectory"),gft.getContext());
        Action action = taskdef.getFlow();
        if (action instanceof Transaction)
        	transaction = (Transaction) action;
        else
        	transaction=null;
	}
	@Override public String toString() { return toString(filename); }
	
	public String getFullPath() { return basePath + "/" + filename; }
	
	public void startTransaction() {
		if (transaction!=null)
			transaction.prepareTransaction(this);
	}
	public void commit() {
		if (transaction!=null)
			transaction.commitTransaction(this);
	}
	public void rollback() {
		if (transaction!=null)
			transaction.rollbackTransaction(this);
	}
}
