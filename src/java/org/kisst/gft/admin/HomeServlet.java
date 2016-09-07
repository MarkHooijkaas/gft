package org.kisst.gft.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.gft.admin.status.StatusItem;
import org.kisst.jms.MultiListener;
import org.kisst.util.JarLoader;

public class HomeServlet extends TemplateServlet {


	public HomeServlet(GftWrapper wrapper) {
		super(wrapper);
	}


	@Override protected void addContext(HashMap<String, Object> root) {
		wrapper.addContext(root);
	}

}
