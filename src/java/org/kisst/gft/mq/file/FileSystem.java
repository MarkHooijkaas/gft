package org.kisst.gft.mq.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kisst.gft.mq.LockedBySomeoneElseException;
import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.MqSystem;

public class FileSystem implements MqSystem {
	private final File basedir;
	public FileSystem(String basedir) {
		this.basedir=new File(basedir);
		if (this.basedir.exists()) {
			if (! this.basedir.isDirectory())
				throw new RuntimeException("Could not use queue directory "+basedir+" because it is not a directory");
		}
		else
			this.basedir.mkdirs();
	}
	File getBaseDir() {return basedir; }
	public Queue getQueue(String name) {
		return new Queue(this, name);
	}

	public static class Queue implements MqQueue {
		private final FileSystem system;
		private final String name;
		private final File fulldir;
		private Queue(FileSystem system, String name) {
			this.system=system;
			this.name=name;
			this.fulldir=new File(system.getBaseDir(),name);
			if (fulldir.exists()) {
				if (! fulldir.isDirectory())
					throw new RuntimeException("Could not use queue directory "+fulldir+" because it is not a directory");
			}
			else
				fulldir.mkdirs();
		}
		@Override public String toString() { return "FileSystemQueue("+name+")"; }
		public String getName() { return name;}
		public FileSystem getSystem() { return system; }

		public int size() { return getAllMessages().size();}

		File getFile(String filename) {
			return new File(fulldir,filename);
		}
		public Message getOneMessage() {
			List<MqMessage> messages=getAllMessages();
			if (messages.size()==0)
				return null;
			return (Message) messages.get(0);
		}
		public List<MqMessage> getAllMessages() {
			List<MqMessage> messages =new ArrayList<MqMessage>();
			File[] files=fulldir.listFiles();
			for (File f :files) {
				if (f.isFile() && ! f.getName().endsWith(".locked")) {
					messages.add(new Message(this,f.getName())); 
					//System.out.println("adding message "+f);
				}
			}
			//System.out.println("returning "+messages.size()+" messages");
			return messages;
		}
		public List<MqMessage> getSomeMessages() {
			return getAllMessages();
		}
	}

	public static class Message implements MqMessage {
		private final Queue queue;
		private final String filename;
		private File lockedFile=null;
		
		private Message(Queue queue, String filename){
			this.queue=queue;
			this.filename=filename;
		}
		@Override public String toString() { return "FileMessage("+queue+","+filename; }
		public boolean isLocked() { return lockedFile!=null;}
		public Queue getQueue() { return queue;}
		
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
			//System.out.println("locking "+f);
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
			if (! (dest instanceof Queue))
				throw new RuntimeException("Can not move message "+this+" to queue of inncompatibe type "+dest);
			Queue dest2=(Queue)dest;
			if (dest2.getSystem()!=queue.getSystem())
				throw new RuntimeException("Can not move message "+this+" from queue "+queue+" on system "+queue.getSystem()
						+" to queue "+dest+" on a different system "+dest2.getSystem());
			Queue newq= (Queue) dest;
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
}
