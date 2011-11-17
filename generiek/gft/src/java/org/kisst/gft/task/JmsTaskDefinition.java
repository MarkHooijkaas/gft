package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsMessage;
import org.kisst.props4j.Props;

public abstract class JmsTaskDefinition extends BasicTaskDefinition {
	public JmsTaskDefinition(GftContainer gft, Props props) {
		super(gft, props, null);
	}

	abstract public Task createNewTask(JmsMessage msg);
}