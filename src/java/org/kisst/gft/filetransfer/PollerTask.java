package org.kisst.gft.filetransfer;

import org.kisst.gft.action.Action;
import org.kisst.gft.action.Transaction;
import org.kisst.gft.poller.PollerJob;
import org.kisst.gft.task.BasicTask;

public class PollerTask extends BasicTask  {
	
	public final FileServerConnection fsconn;
	public final String filename;
	private final Transaction transaction;
	private final PollerJob job;
	private boolean inprogress=false;

	public PollerTask(PollerJob taskdef, FileServerConnection fsconn, String filename) {
		super(taskdef.gft, taskdef, filename);
		this.job=taskdef;
		this.fsconn=fsconn;
		this.filename = filename;
        Action action = taskdef.getFlow();
        if (action instanceof Transaction)
        	transaction = (Transaction) action;
        else
        	transaction=null;
	}

	public void moveToInProgress() throws FileCouldNotBeMovedException {
		fsconn.move(getOrigPath(),	getInprogressPath());
		inprogress=true;
	}
	public void deleteInProgressFile() throws FileCouldNotBeMovedException {
		if (inprogress)
			fsconn.deleteFile(getActivePath());
		else
			throw new RuntimeException("Trying to delete inprogress file that is not inprogress "+getActivePath());
	}

	public String getOrigPath() { return job.getDir() + "/" + filename; }
	public String getInprogressPath() { return job.getMoveToDir() + "/" + filename; }
	public String getActivePath() {
		if (inprogress)
			return getInprogressPath();
		else
			return getOrigPath();
	}
	
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
