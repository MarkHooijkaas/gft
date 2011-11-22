package org.kisst.gft.task;

import org.kisst.util.XmlNode;


public interface SoapTask extends Task {
	public XmlNode getMessage();
	public XmlNode getContent(); 		
}
