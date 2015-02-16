package org.kisst.gft.admin;

import java.util.HashMap;

import org.kisst.gft.GftContainer;
import org.kisst.gft.StatusItem;

public class HomeServlet extends TemplateServlet {
	public HomeServlet(GftContainer gft) { super(gft); }

	@Override protected void addContext(HashMap<String, Object> root) {
		root.put("channels", gft.channels);
		//root.put("actions", gft.actions);
		root.put("listeners", gft.listeners);
		root.put("pollers", gft.pollers);
		root.put("modules", gft.getModuleInfo());
		for (StatusItem item: gft.statusItems)
			item.autoRefresh();
		root.put("statusItems", gft.statusItems);
	}

}
