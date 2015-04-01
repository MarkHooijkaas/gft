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

import org.kisst.util.IndentUtil;

public class MultiProps extends PropsBase implements IndentUtil.Indentable {
	private final ArrayList<Props> propsList;

	public MultiProps() {
		this.propsList=new ArrayList<Props>();
	}
	public MultiProps(Props ... props) {
		this.propsList=new ArrayList<Props>(props.length);
		for (Props p:props)
			if (p!=null) propsList.add(p);
	}
	@Override public Props getParent() { return null;	}
	@Override public String getLocalName() { return propsList.get(0).getLocalName(); }
	@Override public String getFullName() { return propsList.get(0).getFullName(); }

	public Object get(String key, Object defaultValue) {
		for (Props p:propsList) {
			Object result=p.get(key, null);
			if (result!=null)
				return result;
		}
		return defaultValue;
	}

	public Object get(String key) {
		Object result=get(key, null);
		if (result!=null)
			return result;
		throw new RuntimeException("Could not find "+key+" in any of the properties "+this);
	}


	public Iterable<String> keys() {
		HashSet<String> result= new HashSet<String>();
		for (Props layer: propsList) {
			for (String key: layer.keys())
				result.add(key);
		}
		return result; 
	}

	public String toString() {
		StringBuilder result=new StringBuilder("MultiProps(");
		String sep="";
		for (Props layer: propsList) {
			result.append(layer.toString()).append(sep);
			sep=",";
		}
		result.append(")");
		return result.toString(); 
	}
	public String toIndentedString(String indent) {
		StringBuilder result=new StringBuilder();
		for (Props layer: propsList)
			result.append(indent+"#layer: "+layer+"\n").append(IndentUtil.toIndentedString(layer, indent)).append("\n");
		return result.toString(); 
	}
}
