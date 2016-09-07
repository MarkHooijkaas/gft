package org.kisst.gft.admin;

import org.kisst.gft.GftWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ReloadServlet extends BaseServlet {
	public ReloadServlet(GftWrapper wrapper) { super(wrapper);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		String result = wrapper.reload();
		PrintWriter out = response.getWriter();
		out.println("<pre>");
		out.println("Reloading hosts, channels and pollers");
		if (result==null)
			out.println("Succeeded");
		else
			out.println("Failed: "+result);
		out.println("</pre>");
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
