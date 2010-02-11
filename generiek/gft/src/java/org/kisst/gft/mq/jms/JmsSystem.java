package org.kisst.gft.mq.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.MqSystem;

public abstract class JmsSystem implements MqSystem {
	protected final Props props;
	private final Connection connection;
	
	public JmsSystem(Props props) {
		this.props=props;
		try {
			ConnectionFactory connectionFactory = createConnectionFactory();
			String username=props.getString("username", null);
			String password=props.getString("password", null);
			if (username==null)
				connection = connectionFactory.createConnection();
			else
				connection = connectionFactory.createConnection(username, password);
			connection.start();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}

	abstract protected ConnectionFactory createConnectionFactory();
	
	public MqQueue getQueue(String name) { return new JmsQueue(this, props.getProps("queue."+name)); }
	public Connection getConnection() { return connection;	}
	public void close() {
		try {
			connection.close();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}
}
