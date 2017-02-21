package org.kisst.gft.admin;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.servlet4j.AbstractServlet;



public abstract class BaseServlet extends AbstractServlet {
	protected final GftWrapper wrapper;
	public BaseServlet(GftWrapper wrapper) {
		super(wrapper.getProps());
		this.wrapper=wrapper;
	}
}
