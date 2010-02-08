package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;

public class ChannelServlet extends BaseServlet {
	public ChannelServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String name=url.substring("/channel/".length());
		out.println("<h1>Channel "+name+"</h1>");
		out.println("<pre>");
		out.println(gft.channels.get(name).props);
		out.println("</pre>");
	}

}
