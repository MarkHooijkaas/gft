package org.kisst.gft.admin;

import org.kisst.gft.GftWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.eclipse.jgit.lib.ObjectChecker.tag;

public class HomeServlet extends TemplateServlet {


	public HomeServlet(GftWrapper wrapper) {
		super(wrapper);
	}


	@Override protected void addContext(HttpServletRequest request, HashMap<String, Object> root) {
		String tag=request.getParameter("tag");
		if (tag==null)
			tag="*";
		root.put("tag", tag.trim());
		System.out.println("looging for tag["+tag+"]");
		//root.put("tags", wrapper.getCurrentGft().tags);
		wrapper.addContext(root);
	}

}
