package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;
import org.kisst.util.XmlNode;


public class JmsXmlTask extends JmsTask implements SoapTask {
	private final XmlNode content; 		
	
	public JmsXmlTask(GftContainer gft, TaskDefinition taskdef, String id, JmsMessage msg, XmlNode content) {
		super(gft, taskdef, id, msg);
		this.content=content;
	}

	public static XmlNode getContent(JmsMessage msg) { return new XmlNode(msg.getData()).getChild("Body").getChildren().get(0); }

	@Override public XmlNode getContent() { return content; }
	@Override public XmlNode getMessage() { return content.getRoot();}
}
