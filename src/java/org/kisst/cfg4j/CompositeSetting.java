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

import java.util.ArrayList;
import java.util.HashMap;

public class CompositeSetting extends Setting {
	private final ArrayList<Setting> settings=new ArrayList<Setting>();
	private final HashMap<String, DefaultSpecification > defaults = new HashMap<String, DefaultSpecification>();

	private static final DefaultSpecification[] emptyDefautlSpecs=new DefaultSpecification[0];
	public CompositeSetting (String name) {	this(null, name); }
	public CompositeSetting(CompositeSetting parent, String name) { this(parent,name,emptyDefautlSpecs); }
	public CompositeSetting(CompositeSetting parent, String name, DefaultSpecification... otherDefaultValues) {
		super(parent, name);
		for (DefaultSpecification dv : otherDefaultValues) { 
			if (dv.setting.parent!=null && dv.setting.parent.getClass()!=this.getClass())
				throw new RuntimeException("Class of parent of changed default setting "+dv.setting+" is different from class composite setting "+this);
			defaults.put(dv.setting.name, dv);
		}
	}

	public void add(Setting s) { settings.add(s); }
	
	public DefaultSpecification getDefaultSpecification(StringBasedSetting setting) {
		DefaultSpecification dv = defaults.get(setting.name);
		if (dv==null)
			return null;
		if (dv.setting.getClass()!=setting.getClass())
			throw new RuntimeException("Class of changed default setting "+dv.setting+" is different from class of setting "+setting);
		return dv;
	}

	public boolean hasDefaultSpecification(Setting setting) {
		if (! (setting instanceof StringBasedSetting))
			return false;
		return getDefaultSpecification((StringBasedSetting) setting)!=null;
	}
	
	
}
