package org.kisst.gft.odwek;

import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;


public class MandatoryField  extends OptionalField {
	public MandatoryField(String name) { super(name); }
	public MandatoryField(String name, Props props) { super(name, props); }

	@Override public String getValue(Task task) {
		String result=super.getValue(task);
		if (result==null )
			throw new RuntimeException("Mandatory field "+getName()+" returned a null value");
		return result.toString();
	}
}
