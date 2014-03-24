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

import org.kisst.props4j.Props;

public class StringBasedSetting extends Setting {
	private final String defaultValue;

	protected StringBasedSetting(CompositeSetting parent, String name) {
		super(parent, name, true);
		this.defaultValue=null;
	}

	protected StringBasedSetting(CompositeSetting parent, String name, String defaultValue) {
		super(parent, name);
		if (parent==null)
			this.defaultValue=defaultValue;
		else {
			DefaultSpecification dv = parent.getDefaultSpecification(this);
			if (dv==null)
				this.defaultValue=defaultValue;
			else
				this.defaultValue=dv.defaultValue;
		}
	}
	
	protected static String convertToString(Object o){
		if (o==null)
			return null;
		else
			return o.toString();
	}
	public String getStringValue(Props props) {
		String result = props.getString(fullName, null);
		if (result!=null)
			return result;
		if (isRequired())
			throw new RuntimeException("config value "+fullName+" is required but missing in properties "+props.getFullName());
		else
			return defaultValue;
	}
}
