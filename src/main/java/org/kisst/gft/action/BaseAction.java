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

package org.kisst.gft.action;

import org.kisst.props4j.Props;
import org.kisst.util.ReflectionUtil;

public abstract class BaseAction implements Action {
	private final boolean safeToRetry;
	
	public BaseAction(Props props) {
		this.safeToRetry = props.getBoolean("safeToRetry", true);
	}
	public boolean safeToRetry() { return safeToRetry; }
	
	//@Override public String toString() { return this.getClass().getSimpleName()+"()";}
	@Override public String toString() { return ReflectionUtil.toString(this);}
}
