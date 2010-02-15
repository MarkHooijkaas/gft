package org.kisst.gft.admin.rest;

import org.kisst.cfg4j.SimpleProps;

public class PropsResource extends Resource {
	private final SimpleProps props;
	public PropsResource(SimpleProps props) { this.props=props;	}
	@Override protected Object getObject(String name) { return props.get(name); }
}
