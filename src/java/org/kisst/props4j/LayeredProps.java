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

import java.util.ArrayList;
import java.util.HashSet;

public class LayeredProps extends PropsBase {
	private final Props global; // TODO: better way to have global as always last layer
	private final ArrayList<Props> layers = new ArrayList<Props>();

	public LayeredProps(Props global) {
		this.global=global;
	}
	public void addTopLayer(Props props) { if (props!=null) layers.add(0,props); }
	public void addLayer(Props props)    { if (props!=null) layers.add(props); }
	
	public Object get(String key, Object defaultValue) {
		for (Props p:layers) {
			Object result=p.get(key, null);
			if (result!=null)
				return result;
		}
		return global.get(key,defaultValue);
	}

	public Object get(String key) {
		Object result=get(key, null);
		if (result!=null)
			return result;
		else
			return global.get(key);
	}


	public Iterable<String> keys() {
		HashSet<String> result= new HashSet<String>();
		for (Props layer: layers) {
			for (String key: layer.keys())
				result.add(key);
		}
		for (String key: global.keys())
			result.add(key);
		return result; 
	}

	public String toString() {
		StringBuilder result=new StringBuilder();
		for (Props layer: layers)
			result.append(layer.toString()).append("\n");
		result.append(global.toString()).append("\n");
		return result.toString(); 
	}
}
