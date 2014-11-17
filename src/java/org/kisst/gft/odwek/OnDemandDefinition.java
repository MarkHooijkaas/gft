package org.kisst.gft.odwek;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import org.kisst.props4j.Props;

public class OnDemandDefinition {
	public final String odfolder;
	public final String odapplgroup;
	public final String odapplication;
	private final HashMap<String, OnDemandField> fields = new HashMap<String, OnDemandField>();


	public OnDemandDefinition(String odfolder, String odapplgroup, String odapplication) {
		this.odfolder=odfolder;
		this.odapplgroup=odapplgroup;
		this.odapplication=odapplication;
	}

	public OnDemandDefinition(Props props) {
		this.odfolder=props.getString("folder"); 
		this.odapplgroup=props.getString("applgroup"); 
		this.odapplication=props.getString("application");
	}

	public OnDemandDefinition(OnDemandDefinition def, Props props) {
		if (props==null) {
			this.odfolder=def.odfolder;
			this.odapplgroup=def.odapplgroup;
			this.odapplication=def.odapplication;
		}
		else {
			this.odfolder=props.getString("folder",def.odfolder); 
			this.odapplgroup=props.getString("applgroup", def.odapplgroup); 
			this.odapplication=props.getString("application",def.odapplication);
		}
	}
	
	public OnDemandDefinition addField(String name, OnDemandField f) { fields.put(name, f); return this; }
	public OnDemandDefinition addOptionalField(String name, String defaultValue) { return addField(name, new OptionalField(name,defaultValue)); }
	public OnDemandDefinition addMandatoryField(String name) { return addField(name, new MandatoryField(name)); }
	public OnDemandDefinition addFixedField(String name, String fixedValue) { return addField(name, new FixedField(name, fixedValue)); }
	
	@Override public String toString() { return "OnDemand("+odfolder+","+odapplgroup+","+odapplication+")"; }

	public Set<String> getFieldNames() { return fields.keySet();}
	public OnDemandField getField(String name) { 
		OnDemandField field=fields.get(name);
		if (field==null)
			throw new RuntimeException("Unknown OnDemand field "+name);
		return field;
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
			OnDemandField fld = fields.get(name);
			out.print("<tr>");
			out.print("<td>"+name+"</td>");
			out.print("<td>"+fld+"</td>");
			out.println("</tr>");
		}
		out.println("</table>");
	}

}
