package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;


public class JmsTask extends BasicTask implements TextTask {
	private final JmsMessage msg;
	
	public JmsTask(GftContainer gft, TaskDefinition taskdef, JmsMessage msg) {
		super(gft, taskdef);
		this.msg=msg;
	}

	public String toString() {	return toString(msg==null ? "" : msg.getMessageId());}
	
	public JmsMessage getJmsMessage() { return msg; }

	@Override
	public String getText() { return getJmsMessage().getData();} 
	
	@Override public String getIdentification() { return "msgid:"+msg.getMessageId(); }

}
