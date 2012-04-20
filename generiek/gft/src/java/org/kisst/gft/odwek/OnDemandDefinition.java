package org.kisst.gft.odwek;

import java.util.HashMap;

import org.kisst.props4j.Props;

public class OnDemandDefinition {
	public static class Field {
		public final String alias;
		public final String defaultValue;
		public final String fixedValue;
		public final boolean optional;
		
		Field(Props props) {
			this.alias = props.getString("alias", null);
			this.defaultValue = props.getString("defaultValue", null);
			this.fixedValue = props.getString("fixedValue", null);
			this.optional = props.getBoolean("optional", false);
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
}
