package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;


public abstract class JmsTask extends BasicTask implements TextTask {
	private final JmsMessage msg;
	
	public JmsTask(GftContainer gft, TaskDefinition taskdef, String id, JmsMessage msg) {
		super(gft, taskdef, id);
		this.msg=msg;
	}

	public String toString() {	return toString(msg==null ? "" : msg.getMessageId());}
	
	public JmsMessage getJmsMessage() { return msg; }

	@Override public String getText() { return getJmsMessage().getData();} 
}
