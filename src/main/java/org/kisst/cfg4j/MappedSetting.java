/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.cfg4j;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.kisst.util.ReflectionUtil;
import org.kisst.props4j.Props;


public class MappedSetting<T extends Setting> extends CompositeSetting {
	// TODO: This is a cache, but it is reset-proof
	private final HashMap<String,T> items=new HashMap<String,T>();
	private final Constructor<?> constructor;
	private final String defaultValue;
	
	public MappedSetting(CompositeSetting parent, String name, Class<?> clazz) {
		this(parent, name, clazz, null);
	}
	public MappedSetting(CompositeSetting parent, String name, Class<?> clazz, String defaultValue) { 
		super(parent, name);
		try {
			constructor=clazz.getConstructor(new Class[] {CompositeSetting.class, String.class});
		} catch (NoSuchMethodException e) { throw new RuntimeException(e); }
		this.defaultValue=defaultValue;
	}

	public String get(Props props) { return props.getString(fullName, defaultValue);  }

	@SuppressWarnings("unchecked")
	public T get(String name) {
		T result=items.get(name);
		if (result==null) {
			result= (T) ReflectionUtil.createObject(constructor, new Object[] {this, name});
			items.put(name, result);
		}
		return  result;
	}
	
	public Iterable<String> keys(Props props) { return props.getProps(name).keys();}
	
	public void reset() { items.clear(); }
}
