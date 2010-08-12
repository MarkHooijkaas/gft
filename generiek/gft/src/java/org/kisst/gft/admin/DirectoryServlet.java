package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;

public class DirectoryServlet extends BaseServlet {
	public DirectoryServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String name=url.substring("/dir/".length());
		out.println("<h1>Directory "+name+"</h1>");
		out.println("<pre>");
		out.println(gft.sshhosts.get(name).ls());
		out.println("</pre>");
	}

}
