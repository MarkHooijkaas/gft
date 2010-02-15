package org.kisst.gft.admin.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.BaseServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServlet extends BaseServlet {
	private final Logger logger=LoggerFactory.getLogger(RestServlet.class);
	private final String prefix;
	private final LinkedHashMap<String, Resource> urls=new LinkedHashMap<String, Resource>();
	public RestServlet(GftContainer gft, String path) { 
		super(gft);
		this.prefix=path;
	}
	public String getPrefix() { return prefix; }
	public void map(String path, Resource res) { urls.put(path, res); }
 
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url=request.getRequestURI();
		String path=url.substring(this.prefix.length());
		while (path.startsWith("/"))
			path=path.substring(1);

		int pos=path.indexOf('/');
		String name=path;
		String remainder=null;
		if (pos>0) {
			name=path.substring(0,pos);
			remainder=path.substring(pos+1);
		}
		logger.info("searching for resource {}",name);
		handle(urls.get(name), remainder,request, response);
	}
	
	public void handle(Resource res, String path, HttpServletRequest request, HttpServletResponse response) {
		String method=request.getMethod();
		if (request.getParameter("HTTP_METHOD_OVERRIDE")!=null)
			method=request.getParameter("HTTP_METHOD_OVERRIDE");
		if ("GET".equals(method))
			doGet(res,path, request, response);
		else if ("POST".equals(method))
			doPost(res, path, request, response);
		else if ("PUT".equals(method))
			doPut(res, path, request, response);
		else if ("DELETE".equals(method))
			doDelete(res, path, request, response);
		else
			throw new RuntimeException("Unknown HTTP method "+method);
	}

	private PrintWriter getWriter(HttpServletResponse response) {
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return response.getWriter();
		} 
		catch (IOException e) { throw new RuntimeException(e); }
	}
	protected void doGet(Resource res, String path, HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = getWriter(response);
		try {
			out.write("<pre>\n");
			if (path==null || path.length()==0) 
				out.write(res.toString());
			else
				out.write(res.get(path).toString());
			out.write("</pre>\n");
		}
		finally { out.close(); }
	}

	protected void doPost(Resource res, String path, HttpServletRequest request, HttpServletResponse response) {
		throw new RuntimeException("not implemented yet");
	}

	protected void doPut(Resource res, String path, HttpServletRequest request, HttpServletResponse response) {
		throw new RuntimeException("not implemented yet");
	}

	protected void doDelete(Resource res, String path, HttpServletRequest request, HttpServletResponse response) {
		throw new RuntimeException("not implemented yet");
	}
}
