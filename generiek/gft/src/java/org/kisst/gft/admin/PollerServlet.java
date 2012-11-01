package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.kisst.gft.GftContainer;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.poller.PollerJob;

public class PollerServlet extends BaseServlet {
	public PollerServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String name=url.substring("/poller/".length());
		Poller poller=gft.pollers.get(name);

		out.println("<h1>Poller "+poller.getName()+"</h1>");
		out.println("Interval: "+poller.getInterval()+"<br>");
		out.println("Paused: "+poller.isPaused()+"<br>");
		out.println("Running: "+poller.isRunning()+"<br>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<td>name</td>");
		out.println("<td>runs</td>");
		out.println("<td>Successes</td>");
		out.println("<td>Errors</td>");
		out.println("<td>TotalCount</td>");
		out.println("<td>Directory</td>");
		out.println("<td>Move To Directory</td>");
		out.println("</tr>");

		for (PollerJob job : poller.getJobs()) {
			out.println("<tr>");
			out.println("<td>"+job.getName()+"</td>");
			out.println("<td>"+job.getRuns()+"</td>");
			out.println("<td>"+job.getSuccesses()+"</td>");
			out.println("<td>"+job.getErrors()+"</td>");
			out.println("<td>"+job.getTotalCount()+"</td>");
			out.println("<td>"+job.getDir()+"</td>");
			out.println("<td>"+job.getMoveToDir()+"</td>");
			out.println("</tr>");
		}
		out.println("</table>");
		
		out.println("<h2>Config</h2>");
		out.println("<pre>");
		out.println(poller.getProps());
		out.println("</pre>");
	}

}
