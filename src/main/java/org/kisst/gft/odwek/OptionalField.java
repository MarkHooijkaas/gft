package org.kisst.gft.odwek;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;



public class OptionalField implements OnDemandField {
	private final String name;
	private final String alias;
	private final String defaultValue;
	private final String dateFormat;
	

	public OptionalField(String name) { this (name, (String) null); }
	public OptionalField(String name, String defaultValue) {
		this.name=name;
		this.alias=name;
		this.defaultValue=defaultValue;
		this.dateFormat="yyyy-MM-dd";
	}

	public OptionalField(String name, Props props) {
		this.name=name;
		this.alias=props.getString("alias",name);
		this.dateFormat = props.getString("dateFormat", "yyyy-MM-dd");
		this.defaultValue = props.getString("defaultValue", null);
	}

	
	@Override public String getName() { return name; } 
	@Override public String getValue(Task task) {
		Object result= task.getFieldValue(alias);
		if (result instanceof Date) {
			DateFormat date2odwek = new SimpleDateFormat(dateFormat);
			result = date2odwek.format((Date) result);
		}
		if (result==null )
			return defaultValue;
		return result.toString();
	}
	
	@Override public String toString() {
		String result=this.getClass().getSimpleName()+"(";
		String sep="";
		if (defaultValue!=null) {
			result+=sep+"default="+defaultValue;
			sep=",";
		}
		if (alias!=name) {
			result+=sep+"alias="+alias;
			sep=",";
		}
		return result+")";
	}
}
