package org.kisst.gft.task.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kisst.gft.task.LockedBySomeoneElseException;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskQueue;
import org.kisst.gft.task.Action;

public class FileSystem {
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

	public static class Queue implements TaskQueue {
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
		@Override public String toString() { return "FileTaskQueue("+name+")"; }
		public String getName() { return name;}
		public FileSystem getSystem() { return system; }

		public void sendTask(Action type, String data) { sendTask(type, data, 0); }
		public void sendTask(Action type, String data, Date scheduledTime) {
		}
		public void sendTask(Action type, String data, long delay) {
		}
		public int size() { return getAllOpenTasks().size();}

		File getFile(String filename) {
			return new File(fulldir,filename);
		}
		public FileTask getOneOpenTask() {
			List<Task> tasks=getAllOpenTasks();
			if (tasks.size()==0)
				return null;
			return (FileTask) tasks.get(0);
		}
		public List<Task> getAllOpenTasks() {
			List<Task> tasks =new ArrayList<Task>();
			File[] files=fulldir.listFiles();
			for (File f :files) {
				if (f.isFile() && ! f.getName().endsWith(".locked")) {
					tasks.add(new FileTask(this,f.getName())); 
					//System.out.println("adding task "+f);
				}
			}
			//System.out.println("returning "+tasks.size()+" tasks");
			return tasks;
		}
		public List<Task> getSomeOpenTasks() {
			return getAllOpenTasks();
		}
		public void lock(Task t) throws LockedBySomeoneElseException {
			((FileTask)t).lock();
		}
		public void done(Task t) {
			((FileTask)t).done();
		}
	}

	public static class FileTask implements Task {
		private final Queue queue;
		private final String filename;
		private File lockedFile=null;
		
		private FileTask(Queue queue, String filename){
			this.queue=queue;
			this.filename=filename;
		}
		@Override public String toString() { return "FileTask("+queue+","+filename; }
		public boolean isLocked() { return lockedFile!=null;}
		public Queue getQueue() { return queue;}
		
		public void done() {
			if (lockedFile==null)
				throw new RuntimeException("Need to acquire lock before deleting task "+this);
			lockedFile.delete();
			lockedFile=null;
		}

		public String getData() {
			return FileUtil.loadString(getFile());
		}

		public void lock() throws LockedBySomeoneElseException {
			if (isLocked())
				return; // it is not an error to lock a task twice
			// To lock a file it is renamed to extension .locked plus
			// an unique id of the task, so that only THIS task holds the lock
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

		public void move(TaskQueue dest) { move(dest,0); }
		public void move(TaskQueue dest, long delay) {
			if (! isLocked())
				throw new RuntimeException("Need to acquire lock before moving task "+this);
			if (! (dest instanceof Queue))
				throw new RuntimeException("Can not move task "+this+" to queue of inncompatibe type "+dest);
			Queue dest2=(Queue)dest;
			if (dest2.getSystem()!=queue.getSystem())
				throw new RuntimeException("Can not move task "+this+" from queue "+queue+" on system "+queue.getSystem()
						+" to queue "+dest+" on a different system "+dest2.getSystem());
			Queue newq= (Queue) dest;
			File newfile=newq.getFile(filename);
			if (! lockedFile.renameTo(newfile))
				throw new RuntimeException("could not move task "+this+" to queue "+dest);
			lockedFile=null; // not really necessary because the task does not exist anymore
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
