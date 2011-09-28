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

package org.kisst.props4j;



public interface Props extends MinimalProps {
	public String getLocalName();
	//public String getFullName();
	
	//public Object get(String key, Object defaultValue);
	public String getString(String key, String defaultValue);
	public int    getInt(String string, int defaultValue);
	public long   getLong(String string, long defaultValue);
	public boolean getBoolean(String name, boolean defaultValue);
	public Props getProps(String name);
	//public Iterable<String> keys();
	
	public Object get(String key);
	public String getString(String key);
	public int    getInt(String string);
	public long   getLong(String string);
	public boolean getBoolean(String name);
	public Sequence getSequence(String name);
}
