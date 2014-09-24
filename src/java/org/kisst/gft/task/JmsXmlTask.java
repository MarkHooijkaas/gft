package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;
import org.kisst.util.XmlNode;


public class JmsXmlTask extends JmsTask implements SoapTask {
	private final XmlNode message;
	private final XmlNode content; 		
	
	public JmsXmlTask(GftContainer gft, TaskDefinition taskdef, JmsMessage msg) {
		super(gft, taskdef, msg);
		this.message=new XmlNode(msg.getData());
		this.content=message.getChild("Body").getChildren().get(0);
	}

	@Override public XmlNode getContent() { return content; }
	@Override public XmlNode getMessage() { return message;}
}
