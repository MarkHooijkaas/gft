package org.kisst.jms;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kisst.cfg4j.Props;
import org.kisst.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsSystem {
	private final static Logger logger=LoggerFactory.getLogger(JmsSystem.class); 
	protected final Props props;
	private final QueueConnection connection;
	public final String sendParams;
	
	public JmsSystem(Props props) {
		this.props=props;
		if (props.getString("sendParams", null)==null)
			this.sendParams = "";
		else
			this.sendParams = "?"+props.getString("sendParams", "");
		try {
			QueueConnectionFactory connectionFactory = createConnectionFactory();
			String username=props.getString("username", null);
			if (username==null)
				connection = connectionFactory.createQueueConnection();
			else {
				String password=props.getString("password", null);
				if (password==null)
					password=CryptoUtil.decrypt(props.getString("encryptedPassword"));
				connection = connectionFactory.createQueueConnection(username, password);
			}
			connection.start();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}

	protected QueueConnectionFactory createConnectionFactory() {
        Hashtable<String, String> env= new Hashtable<String,String>();
        env.put( "java.naming.factory.initial", "com.sun.jndi.fscontext.RefFSContextFactory" );
        Object jndifile = props.get("jndifile");
        if (jndifile instanceof File) {
			try {
				env.put( "java.naming.provider.url", ((File)jndifile).toURI().toURL().toString());
			} catch (MalformedURLException e) { throw new RuntimeException(e); }
        }
		else if (jndifile instanceof String )
        	env.put( "java.naming.provider.url", (String) jndifile);
		else 
			throw new RuntimeException("Unknown configuration type "+jndifile+" for property jndifile");
        env.put( "java.naming.security.authentication", "none" );
        if( !"none".equals( env.get("java.naming.security.authentication"))) {
            env.put( "java.naming.security.principal", props.getString("username",null));
            env.put( "java.naming.security.credentials", props.getString("password",null));
        }
        
		InitialContext jndiContext;
		try {
			String name=props.getString("jndiName");
			jndiContext = new InitialContext( env);
			logger.debug("Looking up {}",name);
			return (QueueConnectionFactory) jndiContext.lookup( name );
		} catch (NamingException e) { throw new RuntimeException(e); }

	}


	public JmsQueue getQueue(String name) { return new JmsQueue(this, name); }
	public QueueConnection getConnection() { return connection;	}
	public void close() {
		try {
			connection.close();
		}
		catch (JMSException e) {throw new RuntimeException(e); }
	}
	
	public void stop() {
		try {
			connection.close();
		} catch (JMSException e) { throw new RuntimeException(e);}
	}
}
