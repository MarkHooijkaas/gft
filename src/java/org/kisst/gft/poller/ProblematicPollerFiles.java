package org.kisst.gft.poller;

import org.kisst.gft.GftContainer;
import org.kisst.gft.StatusItem;

public class ProblematicPollerFiles extends StatusItem {
	private final GftContainer gft;
	public ProblematicPollerFiles(GftContainer gft) {
		super(ProblematicPollerFiles.class.getSimpleName());
		this.gft=gft;
	}
	@Override public void refreshData() {
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

}
