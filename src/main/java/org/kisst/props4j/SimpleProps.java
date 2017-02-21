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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kisst.util.FileUtil;
import org.kisst.util.IndentUtil;
import org.kisst.util.IndentUtil.Indentable;
import org.kisst.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProps extends PropsBase implements IndentUtil.Indentable {
	private static final Logger logger = LoggerFactory.getLogger(SimpleProps.class);
	//private static final long serialVersionUID = 1L;

	public final String desc;
	private final SimpleProps parent;
	private final String name; 
	private final Map<String, Object> values=new LinkedHashMap<String, Object>();
	public String stringValue=null;
	
	public SimpleProps() { this(null,null); }
	public SimpleProps(String name) { this(null,name); }
	public SimpleProps(File file) { 
		this.parent=null; 
		this.name=null;
		this.desc="file:"+file.getAbsolutePath(); 
		load(file); 
	}

	public SimpleProps(SimpleProps parent, String name) {
		this.parent=parent;
		if (name!=null && name.lastIndexOf(".")>0)
			this.name=name.substring(name.lastIndexOf(".")+1);
		else
			this.name=name;
		this.desc=getFullName();
	}
	@Override public Props getParent() { return parent; }
	@Override public String getLocalName() { return name; }
	@Override public String getFullName() {
		if (parent==null)
			return name;
		else { 
			String prefix=parent.getFullName();
			if (prefix==null)
				return name;
			else
				return prefix+"."+name;
		}
	}

	public SimpleProps shallowClone() {
		SimpleProps result=new SimpleProps(parent,name);
		for (String key: keys())
			result.put(key, this.get(key));
		return result;
	}

	@Override public Iterable<String> keys() { return values.keySet(); }
	@Override public int nrofKeys() { return values.keySet().size();}

	public SimpleProps put(String key, Object value) {
		int pos=key.indexOf('.');
		if (pos<0) {
			if (value==null) {
				if (logger.isInfoEnabled())
					logger.info("removing {}",getFullName()+"."+key);
				values.remove(key);
			}
			else {
				if (logger.isInfoEnabled())
					logger.info("put {} = {}",getFullName()+"."+key,value);
				Object old=values.get(key);
				if (old instanceof SimpleProps && value instanceof String)
					((SimpleProps) old).stringValue=(String) value;
				else
					values.put(key, value);
			}
			return this;
		}
		String keystart=key.substring(0,pos);
		String keyremainder=key.substring(pos+1);
		Object o=values.get(keystart);
		if (o==null || o instanceof String) {
			SimpleProps props=new SimpleProps(this,keystart);
			values.put(keystart, props);
			props.put(keyremainder, value);
			if (o instanceof String)
				props.stringValue= (String) o;
		}
		else if (o instanceof SimpleProps)
			((SimpleProps)o).put(keyremainder, value);
		else
			throw new RuntimeException("key "+getFullName()+"."+key+" already has value "+o+" when adding subkey "+keyremainder);
		return this;
	}

	// TODO: code duplication with function above
	SimpleProps getParentForKeyWithCreate(String key) {
		int pos=key.indexOf('.');
		if (pos<0)
			return this;
		String keystart=key.substring(0,pos);
		String keyremainder=key.substring(pos+1);
		Object o=values.get(keystart);
		if (o==null || o instanceof String) {
			SimpleProps props=new SimpleProps(this,keystart);
			values.put(keystart, props);
			if (o instanceof String)
				props.stringValue= (String) o;
			return props.getParentForKeyWithCreate(keyremainder);
		}
		else if (o instanceof SimpleProps)
			return ((SimpleProps)o).getParentForKeyWithCreate(keyremainder);
		else
			throw new RuntimeException("key "+getFullName()+"."+key+" already has value "+o+" when adding subkey "+keyremainder);

	}
	
	public Object get(String key, Object defaultValue) {
		logger.debug("getting {}",key);
		int pos=key.indexOf('.');
		if (pos<0) {
			Object result=values.get(key);
			if (logger.isInfoEnabled())
				logger.info("returned prop {} with value {}",getFullName()+"."+key,result);
			if (result==null)
				return defaultValue;
			else
				return result;
		}
		String keystart=key.substring(0,pos);
		String keyremainder=key.substring(pos+1);
		Object o=values.get(keystart);
		if (o==null)
			return defaultValue;
		else if (o instanceof SimpleProps)
			return ((SimpleProps)o).get(keyremainder,defaultValue);
		else
			return defaultValue;
	}


	public void load(String filename)  { load(new File(filename));	}
	public void load(File file) {
		InputStreamReader f = new InputStreamReader(FileUtil.open(file));
		try {
			new Parser(f, file).fillMap(this);
		}
		finally {
			try {
				f.close();
			} catch (IOException e) { throw new RuntimeException(e); }
		}
	}
	public void read(InputStream inp)  { new Parser(inp).fillMap(this);} 


	public String toString() { return "SimpleProps("+this.desc+")";	}
	public String toIndentedString() { return toIndentedString("");	}
	public String toIndentedString(String indent) {
		StringBuilder result=new StringBuilder("{\n");
		for (String key: values.keySet()) {
			result.append(indent+"\t"+key+": ");
			Object o=values.get(key);
			if (o instanceof Indentable)
				result.append(((Indentable)o).toIndentedString(indent+"\t"));
			else if (o instanceof String)
				result.append(StringUtil.doubleQuotedString((String)o)+";");
			else if (o instanceof File)
				result.append("@file("+o.toString()+")");
			else
				result.append(o.toString());
			result.append("\n");
		}
		result.append(indent+"}");
		return result.toString();
	}
	public String toPropertiesString() {
		StringBuilder result=new StringBuilder();
		for (String key: values.keySet()) {
			Object o=values.get(key);
			if (o instanceof SimpleProps) {
				result.append(((SimpleProps)o).toPropertiesString());
				continue;
			}
			result.append(getFullName()+"."+key+"=");
			if (o instanceof String)
				result.append(StringUtil.doubleQuotedString((String)o)+"\n");
			else if (o instanceof File)
				result.append("@file("+o.toString()+")\n");
			else
				result.append(o.toString()+"\n");
		}
		return result.toString();
	}
}
