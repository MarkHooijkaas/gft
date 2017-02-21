package org.kisst.gft.odwek;

import java.io.PrintWriter;
import java.util.HashMap;

import org.kisst.props4j.Props;

public class OnDemandDefinition {
	public static class Field {
		public final String alias;
		public final String defaultValue;
		public final String fixedValue;
		public final boolean optional;
		public final int maxLength;
		public final String varName;
		
		Field(Props props) {
			this.alias = props.getString("alias", null);
			this.defaultValue = props.getString("defaultValue", null);
			this.fixedValue = props.getString("fixedValue", null);
			this.optional = props.getBoolean("optional", false);
			this.maxLength = props.getInt("maxLength", -1);
			this.varName= props.getString("varName", null);
		}
	}
	
	public final String odfolder;
	public final String odapplgroup;
	public final String odapplication;
	public final HashMap<String, Field> fields = new HashMap<String, Field>();
	
	public OnDemandDefinition(Props props) {
		props = props.getProps("ondemand");
		
		this.odfolder=props.getString("folder"); 
		this.odapplgroup=props.getString("applgroup"); 
		this.odapplication=props.getString("application"); 
		
		Object obj = props.get("field", null);
		if (obj instanceof Props) {
			Props fieldprops = (Props) obj;
			for (String name: fieldprops.keys()) {
				fields.put(name, new Field(fieldprops.getProps(name)));
			}
		}
	}
	public void writeHtml(PrintWriter out) {
		out.println("<h2>Kenmerken</h2>");
		out.println("<table>");
		out.print("<tr>");
		out.print("<td><b>name</b></td>");
		out.print("<td><b>alias</b></td>");
		out.print("<td><b>defaultValue</b></td>");
		out.print("<td><b>fixedValue</b></td>");
		out.print("<td><b>optional</b></td>");
		out.println("</tr>");
		for (String name: fields.keySet()) { 
			Field fld = fields.get(name);
			out.print("<tr>");
			out.print("<td>"+name+"</td>");
			out.print("<td>"+fld.alias+"</td>");
			out.print("<td>"+fld.defaultValue+"</td>");
			out.print("<td>"+fld.fixedValue+"</td>");
			out.print("<td>"+fld.optional+"</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}

}
