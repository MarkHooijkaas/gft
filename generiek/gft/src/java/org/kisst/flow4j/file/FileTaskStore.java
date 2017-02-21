package org.kisst.flow4j.file;

import java.io.File;
import java.util.Map;

import org.kisst.flow4j.FlowRegistry;
import org.kisst.flow4j.Task;
import org.kisst.flow4j.TaskStore;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.FileUtil;

public class FileTaskStore implements TaskStore {
	private final FlowRegistry flowregistry;
	private final File baseDir;
	
	public FileTaskStore(FlowRegistry flowregistry, File baseDir) {
		this.flowregistry=flowregistry;
		this.baseDir = baseDir;
	}

	public Task getTask(String id) {
		File f=new File(baseDir, id+".task");
		if (! f.exists())
			throw new RuntimeException("No task with id "+id+" (should be file named "+f+")");
		SimpleProps props=new SimpleProps();
		props.load(f);
		return new Task(flowregistry, props);
	}

	public Map<String, String> getTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(Task task) {
		File f=new File(baseDir, task.getId()+".task");
		File backup=null;
		if (f.exists()) {
			backup=new File(baseDir, task.getId()+".task.bu");
			if (backup.exists())
				throw new RuntimeException("backup file "+backup+" still exists, will not save over this file");
			f.renameTo(backup);
		}
		FileUtil.saveString(f, task.toString());
		if (backup!=null)
			backup.delete();
	}

}
