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
		out.println("URI: "+request.getRequestURI());
		out.println("<h2>Channels</h2>");
		out.println("<table>");
		for (String name : gft.channels.keySet()) {
			out.println("<tr><td>"+name+"</td></tr>");
		}
		out.println("</table>");
	}

}
