package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.props4j.SimpleProps;

public class ConfigServlet extends BaseServlet {
	public ConfigServlet(GftWrapper gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println("<pre>");
		out.println(""+wrapper.getCurrentGft().props.getParent());
		filteredOutput(out, ((SimpleProps)wrapper.getCurrentGft().props).toIndentedString());
		filteredOutput(out, ((SimpleProps)wrapper.getCurrentGft().props).toPropertiesString());
		out.println("</pre>");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	
	private void filteredOutput(PrintWriter out, String str) {
		for (String line: str.split("[\n]")) {
			if ( line.toLowerCase().contains("password")) {
				int pos = line.indexOf('=');
				if (pos<=0)
					pos=line.toLowerCase().indexOf("password")+8;
				line=line.substring(0,pos+1)+"***";
			}
			out.println(line);
		}
	}
	
}
