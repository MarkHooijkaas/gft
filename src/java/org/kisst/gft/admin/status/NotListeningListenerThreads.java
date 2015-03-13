package org.kisst.gft.admin.status;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsListener;
import org.kisst.jms.MultiListener;

public class NotListeningListenerThreads extends StatusItem {
	private String message;
	public NotListeningListenerThreads(GftContainer gft) {
		super(gft, NotListeningListenerThreads.class.getSimpleName());
	}
	@Override public void refresh() {
		int count=0;
		String message="";
		for (MultiListener ml : gft.listeners.values()) {
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
		for (MultiListener ml : gft.listeners.values()) {
			int i=0;
			for (JmsListener l : ml.listeners) {
				i++;
				out.write("<tr><td>"+ml.getName()+"/"+i+"</td><td>"+l.getStatus()+"</td></tr>\n");
			}
		}
		out.write("</table>\n");
	}
}
