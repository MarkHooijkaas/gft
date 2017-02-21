package org.kisst.gft.admin;

import org.kisst.gft.GftWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class HomeServlet extends TemplateServlet {


	public HomeServlet(GftWrapper wrapper) {
		super(wrapper);
	}


	@Override protected void addContext(HttpServletRequest request, HashMap<String, Object> root) {
		String tag=request.getParameter("tag");
		if (tag==null)
			tag="*";
		root.put("tag", tag.trim());
		//root.put("tags", wrapper.getCurrentGft().tags);
		wrapper.addContext(root);
	}

}
