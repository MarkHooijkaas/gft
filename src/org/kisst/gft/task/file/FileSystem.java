package org.kisst.gft.task.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kisst.gft.task.CouldNotLockTaskException;
import org.kisst.gft.task.Task;
import org.kisst.gft.task.TaskQueue;
import org.kisst.gft.task.TaskSystem;
import org.kisst.gft.task.TaskType;

public class FileSystem implements TaskSystem {
	private final File basedir;
	FileSystem(String basedir) {
		this.basedir=new File(basedir);
	}
	File getBaseDir() {return basedir; }
	public Queue getQueue(String name) {
		TaskType type=null; // TODO
		return new Queue(this, name, type);
	}

	public static class Queue implements TaskQueue {
		private final FileSystem system;
		private final String name;
		private final TaskType type;
		private final File fulldir;
		private Queue(FileSystem system, String name, TaskType type) {
			this.system=system;
			this.name=name;
			this.type=type;
			this.fulldir=new File(system.getBaseDir(),name);
		}
		@Override public String toString() { return "FileTaskQueue("+name+")"; }
		public String getName() { return name;}
		public FileSystem getSystem() { return system; }

		public void sendTask(TaskType type, String data) { sendTask(type, data, 0); }
		public void sendTask(TaskType type, String data, Date scheduledTime) {
		}
		public void sendTask(TaskType type, String data, long delay) {
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
				if (f.isFile() && ! f.getName().endsWith(".locked"))
					tasks.add(new FileTask(this,f.getName(),null)); // TODO: type
			}
			return tasks;
		}
		public List<Task> getSomeOpenTasks() {
			return getAllOpenTasks();
		}
	}

	public static class FileTask implements Task {
		private final Queue queue;
		private final String filename;
		private File lockedFile=null;
		
		private FileTask(Queue queue, String filename, TaskType type){
			this.queue=queue;
			this.filename=filename;
		}
		@Override public String toString() { return "FileTask("+queue+","+filename; }
		public TaskType getType() { return queue.type; }
		public boolean isLocked() { return lockedFile!=null;}
		public Queue getQueue() { return queue;}
		
		public void delete() {
			if (lockedFile==null)
				throw new RuntimeException("Need to acquire lock before deleting task "+this);
			lockedFile.delete();
			lockedFile=null;
		}

		public String getData() {
			return FileUtil.loadString(getFile());
		}

		public void lock() throws CouldNotLockTaskException {
			if (isLocked())
				return; // it is not an error to lock a task twice
			// To lock a file it is renamed to extension .locked plus
			// an unique id of the task, so that only THIS task holds the lock
			lockedFile=queue.getFile(filename+"."+this.hashCode()+".locked");
			File f=getFile();
			if (!f.exists())
				throw new CouldNotLockTaskException(this);
			try {
				f.renameTo(lockedFile);
			}
			catch(RuntimeException e) {
				if (e.getMessage().indexOf("TODO, what kind of exception")>0)
					throw new CouldNotLockTaskException(this);
				else
					throw e;
			}
		}

		public void move(TaskQueue dest) { move(dest,0); }
		public void move(TaskQueue dest, long delay) {
			if (! isLocked())
				throw new RuntimeException("Need to acquire lock before moving task "+this);
			if (dest.getSystem()!=queue.getSystem())
				throw new RuntimeException("Can not move task "+this+" from queue "+queue+" on system "+queue.getSystem()
						+" to queue "+dest+" on a different system "+dest.getSystem());
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
	}
}
