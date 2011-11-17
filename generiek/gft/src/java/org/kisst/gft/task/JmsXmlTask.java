package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;
import org.kisst.util.XmlNode;


public class JmsXmlTask extends BasicTask implements SoapTask {
	private final JmsMessage msg;
	private final XmlNode message;
	private final XmlNode content; 		
	
	public JmsXmlTask(GftContainer gft, TaskDefinition taskdef, JmsMessage msg) {
		super(gft, taskdef);
		this.msg=msg;
		this.message=new XmlNode(msg.getData());
		this.content=message.getChild("Body").getChildren().get(0);
	}

	public JmsMessage getJmsMessage() { return msg; } 
	@Override public XmlNode getContent() { return content; }
	@Override public XmlNode getMessage() { return message;}
}
