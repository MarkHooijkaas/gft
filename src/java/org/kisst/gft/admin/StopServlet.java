package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;

public class StopServlet extends BaseServlet {
	public StopServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println("<pre>");
		out.println("stopping GFT");
		out.println("</pre>");
		response.setStatus(HttpServletResponse.SC_OK);
		gft.stop();
	}

}
