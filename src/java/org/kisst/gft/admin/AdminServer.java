package org.kisst.gft.admin;

import java.io.IOException;
import java.util.HashMap;

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
		int port=gft.props.getInt("gft.admin.port",8080);
		System.out.println("admin site running on port "+port);
		Server server = new Server(port);
        server.setHandler(this);
        handlerMap.put("default", new HomeServlet(gft));
        handlerMap.put("/channel", new ChannelServlet(gft));
        handlerMap.put("/config", new ConfigServlet(gft));
		try {
			server.start();
			server.join();
		} catch (Exception e) { throw new RuntimeException(e);}
	}

	private HashMap<String, BaseServlet> handlerMap=new HashMap<String, BaseServlet>();
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	throws IOException, ServletException
	{
		String path=request.getRequestURI();
        baseRequest.setHandled(true);
        for (String prefix : handlerMap.keySet()) {
			if (path.startsWith(prefix)) {
				handlerMap.get(prefix).handle(request, response);
				return;
			}
		}
		handlerMap.get("default").handle(request, response);
	}
}
