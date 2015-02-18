package org.kisst.gft.admin;

import org.kisst.gft.GftContainer;
import org.kisst.servlet4j.AbstractServlet;



public abstract class BaseServlet extends AbstractServlet {
	protected final GftContainer gft;
	public BaseServlet(GftContainer gft) {
		super(gft.props);
		this.gft=gft;
	}
}
