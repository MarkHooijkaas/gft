package org.kisst.gft.action;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.props4j.MultiProps;
import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;

public class ActionCreator {
	private final HashMap<String, Class<?>>   actions= new LinkedHashMap<String, Class<?>>();

	public void addActionType(String name, Class<?> cls) { actions.put(name, cls); }

	
	private Props getActionProps(Props channelprops, Class<?> clz) {
		if (channelprops.get(clz.getSimpleName(),null) instanceof Props)
			return new MultiProps(channelprops.getProps(clz.getSimpleName()), channelprops);
		return channelprops;
	}
	
	public Action createAction(TaskDefinition taskdef, Class<?> clz) {
		if (clz==null)
			throw new IllegalArgumentException("Unknown action when definining "+taskdef.getName());
		Props actionprops=getActionProps(taskdef.getProps(), clz);
		Constructor<?> c = ReflectionUtil.getFirstCompatibleConstructor(clz, new Class<?>[] {TaskDefinition.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef, actionprops} );

		c = ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {this, actionprops} );
		
		return (Action) ReflectionUtil.createObject(clz);
	}

	
}
