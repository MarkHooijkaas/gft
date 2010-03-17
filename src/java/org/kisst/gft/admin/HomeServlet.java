package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;

public class HomeServlet extends BaseServlet {
	public HomeServlet(GftContainer gft) { super(gft); }

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<h1>GFT</h1>");

		out.println("<a href=\"config\">Configuration</a><br>");
		out.println("<h2>Channels</h2>");
		out.println("<table>");
		for (String name : gft.channels.keySet()) {
			out.println("<tr><td><a href=\"channel/"+name+"\">"+name+"</a></td><td>"+gft.channels.get(name)+"</td></tr>");
		}
		out.println("</table>");
		
		out.println("<h2>Actions</h2>");
		out.println("<table>");
		for (String name : gft.actions.keySet()) {
			out.println("<tr><td>"+name+"</td><td>"+gft.actions.get(name)+"</td></tr>");
		}
		out.println("</table>");

		out.println("<h2>Listener</h2>");
		out.println("<table>");
		for (String name : gft.listeners.keySet()) {
			out.println("<tr><td>"+name+"</td><td>"+gft.listeners.get(name)+"</td></tr>");
		}
		out.println("</table>");

		out.println("<h2>SSH Hosts</h2>");
		out.println("<table>");
		for (String name : gft.sshhosts.keySet()) {
			out.println("<tr><td>"+name+"</td><td>"+gft.sshhosts.get(name)+"</td></tr>");
		}
		out.println("</table>");

		out.println("<h2>HTTP Hosts</h2>");
		out.println("<table>");
		for (String name : gft.httphosts.keySet()) {
			out.println("<tr><td>"+name+"</td><td>"+gft.httphosts.get(name)+"</td></tr>");
		}
		out.println("</table>");

	}

}
