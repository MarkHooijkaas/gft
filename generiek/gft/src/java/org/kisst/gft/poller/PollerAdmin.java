package org.kisst.gft.poller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kisst.gft.admin.BaseServlet;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollerAdmin extends AbstractHandler {
	private final static Logger logger=LoggerFactory.getLogger(PollerAdmin.class); 
	private Server server=null;
	private final Props props;

	public PollerAdmin(Props props)
	{
		this.props=props;
	}
	
	public void startListening() {
		int port=props.getInt("gft.admin.port",8080);
		logger.info("admin site running on port {}",port);
		server = new Server(port);
        server.setHandler(this);
        //handlerMap.put("default", new ConfigServlet());  //new HomeServlet(gft));
        
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
        	logger.error("Error when handling "+path, e);
        }
	}
}
