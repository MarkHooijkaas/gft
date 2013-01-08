package org.kisst.gft.odwek;

import java.util.Properties;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.kisst.props4j.Props;

import com.ibm.edms.od.ODConfig;
import com.ibm.edms.od.ODConstant;
import com.ibm.edms.od.ODException;
import com.ibm.edms.od.ODServer;

public class OnDemandHost {
	private final String server;
	private final String user;
	private final String passwd;
	private final int port;
	private final int tracelevel;
	private final String arslib;

	private final GenericObjectPool pool=new GenericObjectPool(new OnDemandConnectionFactory());

	public OnDemandHost(Props props) {
		this.server=props.getString("server");
		this.user=props.getString("username");
		this.passwd=props.getString("password");
		this.port=props.getInt("port");
		this.tracelevel=props.getInt("tracelevel",0);
		this.arslib=props.getString("ars3wapidll","ars3wapi32");
		initNative();
		initPool(props);
	}
	
	public ODServer borrowConnection() { 
		try {
			return (ODServer) pool.borrowObject();
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	public void releaseConnection(ODServer conn) {
		try {
			pool.returnObject(conn);
		} catch (Exception e) {throw new RuntimeException(e); }
	}
	
	public void invalidateConnection(ODServer conn) {
		try {
			pool.invalidateObject(conn);
		} catch (Exception e) {throw new RuntimeException(e); }
	}
	
	private static int nativeCount=0;
	private void initNative() {
		synchronized (OnDemandHost.class) {
			if (nativeCount>0)
				return;
			nativeCount++;
			System.loadLibrary(arslib);
		}
	}
	
	private void initPool(Props props) {
		if (props==null)
			return;
		pool.setMaxActive(props.getInt("maxSize",2));
		pool.setMinIdle(props.getInt("minIdle",2));
		pool.setMaxIdle(props.getInt("maxIdle",2));
		pool.setMaxWait(props.getLong("maxWait",30000));
		pool.setMinEvictableIdleTimeMillis(props.getLong("minEvictableIdleTimeMillis",1800000));
		pool.setTimeBetweenEvictionRunsMillis(props.getLong("timeBetweenEvictionRunsMillis",300000));
		pool.setSoftMinEvictableIdleTimeMillis(props.getLong("softMinEvictableIdleTimeMillis",300000));
		pool.setNumTestsPerEvictionRun(props.getInt("numTestsPerEvictionRun",3));
		pool.setLifo(props.getBoolean("lifo",true));
	}

	
	private class OnDemandConnectionFactory implements PoolableObjectFactory {
		public Object makeObject() {
			Properties props = new Properties();
			props.setProperty(ODConfig.ODWEK_INSTALL_DIR, "odwek-8.5.0.5");
			try {
				
				ODConfig cfg = new ODConfig(ODConstant.PLUGIN, // AfpViewer
						ODConstant.APPLET, // LineViewer
						null, // MetaViewer
						500, // MaxHits
						null, // AppletDir
						"ENU", // Language
						"./logs", // TempDir
						"./logs", // TraceDir
						tracelevel, // TraceLevel
						props);

				ODServer odServer = new ODServer(cfg);
				//odServer..setInstallDir("odwek-8.5.0.5");
				odServer.initialize("GFT");
				odServer.setServerName(server);
				odServer.setUserId(user);
				odServer.setPassword(passwd);
				//odServer.setConnectType("T");
				odServer.setPort(port);
				odServer.logon();
				//odServer.logon(server, user, passwd, 'T', port, ".");
				return odServer;
			}
			catch (ODException e) { throw new RuntimeException(e.getMessage()+", id="+e.getErrorId()+", msg="+e.getErrorMsg(), e); } 
			catch (Exception e) { throw new RuntimeException(e); }
		}
		public void destroyObject(Object obj) {
			ODServer odServer = (ODServer) obj;
			try {
				odServer.logoff();
				//odServer.terminate();
			} catch (Exception e) { throw new RuntimeException(e);	}
		}

		public void activateObject(Object obj) {}
		public void passivateObject(Object obj) {}
		public boolean validateObject(Object obj) {	return true; }
	}
}
