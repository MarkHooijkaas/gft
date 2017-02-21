package org.kisst.flow4j;

import java.util.Map;

public interface TaskStore {
	public void save(Task task);
	public Task getTask(String id);
	public Map<String, String> getTasks(); // id, info pairs: TODO, add cursor possibilities
}
