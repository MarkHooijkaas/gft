package org.kisst.gft.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;

public abstract class BaseServlet {
	protected final GftContainer gft;
	public BaseServlet(GftContainer gft) {
		this.gft=gft;
	}
 
	abstract public void handle(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException;
}
