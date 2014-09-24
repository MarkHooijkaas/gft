package org.kisst.gft;

import java.util.ArrayList;


public class LogService {
	public static interface Logger{
		public void log(String level, String tech_proc, String func_proc, String eventtype, String msg);
	}	

	private static final ArrayList<Logger> loggers = new ArrayList<Logger>();
	
	public static void registerLogger(Logger l) { loggers.add(l); }
	public static void log(String level, String tech_proc, String func_proc, String eventtype, String msg) {
		for (Logger l: loggers)
			l.log(level, tech_proc, func_proc, eventtype, msg);
	}
}
