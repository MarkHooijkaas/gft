package org.kisst.gft.odwek;



public class OptionalField implements OnDemandField {
	private final String name;
	private final String defaultValue;

	public OptionalField(String name, String defaultValue) {
		this.name=name;
		this.defaultValue=defaultValue;
	}
	@Override public String getName() { return name; } 
	@Override public String getValue(OnDemandTask task) {
		Object result= task.getOnDemandFieldValue(name);
		if (result==null )
			return defaultValue;
		return result.toString();
	}
	
	@Override public String toString() { return "OptionalField(default="+defaultValue+")"; }
}
