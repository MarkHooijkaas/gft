package org.kisst.gft.odwek;


public class MandatoryField implements OnDemandField {
	private final String name;

	public MandatoryField(String name) {
		this.name=name;
	}
	@Override public String getName() { return name; } 
	@Override public String getValue(OnDemandTask task) {
		Object result= task.getOnDemandFieldValue(name);
		if (result==null )
			throw new RuntimeException("Mandatory field "+name+" did not return a null value");
		return result.toString();
	}
	
	@Override public String toString() { return "MandatoryField("+name+")"; }
}
