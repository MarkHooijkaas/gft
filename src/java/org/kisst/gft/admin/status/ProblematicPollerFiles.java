package org.kisst.gft.admin.status;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.poller.PollerJob;

public class ProblematicPollerFiles extends StatusItem {
	private final GftContainer gft;
	private String message;
	public ProblematicPollerFiles(GftContainer gft) {
		super(gft, ProblematicPollerFiles.class.getSimpleName());
		this.gft=gft;
	}
	@Override public void refresh() {
		int count=0;
		String message="";
		for (Poller poller: gft.pollers.values()) {
			for (PollerJob job: poller.getJobs()) {
				int tmp = job.getNumberOfProblematicFiles();
				if (job.getNumberOfProblematicFiles()>0) {
					count+=tmp;
					message += poller.getName()+"/"+job.name+"\t"+tmp+"\n";
				}
			}
			
		}
		this.problemCount=count;
		this.message=message;
	}
	
	@Override public String getMessage() { refresh(); return message;}
	
	@Override public void writeDetails(PrintWriter out) {
		super.writeDetails(out);
		for (Poller poller: gft.pollers.values()) {
			out.write("<h3>"+poller.getName()+"</h3>\n");
			for (PollerJob job: poller.getJobs()) {
				int tmp = job.getNumberOfProblematicFiles();
				out.write(poller.getName()+"/"+job.name+"\t"+tmp+"<br/>\n");
			}
			
		}
	}
}
