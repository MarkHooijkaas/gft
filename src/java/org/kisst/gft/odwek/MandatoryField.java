package org.kisst.gft.odwek;

import org.kisst.props4j.Props;


public class MandatoryField  extends OptionalField {
	public MandatoryField(String name) { super(name); }
	public MandatoryField(String name, Props props) { super(name, props); }

	@Override public String getValue(OnDemandTask task) {
		String result=super.getValue(task);
		if (result==null )
			throw new RuntimeException("Mandatory field "+getName()+" did not return a null value");
		return result.toString();
	}
}
