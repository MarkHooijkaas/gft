package org.kisst.gft.action;

import java.io.PrintWriter;
import java.util.LinkedHashMap;

import org.kisst.gft.admin.WritesHtml;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionList extends ActionExecutor implements Action, WritesHtml {
	final static Logger logger=LoggerFactory.getLogger(ActionList.class); 
	
	private final LinkedHashMap<String,Action> actions=new LinkedHashMap<String,Action>();

	private final BasicTaskDefinition taskdef;
	
	public ActionList(BasicTaskDefinition taskdef, String[] parts) {
		super(taskdef.getProps());
		this.taskdef=taskdef;
		for (String name: parts) {
			name=name.trim();
			Action a=createAction(name);
			if (a==null)
				throw new RuntimeException("Unknown action "+name);
			this.actions.put(name,a);
		}
	}
	
	public static Action createAction(BasicTaskDefinition taskdef, Class<?> defaultActionClass) {
		String actions=taskdef.getProps().getString("actions", null);
		if (actions==null)
			return taskdef.gft.createAction(taskdef, defaultActionClass);
		String[] parts=actions.split(",");
		if (parts.length==1) {
			return taskdef.gft.createAction(taskdef,parts[0].trim());
		}
		else 
			return new ActionList(taskdef, parts);
	}
	
	
	public boolean safeToRetry() { return false; } // TODO: 

	public Object execute(Task task) {
		for (String name: actions.keySet()) {
			Action a=actions.get(name);
			execute(a,name,task);
		}
		return null;
	}
	
	public boolean contains(Class<?> cls) {
		for (Action a: actions.values()) {
			if (cls.isAssignableFrom(a.getClass()))
				return true;
		}
		return false;
	}
	
	public Action createAction(String name) {
		try {
			return taskdef.gft.createAction(taskdef, name);
		}
		catch (RuntimeException e) {
			throw new RuntimeException("Error when creating action "+name+" in channel "+name,e);
		}
	}
	

	
	@Override public void writeHtml(PrintWriter out) {
		out.println("<h2>Actions</h2>");
		out.println("<table>");
		//out.println("<tr><td>"+name+"</td><td>"+actions.get(name).toString()+"</td></tr>")
		for (String name: actions.keySet()) {
			out.println("<tr><td>"+name+"</td><td>"+actions.get(name).toString()+"</td></tr>");
		}
		out.println("</table>");
	}

}
