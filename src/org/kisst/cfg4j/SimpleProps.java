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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;


public class SimpleProps extends PropsBase {
	private static final long serialVersionUID = 1L;

	private final HashMap<String, Object> map=new HashMap<String, Object>();
	public void put(String key, String value) {
		map.put(key,value);
	}

	public Object get(String key, Object defaultValue) {
		Object result=map.get(key);
		if (result!=null)
			return result;
		else
			return defaultValue;
	}
	
	public void load(String filename)  {
		FileInputStream inp = null;
		try {
			try {
				inp = new FileInputStream(filename);
				load(new BufferedReader(new InputStreamReader(inp)));
			}
			finally {
				if (inp!=null) inp.close();
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

	private void load(BufferedReader input)  {
		try {
			String str;
			while ((str = input.readLine()) != null) {
				str=str.trim();
				if (str.startsWith("#") || str.length()==0) {
					//ignore comments and empty lines
				}
				else {
					int pos=str.indexOf('=');
					if (pos<0)
						throw new RuntimeException("props line should contain = sign "+str);
					String key=str.substring(0,pos).trim();
					String value=str.substring(pos+1).trim();
					map.put(key,value);
				}
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

}
