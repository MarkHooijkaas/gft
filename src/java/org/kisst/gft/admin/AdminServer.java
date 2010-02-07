package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kisst.gft.GftContainer;

public class AdminServer extends AbstractHandler {
	private final GftContainer gft;
	public AdminServer(GftContainer gft)
	{
		this.gft=gft;
	}
	
	public void run() {
		Server server = new Server(gft.props.getInt("gft.admin.port",8080));
		server.setHandler(this);
		try {
			server.start();
			server.join();
		} catch (Exception e) { throw new RuntimeException(e);}
	}

	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	throws IOException, ServletException
	{
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		PrintWriter out = response.getWriter();
		out.println("<h1>GFT</h1>");
		out.println("<h2>Channels</h2>");
		out.println("<table>");
		for (String name : gft.channels.keySet()) {
			out.println("<tr><td>"+name+"</td></tr>");
		}
		out.println("</table>");
	}
}
