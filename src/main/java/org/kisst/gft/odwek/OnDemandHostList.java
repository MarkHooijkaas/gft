package org.kisst.gft.odwek;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.kisst.props4j.Props;

public class OnDemandHostList {
	private static OnDemandHostList globalList=null;
	public static void init(Props props) { globalList=new OnDemandHostList(props); }
	public static OnDemandHost getHost(String name) { return globalList.getOnDemandHost(name); }
	
	private final HashMap<String, OnDemandHost> hosts= new LinkedHashMap<String, OnDemandHost>();

	public OnDemandHostList(Props props) {
		for (String name: props.keys()) {
			Props p=props.getProps(name);
			hosts.put(name, new OnDemandHost(p));
		}
	}

	public OnDemandHost getOnDemandHost(String name) {
		return hosts.get(name);
	}
}
