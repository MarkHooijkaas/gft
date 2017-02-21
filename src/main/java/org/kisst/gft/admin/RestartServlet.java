package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;

public class RestartServlet extends BaseServlet {
	public RestartServlet(GftWrapper wrapper) { super(wrapper);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println("<pre>");
		out.println("Restarting GFT");
		out.println("</pre>");
		response.setStatus(HttpServletResponse.SC_OK);
		// The stopping is done in a separate thread, because it will also stop
		// the webserver running this servlet
		//Runnable r=new Runnable() {	public void run() {	gft.restart();	}};
		//new Thread(r).start();
	}

}
