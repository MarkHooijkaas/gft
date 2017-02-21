package org.kisst.gft.admin.status;

import java.io.PrintWriter;
import java.util.HashMap;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.jms.JmsListener;
import org.kisst.jms.MultiListener;

public class NotListeningListenerThreads extends StatusItem {
	private final HashMap<String, MultiListener> listeners;
	private String message;

	public NotListeningListenerThreads(GftWrapper wrapper, HashMap<String, MultiListener> listeners) {
		super(wrapper, NotListeningListenerThreads.class.getSimpleName());
		this.listeners=listeners;
	}
	@Override public void refresh() {
		int count=0;
		String message="";
		for (MultiListener ml : listeners.values()) {
			int i=0;
			for (JmsListener l : ml.listeners) {
				i++;
				if (! "LISTENING".equals(l.getStatus())) {
					count++;
					message += ml.getName()+"/"+i+"\t"+l.getStatus()+"\n";
				}
			}
			
		}
		this.problemCount=count;
		this.message=message;
	}
	
	@Override public String getMessage() { return message;}
	
	@Override public void writeDetails(PrintWriter out) {
		super.writeDetails(out);
		out.write("<table><tr><td><b>Listener</b></td><td><b>Status</b></td></tr></h3>\n");
		for (MultiListener ml : listeners.values()) {
			int i=0;
			for (JmsListener l : ml.listeners) {
				i++;
				out.write("<tr><td>"+ml.getName()+"/"+i+"</td><td>"+l.getStatus()+"</td></tr>\n");
			}
		}
		out.write("</table>\n");
	}
}
