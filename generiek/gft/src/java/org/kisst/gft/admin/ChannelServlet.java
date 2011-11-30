package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.task.TaskDefinition;

public class ChannelServlet extends BaseServlet {
	public ChannelServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String name=url.substring("/channel/".length());
		TaskDefinition def=gft.getTaskDefinition(name);
		writeChannelInfo(out, (Channel) def);
	}
	
	
	private void writeChannelInfo(PrintWriter out, Channel ch) {
		out.println("<h1>Channel "+ch.getName()+"</h1>");
		out.println("<h2>Directories</h2>");
		out.println("<ul>");
		out.println("<li>FROM: <a href=\"/dir/"+ch.src.getSshHost().name+"/"+ ch.srcdir +"\">"+ch.src.getSshHost().name +"/"+ch.src.getBasePath() + ch.srcdir +"</a>");
		out.println("<li>TO:   <a href=\"/dir/"+ch.dest.getSshHost().name+"/"+ch.destdir+"\">"+ch.dest.getSshHost().name+"/"+ch.dest.getBasePath()+ ch.destdir+"</a>");
		out.println("</ul>");

		out.println("<h2>Logging</h2>");
		out.println("<ul>");
		out.println("<li><a href=\"/logging/days=1&channel="+ch.getName()+"\">ALL Logging</a>");
		out.println("<li><a href=\"/logging/days=1&channel="+ch.getName()+"&level=error\">ERROR Logging</a>");
		out.println("</ul>");
		
		out.println("<h2>Config</h2>");
		out.println("<pre>");
		out.println(ch.props);
		out.println("</pre>");
	}

}
