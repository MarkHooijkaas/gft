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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MultiLevelProps {
	private final HashMap<String,SimpleProps> layers=new HashMap<String,SimpleProps>();
	private final SimpleProps globalProps=new SimpleProps();

	public MultiLevelProps(InputStream configStream) {
		load(configStream);
	}
	public Props getGlobalProps() { return globalProps; }
	public Props getProps(String key) {
		Props result = layers.get(key);
		if (result==null)
			return globalProps;
		else
			return result;
	}

	/*
	private void load(String filename)  {
		props.clear();
		FileInputStream inp = null;
		try {
			try {
				inp = new FileInputStream(filename);
				load(inp);
			}
			finally {
				if (inp!=null) inp.close();
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}
	*/

	private void load(InputStream inp)  {
		if (inp==null)
			return;
		load(globalProps, new BufferedReader(new InputStreamReader(inp)));
	}

	private void load(SimpleProps props, BufferedReader input)  {
		try {
			String str;
			while ((str = input.readLine()) != null) {
				str=str.trim();
				if (str.startsWith("#") || str.length()==0) {
					//ignore comments and empty lines
				}
				else if (str.equals("}")) 
					return;
				else if (str.startsWith("@")) {
					str=str.substring(1).trim();
					if (!str.endsWith("{"))
						throw new RuntimeException("layer should have { symbol on same line in config line: @"+str);
					String key=str.substring(0,str.length()-1).trim();
					// TODO: remove the following backward compatibility hack when all old config files are changed
					if (key.startsWith("override")) 
						key="method:"+key.substring(8).trim();
					SimpleProps layer=new SimpleProps();
					this.layers.put(key, layer);
					load(layer, input);
				}
				else {
					int pos=str.indexOf('=');
					if (pos<0)
						throw new RuntimeException("props line should contain = sign "+str);
					String key=str.substring(0,pos).trim();
					String value=str.substring(pos+1).trim();
					props.put(key,value);
				}
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}
}
