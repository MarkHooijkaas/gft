package org.kisst.servlet4j;

import org.kisst.http4j.server.HttpCall;
import org.kisst.http4j.server.HttpCallHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ServletContainer implements HttpCallHandler {
	private final static Logger logger=LoggerFactory.getLogger(ServletContainer.class); 

	public void addServlet(String url, AbstractServlet servlet) {
		handlerMap.put(url, servlet);
	}

	private HashMap<String, AbstractServlet> handlerMap=new HashMap<String, AbstractServlet>();
	@Override public void handle(HttpCall call, String subPath) {
	//public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) {
		logger.info("Handling request {}",call.request.getQueryString());
		String path=call.request.getRequestURI();
        call.baseRequest.setHandled(true);
        try {
			if ("/".equals(path))
				path="default";
        	for (String prefix : handlerMap.keySet()) {
        		if (path.startsWith(prefix)) {
        			handlerMap.get(prefix).handle(call.request, call.response);
        			return;
        		}
        	}
        	//handlerMap.get("default").handle(request, response);
			throw new RuntimeException("No servlet for path ["+path+"}");
        }
        catch (Exception e) {
        	try {
				PrintWriter out = call.response.getWriter();
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
