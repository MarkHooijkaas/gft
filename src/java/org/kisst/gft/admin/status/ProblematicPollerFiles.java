package org.kisst.gft.admin.status;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.poller.PollerJob;

public class ProblematicPollerFiles extends StatusItem {
	private String message;
	public ProblematicPollerFiles(GftWrapper gft) {
		super(gft, ProblematicPollerFiles.class.getSimpleName());
	}
	@Override public void refresh() {
		int count=0;
		String message="";
		for (Poller poller: wrapper.getCurrentGft().pollers.values()) {
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
		out.write("<table><tr><td><b>Poller</b></td><td><b>Job</b></td><td><b>dir</b></td><td><b>count</b></td></tr></h3>\n");
		for (Poller poller: wrapper.getCurrentGft().pollers.values()) {
			for (PollerJob job: poller.getJobs()) {
				int tmp = job.getNumberOfProblematicFiles();
				out.write("<tr><td>"+poller.getName()+"</td>");
				out.write("<td>"+job.name+"</td>");
				out.write("<td><a href=\"/dir/"+poller.getFileServer().getName()+"/"+job.getDir()+"\">"+job.getDir()+"</a></td>");
				out.write("<td>"+tmp+"</td></tr>\n");
			}
		}
		out.write("</table>\n");
	}
}
