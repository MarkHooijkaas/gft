package org.kisst.jms;

import javax.jms.QueueConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.kisst.props4j.Props;

public class ActiveMqSystem extends JmsSystem {
	public ActiveMqSystem(Props props) { super(props); }

	@Override public QueueConnectionFactory createConnectionFactory() {
		String user=props.getString("user",null);
		String password=props.getString("password",null);
		String url=props.getString("url");
		return  new ActiveMQConnectionFactory(user, password, url);
	}
	
	
	
}
