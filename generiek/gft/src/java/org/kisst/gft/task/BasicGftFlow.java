package org.kisst.gft.task;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;

import org.kisst.flow4j.BasicLinearFlow;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;

public class BasicGftFlow extends BasicLinearFlow {
	private final GftContainer gft;
	public BasicGftFlow(GftContainer gft, Props props) { 
		super(createChannelProps(gft,props));
		this.gft=gft;
	}
	public GftContainer getGft() { return gft; }
	private static Props createChannelProps(GftContainer gft, Props props) {
		LayeredProps lprops=new LayeredProps(gft.props.getProps("global"));
		lprops.addLayer(props);
		return lprops;
	}
	
	protected Action myCreateAction(Class<?> clz, Props props) {
		Constructor<?> c=ReflectionUtil.getConstructor(clz, new Class<?>[] {BasicGftFlow.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {this, props} );

		c=ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {gft, props} );
		return super.myCreateAction(clz, props); 
	}
	
	public void writeHtml(PrintWriter out) {
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
			out.print("<td>"+act.getClass().getSimpleName()+"</td>");
			if (isSkippedAction(name))
				out.print("<td>SKIPPED</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}
}
