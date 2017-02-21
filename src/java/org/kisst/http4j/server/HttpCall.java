package org.kisst.http4j.server;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

public class HttpCall {
	final static Logger logger=LoggerFactory.getLogger(HttpCall.class);

	public final Request baseRequest;
	public final HttpServletRequest request;
	public final HttpServletResponse response;
	private PrintWriter out=null;


	protected HttpCall(HttpCall call) { this(call.baseRequest, call.request,call.response); }
	public HttpCall(Request baseRequest,HttpServletRequest request, HttpServletResponse response) {
		this.baseRequest=baseRequest;
		this.request=request;
		this.response=response;
	}
	
	public boolean isGet() { return "GET".equals(request.getMethod()); }
	public boolean isPost() { return "POST".equals(request.getMethod()); }
	public boolean isAjax() { return "true".equals(request.getParameter("ajax")); }
	
	@Override public String toString(){ return toString(null); }
	public String toString(String extra){
		StringBuilder result=new StringBuilder(request.getMethod());
		result.append("(");
		result.append(getLocalUrl());
		if (extra!=null)
			result.append(", "+extra);
		if (isPost()) {
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name=names.nextElement();
				result.append(", "+name+"="+request.getParameter(name));
			}
		}
		result.append(")");
		return result.toString();
	}
	
	public String getTopUrl() {
		String url=request.getScheme()+"://"+request.getServerName();
		url+=":"+request.getServerPort();
		return url;
	}
	public String getLocalUrl() {
		String url=request.getRequestURI();
		String query = request.getQueryString();
		if (query!=null && query.trim().length()>0)
			url+="?"+query;
		return url;
	}
	
	public void handle(String subPath) {
		String method = request.getMethod();
		if (isGet())
			handleGet(subPath);
		else if (isPost())
			handlePost(subPath);
		else
			throw new RuntimeException("Unknown method type "+method);
	}

	public void handleGet(String subPath) { invalidPage(); }
	public void handlePost(String subPath) { invalidPage(); }
	
	public void sendError(int code, String message) {
		try {
			response.sendError(code, message);
		} 
		catch (IOException e) { throw new RuntimeException(e);}
	}

	public void printParams() {
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements()) {
			String s=names.nextElement();
			System.out.println(s+" => "+request.getParameter(s));
		}
	}
	
	public PrintWriter getWriter() {
		try { 
			if (out==null)
				out = response.getWriter();
			return out;
		}
		catch (IOException e) { throw new RuntimeException(e);}
	}
	public void output(String text) { getWriter().append(text); }
	public void close() {
		if (out!=null)
			out.close();
		out=null;
	}

	public class NoSuchPageException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
	
	public void invalidPage() { throw new NoSuchPageException(); }

	public void redirect(String url) {
		//System.out.println("redirect to "+url+ " from "+getLocalUrl());
		try {
			if (!response.isCommitted()) {
				response.resetBuffer();
				response.sendRedirect(url);
			}
			else
				logger.error("Could not redirect already committed call to url "+url+" from call "+this);
		}
		catch (IOException e) { throw new RuntimeException(e);}
	}
	
	public String calcSubPath(String path) {
		while (path.startsWith("/"))
			path=path.substring(1);
		int pos=path.indexOf("/");
		if (pos<=0)
			return "";
		path=path.substring(pos+1);
		while (path.startsWith("/"))
			path=path.substring(1);
		return path;
	}

	public Cookie getNamedCookie(String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies==null)
			return null;
		for (Cookie c: cookies) {
			if (name.equals(c.getName()))
				return c;
		}
		return null;
	}
	public String getNamedCookieValue(String name, String defaultValue) {
		Cookie c=getNamedCookie(name);
		if (c==null)
			return defaultValue;
		return c.getValue();
	}
	
	public void setNamedCookieValue(String name, String value, int duration) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(duration);
		//cookie.setSecure(true); // TODO: does not work with http: development
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	public void clearCookie(String cookieName) {
		Cookie cookie = new Cookie(cookieName, null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);		
	}

	
	public UnauthorizedException throwUnauthorized(String message) { throw new UnauthorizedException(message); }
	
	public boolean isAuthenticated() { return false; }
	public void ensureUser() { if (! isAuthenticated()) throwUnauthorized("Not Authenticated user"); }
	
	public static class UnauthorizedException extends HttpServer.HttpException {
		private static final long serialVersionUID = 1L;
		public UnauthorizedException(String msg) {super(HttpServletResponse.SC_UNAUTHORIZED, msg); } 
	}

	
}