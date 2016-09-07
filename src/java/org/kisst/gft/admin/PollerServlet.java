package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.poller.PollerJob;
import org.kisst.props4j.MultiProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;

public class PollerServlet extends BaseServlet {
	public PollerServlet(GftWrapper wrapper) { super(wrapper);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response) == null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		String url = request.getRequestURI();
		String name = url.substring("/poller/".length());
		int pos=name.indexOf('/');
		PrintWriter out = response.getWriter();
		if (pos>0) {
			String polljob = name.substring(pos + 1);
			name=name.substring(0,pos);
			handlePollerJob(out, wrapper.getCurrentGft().pollers.get(name),polljob);
		}
		else
			handlePoller(out, wrapper.getCurrentGft().pollers.get(name));
	}


	public void handlePoller(PrintWriter out, Poller poller) throws IOException {
		out.println("<h1>Poller "+poller.getName()+"</h1>");
		out.println("Host: "+poller.getFileServer().getName()+"<br>");
		out.println("Interval: "+poller.getInterval()+"<br>");
		out.println("Paused: "+poller.isPaused()+"<br>");
		out.println("Running: "+poller.isRunning()+"<br>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<td>name</td>");
		out.println("<td>problems</td>");
		out.println("<td>runs</td>");
		out.println("<td>Successes</td>");
		out.println("<td>Errors</td>");
		out.println("<td>TotalCount</td>");
		out.println("<td>Directory</td>");
		out.println("<td>Move To Directory</td>");
		out.println("</tr>");

		for (PollerJob job : poller.getJobs()) {
			out.println("<tr>");
			out.println("<td><a href=\"/poller/"+poller.getName()+"/"+job.getShortName()+"\">"+job.getShortName()+"</a></td>");
			out.println("<td>"+job.getNumberOfConsecutiveProblems()+"</td>");
			out.println("<td>"+job.getRuns()+"</td>");
			out.println("<td>"+job.getSuccesses()+"</td>");
			out.println("<td>"+job.getErrors()+"</td>");
			out.println("<td>"+job.getTotalCount()+"</td>");
			out.println("<td><a href=\"/dir/"+job.getFileServer().getName()+job.getDir()+"\">"+job.getDir()+"</a></td>");
			out.println("<td><a href=\"/dir/"+job.getFileServer().getName()+job.getMoveToDir()+"\">"+job.getMoveToDir()+"</a></td>");
			out.println("</tr>");
		}

		out.println("<h2>Directories</h2>");
		out.println("<ul>");
		out.println("</ul>");

		out.println("</table>");
		
		out.println("<h2>Config</h2>");
		out.println("<pre>");
		Props props = poller.getProps();
		if (props instanceof SimpleProps)
			out.println(((SimpleProps)props).toIndentedString(""));
		else if (props instanceof MultiProps)
			out.println(((MultiProps)props).toIndentedString(""));
		else
			out.println(""+props);

		out.println("</pre>");
	}

	private void handlePollerJob(PrintWriter out, Poller poller, String polljob) {
		PollerJob job = findJob(poller, polljob);
		if (job==null) {
			out.println("<h1>No job with name "+polljob+" in poller "+poller.getName()+"</h1>");
			return;
		}
		out.println("<h1>PollerJob "+poller.getName()+"/"+job.getShortName()+"</h1>");
		out.println("<h2>Poller: <a href=\"/poller/"+poller.getName()+"\">"+poller.getName()+"</a></h2>");
		out.println("<ul>");
		out.println("<li>Host: "+poller.getFileServer().getName()+"</li>");
		out.println("<li>Interval: "+poller.getInterval()+"</li>");
		out.println("<li>Paused: "+poller.isPaused()+"</li>");
		out.println("<li>Running: "+poller.isRunning()+"</li>");
		out.println("</ul>");

		out.println("<h2>Job Details</h2>");
		out.println("<ul>");
		out.println("<li>Src: <a href=\"/dir/"+job.getFileServer().getName()+job.getDir()+"\">"+job.getDir()+"</a></li>");
		out.println("<li>Dest: <a href=\"/dir/"+job.getFileServer().getName()+job.getMoveToDir()+"\">"+job.getMoveToDir()+"</a></li>");
		out.println("<li>Name: "+job.getShortName()+"</li>");
		out.println("<li>Problems: "+job.getNumberOfConsecutiveProblems()+"</li>");
		out.println("<li>Runs: "+job.getRuns()+"</li>");
		out.println("<li>Sucesses: "+job.getSuccesses()+"</li>");
		out.println("<li>Errors: "+job.getErrors()+"</li>");
		out.println("<li>Total: "+job.getTotalCount()+"</li>");
		out.println("</ul>");

		job.writeHtml(out);

		out.println("<h2>Config</h2>");
		out.println("<pre>");
		Props props = job.getProps();
		if (props instanceof SimpleProps)
			out.println(((SimpleProps)props).toIndentedString(""));
		else if (props instanceof MultiProps)
			out.println(((MultiProps)props).toIndentedString(""));
		else
			out.println(""+props);
		out.println("</pre>");	}

	private PollerJob findJob(Poller poller, String polljob) {
		for (PollerJob job : poller.getJobs()){
			if (polljob.equals(job.getShortName()))
				return job;
		}
		return null;
	}
}
