package org.kisst.gft;

import org.kisst.util.XmlNode;

public class XmlRelatedException extends DetailedException {
	private static final long serialVersionUID = 1L;
	private final XmlNode xml;

	public XmlRelatedException(String msg, XmlNode xml) { super(msg,null); this.xml=xml;}
	public XmlRelatedException(String msg, Throwable err, XmlNode xml) { super(msg, err,null); this.xml=xml;}
	
	public XmlNode getEmbeddedXml() { return xml; } 
	
	@Override public String getDetails() {
		String result="extra detail, related XML:"+xml;
		if (xml.getRoot()!=xml)
			result=result+"\nextra detail, related XML root element:"+xml.getRoot();
		return result;
	} 

}
