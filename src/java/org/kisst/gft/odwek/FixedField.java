package org.kisst.gft.odwek;


public class FixedField implements OnDemandField {
	private final String name;
	private final String fixedValue;

	public FixedField(String name, String fixedValue) {
		this.name=name;
		this.fixedValue=fixedValue;
	}
	@Override public String getName() { return name; } 
	@Override public String getValue(OnDemandTask task) { return fixedValue; }
	@Override public String toString() { return "FixedValue("+fixedValue+")"; }
}
