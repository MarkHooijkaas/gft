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



public abstract class PropsBase implements Props {
	private static final long serialVersionUID = 1L;
	abstract public Object get(String key, Object defaultValue);

	public String getString(String key) { return (String) get(key); }
	public int getInt(String key) { return Integer.parseInt(getString(key)); }
	public long getLong(String key) { return Long.parseLong(getString(key)); }

	public Object get(String key) {
		Object result=get(key, null);
		if (result!=null)
			return result;
		else
			throw new RuntimeException("Could not find property "+key);
	}

	public String getString(String key, String defaultValue) {
		return (String) get(key,defaultValue);
	}

	public int getInt(String key, int defaultValue) {
		String s=getString(key,null);
		if (s==null)
			return defaultValue;
		else
			return Integer.parseInt(s);
	}

	public long getLong(String key, long defaultValue) {
		String s=getString(key,null);
		if (s==null)
			return defaultValue;
		else
			return Long.parseLong(s);
	}

	public boolean getBoolean(String name, boolean defaultValue) {
		String value=getString(name, null);
		if (value==null)
			 return defaultValue;
		else 
			return getBoolean(name);
	}

	public boolean getBoolean(String name) {
		String value=getString(name, null);
		if (value==null)
			 throw new RuntimeException("property "+name+" is not optional");
		if ("true".equals(value))
			return true;
		else if ("false".equals(value))
			return false;
		else
			throw new RuntimeException("property "+name+" should be true or false, not "+value);
	}

}
