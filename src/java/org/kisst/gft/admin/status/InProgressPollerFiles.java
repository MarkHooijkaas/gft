package org.kisst.gft.admin.status;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.poller.PollerJob;

public class InProgressPollerFiles extends StatusItem {
	private String message;
	public InProgressPollerFiles(GftContainer gft) {
		super(gft, InProgressPollerFiles.class.getSimpleName());
	}
	@Override public void refresh() {
		int count=0;
		String message="";
		for (Poller poller: gft.pollers.values()) {
			for (PollerJob job: poller.getJobs()) {
				int tmp = job.getNumberOfInProgressFiles();
				if (tmp>0) {
					count+=tmp;
					message += poller.getName()+"/"+job.name+"\t"+tmp+"\n";
				}
			}
			
		}
		this.problemCount=count;
		this.message=message;
	}
	
	@Override public String getMessage() { return message;}
	
	@Override public void writeDetails(PrintWriter out) {
		super.writeDetails(out);
		out.write("<table><tr><td><b>Job</b></td><td><b>count</b></td></tr></h3>\n");
		for (Poller poller: gft.pollers.values()) {
			for (PollerJob job: poller.getJobs()) {
				int tmp = job.getNumberOfInProgressFiles();
				out.write("<tr><td>"+poller.getName()+"/"+job.name+"</td><td>"+tmp+"</td></tr>\n");
			}
		}
		out.write("</table>\n");
	}
}
