package org.kisst.gft.mq;

import java.util.List;

public interface MqQueue {
	public String getName();
	public int size();
	
	public MqMessage getOneMessage();
	public List<MqMessage> getAllMessages();
	public List<MqMessage> getSomeMessages();
}
