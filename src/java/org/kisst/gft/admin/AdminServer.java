package org.kisst.gft.admin;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.rest.MappedResource;
import org.kisst.gft.admin.rest.RestServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminServer extends AbstractHandler {
	private final static Logger logger=LoggerFactory.getLogger(AdminServer.class); 

	private final GftContainer gft;
	public AdminServer(GftContainer gft)
	{
		this.gft=gft;
	}
	
	public void run() {
		int port=gft.props.getInt("gft.admin.port",8080);
		logger.info("admin site running on port {}",port);
		Server server = new Server(port);
        server.setHandler(this);
        handlerMap.put("default", new TemplateServlet(gft));  //new HomeServlet(gft));
        handlerMap.put("/channel", new ChannelServlet(gft));
        handlerMap.put("/config", new ConfigServlet(gft));
        
        RestServlet rest=new RestServlet(gft, "/rest/");
        rest.map("channel",new MappedResource(gft.channels));
        rest.map("action",new MappedResource(gft.actions));
        rest.map("listener",new MappedResource(gft.listeners));
        rest.map("host",new MappedResource(gft.hosts));
        
        handlerMap.put(rest.getPath(), rest);
		try {
			server.start();
			server.join();
		} catch (Exception e) { throw new RuntimeException(e);}
	}

	private HashMap<String, BaseServlet> handlerMap=new HashMap<String, BaseServlet>();
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
	{
		String path=request.getRequestURI();
        baseRequest.setHandled(true);
        try {
        	for (String prefix : handlerMap.keySet()) {
        		if (path.startsWith(prefix)) {
        			handlerMap.get(prefix).handle(request, response);
        			return;
        		}
        	}
        	handlerMap.get("default").handle(request, response);
        }
        catch (Exception e) {
        	logger.error("Error when handling "+path, e);
        }
	}
}
