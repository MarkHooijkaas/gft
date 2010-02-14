package org.kisst.gft.admin.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.util.ReflectionUtil;

public abstract class Resource {

	public void handle(String url, HttpServletRequest request, HttpServletResponse response) {
		if (url.startsWith("/"))
			url=url.substring(1);
		String prop=null;
		String id=url;
		int pos=url.indexOf('/');
		if (pos>0) {
			id=url.substring(0,pos);
			prop=url.substring(pos+1);
		}
		Object obj=lookup(id);
		String method=request.getMethod();
		if (request.getParameter("HTTP_METHOD_OVERRIDE")!=null)
			method=request.getParameter("HTTP_METHOD_OVERRIDE");
		if ("GET".equals(method))
			doGet(obj, prop, request, response);
		else if ("POST".equals(method))
			doPost(obj, prop, request, response);
		else if ("PUT".equals(method))
			doPut(obj, prop, request, response);
		else if ("DELETE".equals(method))
			doDelete(obj, prop, request, response);
		else
			throw new RuntimeException("Unknown HTTP method "+method);
	}

	private PrintWriter getWriter(HttpServletResponse response) {
		try {
			return response.getWriter();
		} 
		catch (IOException e) { throw new RuntimeException(e); }
	}
	protected void doGet(Object obj, String prop, HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = getWriter(response);
		try {
			if (prop==null)
				out.write(obj.toString());
			else {
				Object result;
				Method m=ReflectionUtil.getMethod(obj.getClass(), "get"+prop, (Class<?>[]) null);
				if (m!=null)
					result=ReflectionUtil.invoke(obj, m, null);
				else {
					Field f=ReflectionUtil.getField(obj.getClass(), prop);
					if (f!=null) {
						try {
							result=f.get(obj);
						}
						catch (IllegalArgumentException e) { throw new RuntimeException(e); }
						catch (IllegalAccessException e) { throw new RuntimeException(e); }
					}
					else
						throw new RuntimeException("Unknown field "+prop+" of Object "+obj);
				}
				out.write(result.toString());
			}
		}
		finally { out.close(); }
	}

	protected void doPost(Object obj, String prop, HttpServletRequest request, HttpServletResponse response) {
		throw new RuntimeException("not implemented yet");
	}

	protected void doPut(Object obj, String prop, HttpServletRequest request, HttpServletResponse response) {
		throw new RuntimeException("not implemented yet");
	}

	protected void doDelete(Object obj, String prop, HttpServletRequest request, HttpServletResponse response) {
		throw new RuntimeException("not implemented yet");
	}

	abstract protected Object lookup(String id);

}
