package org.kisst.gft.admin;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.props4j.Props;
import org.kisst.servlet4j.ServletContainer;

public class AdminServer extends ServletContainer {
	public AdminServer(GftWrapper wrapper, Props props) {
		super(props);
	}
}
