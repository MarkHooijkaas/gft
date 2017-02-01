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
