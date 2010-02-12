package org.kisst.gft.mq.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.kisst.cfg4j.Props;

public class ActiveMqSystem extends JmsSystem {
	public ActiveMqSystem(Props props) { super(props); }

	@Override protected ConnectionFactory createConnectionFactory() {
		String user=props.getString("user",null);
		String password=props.getString("password",null);
		String url=props.getString("url");
		return  new ActiveMQConnectionFactory(user, password, url);
	}
	
	
	
}
