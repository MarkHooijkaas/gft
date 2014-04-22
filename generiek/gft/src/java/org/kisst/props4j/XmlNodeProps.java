package org.kisst.props4j;

import java.util.HashSet;

import org.kisst.util.XmlNode;

public class XmlNodeProps extends PropsBase {
	private final XmlNode node;

	public XmlNodeProps(XmlNode node) {
		this.node = node;
	}

	@Override public Props getParent() { return null;	} // TODO

	@Override
	public Object get(String key, Object defaultValue) {
		key=key.replace('.', '/');
		XmlNode result=node.getChild(key);
		if (result==null)
			return defaultValue;
		if (result.getChildren().size()==0)
			return result.getText();
		return new XmlNodeProps(result);
	}

	public Iterable<String> keys() {
		HashSet<String> result= new HashSet<String>();
		for (XmlNode child: node.getChildren())
			result.add(child.getName());
		return result; 
	}

}
