package org.kisst.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static Date parseDateWithFormat(String date, String format) {
		DateFormat plex2date = new SimpleDateFormat(format);
		try {
			return plex2date.parse(date);
		} 
		catch (ParseException e) { throw new RuntimeException(e); }
	}
}
