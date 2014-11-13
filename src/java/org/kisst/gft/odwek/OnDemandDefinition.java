package org.kisst.gft.odwek;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import nl.duo.gft.gas.KenmerkenTask;

import org.kisst.props4j.Props;

public class OnDemandDefinition {
	private static class Field {
		public final String alias;
		public final String defaultValue;
		public final String fixedValue;
		public final boolean optional;
		public final int maxLength;
		
		Field(Props props) {
			this.alias = props.getString("alias", null);
			this.defaultValue = props.getString("defaultValue", null);
			this.fixedValue = props.getString("fixedValue", null);
			this.optional = props.getBoolean("optional", false);
			this.maxLength = props.getInt("maxLength", -1);
		}
	}
	
	public final String odfolder;
	public final String odapplgroup;
	public final String odapplication;
	private final HashMap<String, Field> fields = new HashMap<String, Field>();

	private final String datumFormaat;

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
		datumFormaat = props.getString("ondemand.datumformaat",  "MM/dd/yy"); 

	}

	@Override public String toString() { return "OnDemand("+odfolder+","+odapplgroup+","+odapplication+")"; }
	
	public String getKenmerkString(KenmerkenTask kenmerktask, String docField) {
		String waarde=null;
		Field fielddef=fields.get(docField);
		if (fielddef!=null) {
			String kenmerkNaam=fielddef.alias;
			if (kenmerkNaam==null)
				kenmerkNaam=docField;
			Object kenmerk = kenmerktask.getKenmerk(kenmerkNaam);
			if (kenmerk instanceof String)
				waarde = (String) kenmerk;
			else if (kenmerk instanceof Date) {
				DateFormat date2odwek = new SimpleDateFormat(datumFormaat);
				waarde = date2odwek.format((Date)kenmerk);
			}
			else 
				waarde=null;
			if (fielddef.fixedValue != null) {
				if (waarde!=null)
					throw new RuntimeException("kenmerk "+docField+" heeft fixedValue, maar ook een waarde in XML bericht: "+waarde);
				waarde=fielddef.fixedValue;
			}
			else {
				if (waarde==null && fielddef.defaultValue!=null)
					waarde=fielddef.defaultValue;
				if (waarde==null && fielddef.optional==false)
					throw new RuntimeException("veld "+docField+" is niet optioneel en niet meegegeven");
			}
			if (waarde!=null && fielddef.maxLength>=0 && waarde.length()>fielddef.maxLength)
				throw new RuntimeException("Veld "+docField+" heeft waarde ["+waarde+"] met lengte "+waarde.length()+", maar mag maximale lengte hebben van "+fielddef.maxLength);
		}
		else {
			if (waarde==null)
				throw new RuntimeException("veld "+docField+" is niet optioneel en niet meegegeven");
		}
		return waarde;
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
