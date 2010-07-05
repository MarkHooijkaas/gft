package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;

public class ConfigServlet extends BaseServlet {
	public ConfigServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println("<pre>");
		out.println(gft.props);
		out.println(((SimpleProps)gft.props).toPropertiesString());
		out.println("</pre>");
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
