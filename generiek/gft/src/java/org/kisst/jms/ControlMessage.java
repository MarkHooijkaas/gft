package org.kisst.jms;

import java.util.ArrayList;

import javax.jms.Message;

public class ControlMessage {	
	private static final ArrayList<Recognizer> recognizers = new ArrayList<Recognizer>(); 
	
	public static interface Recognizer {
		public boolean isStopMessage(Message msg);
		public boolean isStartMessage(Message msg);
	}
	
	static public boolean isStopMessage(Message msg) {
		for (Recognizer r: recognizers) {
			if (r.isStopMessage(msg))
				return true;
		}
		return false;
	}
	static public boolean isStartMessage(Message msg) {
		for (Recognizer r: recognizers) {
			if (r.isStartMessage(msg))
				return true;
		}
		return false;
	}


}
