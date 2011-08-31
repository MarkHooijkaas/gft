package org.kisst.gft.admin.rest;

import java.util.Map;

import org.kisst.props4j.SimpleProps;


public class Resource {
	protected Object get(String path) {
		if (path.startsWith("/"))
			path=path.substring(1);
		int pos=path.indexOf('/');
		if (pos<0)
			return getObject(path);
		String key=path.substring(0,pos);
		String remainder=path.substring(pos+1);
		return wrap(getObject(key)).get(remainder);

	
	}

	protected Object getObject(String name) { throw new RuntimeException("not implemented yet"); }
	
	protected Object post(String path, Object value) { throw new RuntimeException("not implemented yet"); }
	protected Object put(String path, Object value) { throw new RuntimeException("not implemented yet"); }
	protected Object delete(String path) { throw new RuntimeException("not implemented yet"); }
	
	@SuppressWarnings("unchecked")
	protected Resource wrap(Object obj) {
		if (obj instanceof Map)
			return new MappedResource((Map<String, ?>) obj);
		else if (obj instanceof SimpleProps)
			return new PropsResource((SimpleProps) obj);
		else 
			return new ObjectResource(obj);
	}
}
