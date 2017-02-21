package org.kisst.http4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.kisst.props4j.Props;

public class BasicHttpHostMap implements HttpHostMap {
	private final HashMap<String, HttpHost> httphosts= new LinkedHashMap<String, HttpHost>();

	public BasicHttpHostMap(Props props) {
		if (props==null)
			return;
		for (String name: props.keys())
			httphosts.put(name, new HttpHost(props.getProps(name)));
	}
	
	@Override public HttpHost getHttpHost(String name) { return httphosts.get(name); }
	@Override public Set<String> getHttpHostNames() { return httphosts.keySet(); }
}
