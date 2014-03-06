package org.kisst.gft;

import org.kisst.util.XmlNode;
import org.kisst.util.exception.HasDetails;

public class XmlRelatedException extends RuntimeException implements HasDetails {
	private static final long serialVersionUID = 1L;
	private final XmlNode xml;

	public XmlRelatedException(String msg, XmlNode xml) { super(msg); this.xml=xml;}
	public XmlRelatedException(String msg, Throwable err, XmlNode xml) { super(msg, err); this.xml=xml;}
	
	public XmlNode getEmbeddedXml() { return xml; } 
	
	@Override public String getDetails() {
		String result="extra detail, related XML:"+xml;
		if (xml.getRoot()!=xml)
			result=result+"\nextra detail, related XML root element:"+xml.getRoot();
		return result;
	} 

}
