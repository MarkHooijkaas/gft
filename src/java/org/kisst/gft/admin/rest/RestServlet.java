package org.kisst.gft.admin.rest;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.BaseServlet;

public class RestServlet extends BaseServlet {
	private final String path;
	private final LinkedHashMap<String, Resource> urls=new LinkedHashMap<String, Resource>();
	public RestServlet(GftContainer gft, String path) { 
		super(gft);
		this.path=path;
	}
	public String getPath() { return path; }
	public void map(String path, Resource res) { urls.put(path, res); }
 
	public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		//PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String remainder=url.substring(path.length());
		for (String key: urls.keySet()) {
			if (remainder.startsWith(key))
				urls.get(key).handle(remainder.substring(key.length()),request, response);
		}
	}
}
