package org.kisst.util;

import java.util.Map;
import java.util.Map.Entry;

public class ThreadUtil {

	public String dumpAllThreads() {
		StringBuilder result=new StringBuilder();
		Map<Thread,StackTraceElement[]> m=Thread.getAllStackTraces();
		for (Entry<Thread,StackTraceElement[]> e: m.entrySet()) {
			if (e.getKey().isDaemon())
				result.append("Daemon ");
			result.append("Thread "+e.getKey()+"\n");
			for (StackTraceElement elm: e.getValue())
				result.append("\t"+elm);
		}
		return result.toString();

	}
}
