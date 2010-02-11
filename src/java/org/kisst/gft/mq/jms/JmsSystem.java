package org.kisst.gft.mq.jms;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.QueueListener;
import org.kisst.gft.mq.QueueSystem;

public class JmsSystem implements QueueSystem {
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
	public QueueListener createListener(Props props) { return new JmsListener(this,props); }

	protected ConnectionFactory createConnectionFactory() {
        Hashtable<String, String> env= new Hashtable<String,String>();
        env.put( "java.naming.factory.initial", "com.sun.jndi.fscontext.RefFSContextFactory" );
        env.put( "java.naming.provider.url", props.getString("jndifile"));
        env.put( "java.naming.security.authentication", "none" );
        if( !"none".equals( env.get("java.naming.security.authentication"))) {
            env.put( "java.naming.security.principal", props.getString("username",null));
            env.put( "java.naming.security.credentials", props.getString("password",null));
        }
        
		InitialContext jndiContext;
		try {
			String name=props.getString("name");
			jndiContext = new InitialContext( env);
			System.out.println("Looking up "+name);
			return (ConnectionFactory) jndiContext.lookup( name );
		} catch (NamingException e) { throw new RuntimeException(e); }

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
