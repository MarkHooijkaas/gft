package org.kisst.gft.admin;

import org.kisst.http4j.server.HttpServer;
import org.kisst.props4j.Props;
import org.kisst.servlet4j.AbstractServlet;
import org.kisst.servlet4j.ServletContainer;

public class AdminServer extends HttpServer {
	private final ServletContainer servlets;


	public AdminServer(Props props) {
		super(props.getProps("http"), new ServletContainer());
		this.servlets= (ServletContainer) handler;
	}

	public void addServlet(String url, AbstractServlet servlet) {
		servlets.addServlet(url, servlet);
	}
}
