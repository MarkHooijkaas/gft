/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the Caas tool.

The Caas tool is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Caas tool is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Caas tool.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlNode {
	public final Element element;
	
	public XmlNode(Element element) { this.element = element; }
	public XmlNode(String name, String namespace) {
		this.element=new Element(name, Namespace.getNamespace(namespace));
	}
	public XmlNode(String xml) {
		if (xml.indexOf("<")<0) {
			//xml is just an element name
			this.element=new Element(xml);
			return;
		}
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(new StringReader(xml));
		}
		catch (JDOMException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		this.element =doc.getRootElement();

	}
	
	@Override public XmlNode clone() { return new XmlNode((Element) this.element.clone()); }
	public String getName() {  return element.getName(); }
	public String getNamespace() {  return element.getNamespace().getURI(); }
	public String getText() { return element.getText(); }
	public String getAttribute(String name) { return getAttribute(name,null);}
	public String getAttribute(String name, String defaultValue) {
		Attribute attr = element.getAttribute(name);
		if (attr==null)
			return defaultValue;
		else
			return attr.getValue(); 
	}
	public XmlNode getChild(String path) { return (XmlNode) get(path); }
	public XmlNode getParent() { return new XmlNode(element.getParentElement()); }
	public Object  propertyMissing(String name) { return get(name); }
	public String getChildText(String path) {
		Object child=get(path);
		if (child==null)
			return null;
		if (child instanceof String)
			return (String) child;
		return ((XmlNode) child).getText();
	}
	
	public void setPrefix(String prefix) {
		Namespace ns = element.getNamespace();
		Namespace ns2 = Namespace.getNamespace(prefix, ns.getURI());
		element.setNamespace(ns2);
	}
	
	public void setChildText(String path, String value) {
		if (path.indexOf("@")>=0)
			throw new RuntimeException("changing an attribute not yet supported in path "+path);
		Object child=get(path);
		((XmlNode) child).setText(value);
	}

	public List<XmlNode> getChildren() {
		List<?> l = element.getChildren();
		ArrayList<XmlNode> result=new ArrayList<XmlNode>(l.size());
		for (Object o: l)
			result.add(new XmlNode((Element) o));
		return result;
	}
	public List<XmlNode> getChildren(String name) {
		List<?> l = element.getChildren(name, null);
		ArrayList<XmlNode> result=new ArrayList<XmlNode>(l.size());
		for (Object o: l)
			result.add(new XmlNode((Element) o));
		return result;
	}
	
	public Object get(String path) {
		String[] parts=path.split("/");
		Element e=element;
		for (String part:parts) {
			boolean optional=false;
			if (part.startsWith("?")) {
				part=part.substring(1);
				optional=true;
			}
			if (part.equals(".."))
				e=e.getParentElement();
			else if (part.equals("text()"))
				return e.getText();
			else if (part.startsWith("@"))
				return e.getAttribute(part.substring(1)).getValue();
			else
				e=e.getChild(part,null);
			if (e==null && optional)
				return null;
			if (e==null)
				throw new RuntimeException("Could not find non-optional element "+part+" in path "+path);
		}
		return new XmlNode(e);
	}

	public String compact() {
		XMLOutputter out=new XMLOutputter(Format.getCompactFormat());
		return out.outputString(element);
	}
	@Override public String toString() {
		XMLOutputter out=new XMLOutputter();
		return out.outputString(element);
	}
	public String shortString(int maxlen) {
		String xml= toString();
		if (xml.length()<=maxlen) 
			return xml;
		return "<"+getName()+">...<"+getName()+">";
	}

	public XmlNode setAttribute(String name, String value) {
		element.setAttribute(name, value);
		return this;
	}
	public XmlNode add(String name, String namespace) {
		Element child=new Element(name, namespace);
		element.addContent(child);
		return new XmlNode(child);
	}
	public XmlNode add(String name) { 
		Element child=new Element(name, element.getNamespace());
		element.addContent(child);
		return new XmlNode(child);
	}
	public XmlNode detach() { this.element.detach(); return this; }
	public void setText(String text) { element.setText(text); }
	public void add(XmlNode node) { element.addContent(node.element); }
	public void remove(XmlNode e) { element.getChildren().remove(e.element); }
	public String getPretty() {
		XMLOutputter out=new XMLOutputter();
		out.setFormat(Format.getPrettyFormat());
		String xml= out.outputString(element);
		return xml;
	}
	public void save(String filename) { FileUtil.saveString(new File(filename), getPretty()); }

	public XmlNode getRoot() {
		Element e = element;
		while (e.getParentElement()!=null)
			e=e.getParentElement();
		if (e==element)
			return this;
		else
			return new XmlNode(e);
	}
}

