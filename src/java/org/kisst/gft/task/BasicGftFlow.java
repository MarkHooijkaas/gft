package org.kisst.gft.task;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;

import org.kisst.flow4j.BasicLinearFlow;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;

public class BasicGftFlow extends BasicLinearFlow implements Action, WritesHtml {
	private final BasicTaskDefinition taskdef;
	public BasicGftFlow(BasicTaskDefinition taskdef, Props props) { 
		super(createChannelProps(taskdef.gft,props));
		this.taskdef=taskdef;
	}
	public GftContainer getGft() { return taskdef.gft; }
	private static Props createChannelProps(GftContainer gft, Props props) {
		LayeredProps lprops=new LayeredProps(gft.props.getProps("global"));
		lprops.addLayer(props);
		return lprops;
	}
	
	protected Action myCreateAction(Class<?> clz, Props props) {
		Constructor<?> c=ReflectionUtil.getFirstCompatibleConstructor(clz, new Class<?>[] {BasicTaskDefinition.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef, props} );

		c=ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef.gft, props} );
		return super.myCreateAction(clz, props); 
	}
	
	@Override public void writeHtml(PrintWriter out) {
		out.println("<h2>Flow</h2>");
		out.println("<table>");
		out.print("<tr>");
		out.print("<td><b>name</b></td>");
		out.print("<td><b>class</b></td>");
		out.print("<td><b>skipped?</b></td>");
		out.println("</tr>");
		for (String name: actions.keySet()) { 
			Action act=actions.get(name);
			out.print("<tr>");
			out.print("<td>"+name+"</td>");
			out.print("<td>"+act.toString()+"</td>");
			if (isSkippedAction(name))
				out.print("<td>SKIPPED</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}
}
