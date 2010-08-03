package org.kisst.util;

import java.util.Calendar;

public class TimeWindowList {
	private final TimeWindow[] list;
	
	public TimeWindowList(String windowlist) {
		String[] parts=windowlist.split(",");
		list=new TimeWindow[parts.length];
		for (int i=0; i<parts.length; i++)
			list[i]=new TimeWindow(parts[i]);
	}

	public String toString() {
		if (list.length==0)
			return "";
		StringBuilder result=new StringBuilder(list[0].toString());
		for (int i=1; i<list.length; i++)
			result.append(","+list[i].toString());
		return result.toString();
	}
	
	public boolean isTimeInWindow() {
		Calendar cal=Calendar.getInstance();
		int dow =cal.get(Calendar.DAY_OF_WEEK);
		int hour=cal.get(Calendar.HOUR_OF_DAY);
		int minute=cal.get(Calendar.MINUTE);
		return isTimeInWindow(dow,hour,minute);
	}
	public boolean isTimeInWindow(int dow, int hour, int minute) {
		for (int i=0; i<list.length; i++)
			if (list[i].isTimeInWindow(dow, hour, minute))
				return true;
		return false;
	}
}
