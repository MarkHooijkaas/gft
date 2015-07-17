package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;
import org.kisst.util.XmlNode;


public class JmsXmlTask extends JmsTask implements SoapTask {
	private final XmlNode content; 		
	
	public JmsXmlTask(GftContainer gft, TaskDefinition taskdef, JmsMessage msg, XmlNode content, String xmlPath) {
		super(gft, taskdef, content.getChildText(xmlPath), msg);
		this.content=content;
	}

	public static XmlNode getContent(JmsMessage msg) { return new XmlNode(msg.getData()).getChild("Body").getChildren().get(0); }

	@Override public XmlNode getContent() { return content; }
	@Override public XmlNode getMessage() { return content.getRoot();}
}
