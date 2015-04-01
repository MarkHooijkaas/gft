package org.kisst.gft.task;

import java.lang.reflect.Constructor;

import org.kisst.flow4j.BasicLinearFlow;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;

public class BasicGftFlow extends BasicLinearFlow implements Action {
	private final GftContainer gft;
	public BasicGftFlow(GftContainer gft, Props props) { 
		super(props);
		this.gft=gft;
	}
	//public BasicTaskDefinition getTaskDef() { return taskdef; }
	public GftContainer getGft() { return gft; }
	
	@Override protected Action myCreateAction(Class<?> clz, Props props) {
		//Constructor<?> c=ReflectionUtil.getFirstCompatibleConstructor(clz, new Class<?>[] {BasicTaskDefinition.class, Props.class} );
		//if (c!=null)
		//	return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef, props} );

		Constructor<?> c=ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {gft, props} );
		return super.myCreateAction(clz, props); 
	}
}
