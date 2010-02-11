package org.kisst.gft.mq;

import org.kisst.cfg4j.Props;


public interface QueueSystem {
	public MqQueue  getQueue(String name);
	public QueueListener  createListener(Props props);

}
