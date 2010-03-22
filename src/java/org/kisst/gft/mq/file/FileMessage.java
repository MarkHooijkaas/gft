package org.kisst.gft.mq.file;

import java.io.File;
import java.util.Date;

import org.kisst.gft.mq.LockedBySomeoneElseException;
import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.mq.MqQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMessage implements MqMessage {
	private final static Logger logger=LoggerFactory.getLogger(FileMessage.class);
	
	private final FileQueue queue;
	private final String filename;
	private File lockedFile=null;

	FileMessage(FileQueue queue, String filename){
		this.queue=queue;
		this.filename=filename;
	}
	@Override public String toString() { return "FileMessage("+queue+","+filename; }
	public boolean isLocked() { return lockedFile!=null;}
	public FileQueue getQueue() { return queue;}
	public String getReplyTo() { return null; }
	public String getMessageId() { return null; }
	public String getCorrelationId() { return null; }
	
	public void done() {
		if (lockedFile==null)
			throw new RuntimeException("Need to acquire lock before deleting message "+this);
		lockedFile.delete();
		lockedFile=null;
	}

	public String getData() {
		return FileUtil.loadString(getFile());
	}

	public void lock() throws LockedBySomeoneElseException {
		if (isLocked())
			return; // it is not an error to lock a message twice
		// To lock a file it is renamed to extension .locked plus
		// an unique id of the message, so that only THIS message holds the lock
		File f=getFile();
		logger.info("locking file {}",f);
		if (!f.exists())
			throw new LockedBySomeoneElseException(this);
		try {
			lockedFile=queue.getFile(filename+"."+this.hashCode()+".locked");
			f.renameTo(lockedFile);
		}
		catch(RuntimeException e) {
			if (e.getMessage().indexOf("TODO, what kind of exception")>0)
				throw new LockedBySomeoneElseException(this);
			else
				throw e;
		}
	}

	public void move(MqQueue dest) { move(dest,0); }
	public void move(MqQueue dest, long delay) {
		if (! isLocked())
			throw new RuntimeException("Need to acquire lock before moving message "+this);
		if (! (dest instanceof FileQueue))
			throw new RuntimeException("Can not move message "+this+" to queue of inncompatibe type "+dest);
		FileQueue dest2=(FileQueue)dest;
		if (dest2.getSystem()!=queue.getSystem())
			throw new RuntimeException("Can not move message "+this+" from queue "+queue+" on system "+queue.getSystem()
					+" to queue "+dest+" on a different system "+dest2.getSystem());
		FileQueue newq= (FileQueue) dest;
		File newfile=newq.getFile(filename);
		if (! lockedFile.renameTo(newfile))
			throw new RuntimeException("could not move message "+this+" to queue "+dest);
		lockedFile=null; // not really necessary because the message does not exist anymore
	}

	private File getFile() {
		if (lockedFile!=null)
			return lockedFile;
		return queue.getFile(filename);
	}

	public void error() { move(null);}
	public void retry() {  move(null);}
	public void retry(Date date) { move(null);}
	public void retry(long delay) { move(null);}
}
