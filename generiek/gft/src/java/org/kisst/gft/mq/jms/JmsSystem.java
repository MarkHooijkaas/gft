package org.kisst.gft.mq.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.MqSystem;

public class JmsSystem implements MqSystem {
	private final Props props;
	private final Connection connection;
	
	public JmsSystem(Props props) {
		this.props=props;
		String user=props.getString("user",null);
		String password=props.getString("password",null);
		String url=props.getString("url");
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
			connection = connectionFactory.createConnection();
			connection.start();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}

	public MqQueue getQueue(String name) { return new JmsQueue(this, props.getProps("queue."+name)); }
	public Connection getConnection() { return connection;	}
	public void close() {
		try {
			connection.close();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}
}
