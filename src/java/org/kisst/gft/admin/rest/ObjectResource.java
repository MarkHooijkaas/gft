package org.kisst.gft.admin.rest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.kisst.util.ReflectionUtil;

public class ObjectResource extends Resource {
	private final Object obj;

	public ObjectResource(Object obj) {
		this.obj=obj;
	}

	@Override protected Object get(String path) { 
		if (path.startsWith("/"))
			path=path.substring(1);
		String prop=path;
		int pos=path.indexOf('/');
		if (pos>=0)
			prop=path.substring(0,pos);
		Object result;
		Method m=ReflectionUtil.getMethod(obj.getClass(), "get"+prop, (Class<?>[]) null);
		if (m!=null)
			result=ReflectionUtil.invoke(obj, m, null);
		else {
			Field f=ReflectionUtil.getField(obj.getClass(), prop);
			if (f!=null) {
				try {
					result=f.get(obj);
				}
				catch (IllegalArgumentException e) { throw new RuntimeException(e); }
				catch (IllegalAccessException e) { throw new RuntimeException(e); }
			}
			else
				throw new RuntimeException("Unknown field "+prop+" of Object "+obj);
		}
		if (pos>=0) {
			String remainder=path.substring(pos+1);
			return wrap(result).get(remainder);
		}
		else
			return result;
	}

}

