package org.kisst.gft.admin;

import org.kisst.gft.GftWrapper;

import java.util.HashMap;

public class HomeServlet extends TemplateServlet {


	public HomeServlet(GftWrapper wrapper) {
		super(wrapper);
	}


	@Override protected void addContext(HashMap<String, Object> root) {
		wrapper.addContext(root);
	}

}
