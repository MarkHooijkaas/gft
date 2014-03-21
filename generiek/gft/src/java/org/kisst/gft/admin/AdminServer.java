package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.rest.MappedResource;
import org.kisst.gft.admin.rest.ObjectResource;
import org.kisst.gft.admin.rest.RestServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminServer extends AbstractHandler {
	private final static Logger logger=LoggerFactory.getLogger(AdminServer.class); 
	private final GftContainer gft;
	private Server server=null;

	public AdminServer(GftContainer gft)
	{
		this.gft=gft;
	}
	
	public void addServlet(String url, BaseServlet servlet) {
		handlerMap.put(url, servlet);
	}
	
	public void startListening() {
		int port=gft.props.getInt("admin.port",8080);
		logger.info("admin site running on port {}",port);
		server = new Server(port);
        server.setHandler(this);
        handlerMap.put("default", new TemplateServlet(gft));  //new HomeServlet(gft));
        handlerMap.put("/channel", new ChannelServlet(gft));
        handlerMap.put("/poller", new PollerServlet(gft));
        handlerMap.put("/dir", new DirectoryServlet(gft));
        handlerMap.put("/listener", new ListenerServlet(gft));
        handlerMap.put("/message", new JmsMessageServlet(gft));
        handlerMap.put("/config", new ConfigServlet(gft));
        //handlerMap.put("/restart", new RestartServlet(gft));
        handlerMap.put("/reset", new ResetServlet(gft));
        //handlerMap.put("/shutdown", new ShutdownServlet(gft));
        handlerMap.put("/encrypt", new EncryptServlet(gft));
        
        RestServlet rest=new RestServlet(gft, "/rest/");
        rest.map("gft",new ObjectResource(gft));
        rest.map("channel",new MappedResource(gft.channels));
        rest.map("action",new MappedResource(gft.actions));
        rest.map("listener",new MappedResource(gft.listeners));
        rest.map("httphost",new MappedResource(gft.httphosts));
        rest.map("sshhost",new MappedResource(gft.sshhosts));
        rest.map("poller",new MappedResource(gft.pollers));
        // TODO: rest.map("ondemandhost",new MappedResource(gft.ondemandhosts));
        
        handlerMap.put(rest.getPrefix(), rest);
		try {
			server.start();
		} catch (Exception e) { throw new RuntimeException(e);}
	}

	public void join() {
		try {
			server.join();
		} catch (Exception e) { throw new RuntimeException(e);}
		logger.info("web server stopped");
		server=null;
	}

	public void stopListening() {
		final Server server=this.server; // remember it, because it will set it self to null
		logger.info("Stopping web server");
		try {
			//server.setGracefulShutdown(1000);
			//Thread.sleep(1000);
			server.stop();
			//Thread.sleep(3000);
			for (Connector conn : server.getConnectors())
				conn.close();
			server.destroy();
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
        	try {
				PrintWriter out = response.getWriter();
				out.println(e.getMessage());
				out.println("<pre>");
				e.printStackTrace(out);
				out.println("</pre>");
			} catch (IOException e1) {
				// ignore the new error, and now write to the logfile anyway
				logger.error("Error when handling "+path, e);
			}
        }
	}
}
