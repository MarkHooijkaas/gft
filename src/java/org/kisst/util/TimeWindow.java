package org.kisst.util;


public class TimeWindow {
	private final int startHour;
	private final int startMinute;
	private final int endHour;
	private final int endMinute;
	
	public TimeWindow(String window) {
		String[] parts=window.split("-");
		if (parts.length!=2)
			throw new IllegalArgumentException("time window string ["+window+"] should contain exactly one - symbol");
		startHour=getHour(parts[0]);
		startMinute=getMinute(parts[0]);
		endHour=getHour(parts[1]);
		endMinute=getMinute(parts[1]);
		if (startHour>endHour)
			throw new IllegalArgumentException("Time interval "+this+" should not have higher start hour than end hour");
		if (startHour==endHour && startMinute>endMinute)
			throw new IllegalArgumentException("Time interval "+this+" should not have higher start time than end time");
		
	}

	public String toString() { return startHour+":"+startMinute+"-"+endHour+":"+endMinute; }
	
	public boolean isTimeInWindow(int hour, int minute) {
		if (hour<startHour || hour>endHour)
			return false;
		if (hour>startHour || minute>=startMinute)
			if (hour<endHour || minute<=endMinute)
				return true;
		return false;
	}
	
	private int getHour(String time) {
		String[] parts=time.split(":");
		if (parts.length!=2)
			throw new IllegalArgumentException("time string ["+time+"] should contain exactly one : symbol");
		return Integer.parseInt(parts[0].trim());
	}
	private int getMinute(String time) {
		String[] parts=time.split(":");
		if (parts.length!=2)
			throw new IllegalArgumentException("time string ["+time+"] should contain exactly one : symbol");
		return Integer.parseInt(parts[1].trim());
	}
}
