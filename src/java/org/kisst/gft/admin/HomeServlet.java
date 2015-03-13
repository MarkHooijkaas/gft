package org.kisst.gft.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.status.StatusItem;

public class HomeServlet extends TemplateServlet {
	private final ArrayList<StatusItem> statusItems;

	public HomeServlet(GftContainer gft, ArrayList<StatusItem> statusItems) { 
		super(gft);
		this.statusItems=statusItems;
	}

	@Override protected void addContext(HashMap<String, Object> root) {
		root.put("channels", gft.channels);
		//root.put("actions", gft.actions);
		root.put("listeners", gft.listeners);
		root.put("pollers", gft.pollers);
		root.put("modules", gft.getModuleInfo());
		root.put("statusItems", statusItems);
		for (StatusItem item :statusItems)
			item.refresh();
	}

}
