package org.kisst.gft.action;

import org.kisst.gft.task.TaskDefinition;


public interface ActionCreator {
	Action createAction(TaskDefinition taskdef, Class<?> defaultActionClass);
	Action createAction(TaskDefinition taskdef, String name);
}
