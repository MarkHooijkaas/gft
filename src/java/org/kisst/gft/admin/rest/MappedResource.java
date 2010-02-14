package org.kisst.gft.admin.rest;

import java.util.Map;

public class MappedResource extends Resource {
	private final Map<String, ?> map;

	public MappedResource(Map<String, ?> map) {
		this.map=map;
	}

	@Override protected Object lookup(String id) { return map.get(id); }
}
