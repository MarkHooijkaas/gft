package org.kisst.gft.poller;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.StatusItem;

public class ProblematicPollerFiles extends StatusItem {
	private final GftContainer gft;
	private String message;
	public ProblematicPollerFiles(GftContainer gft) {
		super(gft, ProblematicPollerFiles.class.getSimpleName());
		this.gft=gft;
	}
	@Override public void refresh() {
		super.refresh();
		int count=1;
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
	
	@Override public String getMessage() { return message;}
	
	@Override public void writeDetails(PrintWriter out) { 
		for (Poller poller: gft.pollers.values()) {
			for (PollerJob job: poller.getJobs()) {
				int tmp = job.getNumberOfProblematicFiles();
				if (job.getNumberOfProblematicFiles()>0) {
					out.append(poller.getName()+"/"+job.name+"\t"+tmp+"<br/>\n");
				}
			}
			
		}
	}
}
