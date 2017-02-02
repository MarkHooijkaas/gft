package org.kisst.http4j.server;

import org.kisst.props4j.Props;

public class HttpServerConfiguration {

	public final String host;
	public final int port;
	public final long idleTimeout;
	public final String restrictedToHost;
	public final boolean redirectToHttps;
	public final boolean httpEnabled;
	public final boolean httpsEnabled;
	public final int httpsPort;
	public final String sslKeyStorePath;
	public final String sslKeyStorePassword;
	public final String sslKeyManagerPassword;
	public final String sslTrustStorePath;
	public final String sslTrustStorePassword;

	public HttpServerConfiguration(Props props) {
		this.host = props.getString("host", null);
		this.port = props.getInt("port", 8080);
		this.idleTimeout = props.getInt("idleTimeout", 30000);
		this.restrictedToHost = props.getString("restrictedToHost",null);
		this.redirectToHttps= props.getBoolean("redirectToHttps",false);
		this.httpEnabled = props.getBoolean("httpEnabled", true);
		this.httpsEnabled = props.getBoolean("httpsEnabled", false);
		this.httpsPort = httpsEnabled ? props.getInt("httpsPort", 8443) : -1;
		this.sslKeyStorePath = httpsEnabled ? props.getString("sslKeyStorePath") : null;
		this.sslKeyStorePassword = httpsEnabled ? props.getString("sslKeyStorePassword") : null;
		this.sslKeyManagerPassword = httpsEnabled ? props.getString("sslKeyManagerPassword") : null;
		this.sslTrustStorePath = httpsEnabled ? props.getString("sslTrustStorePath") : null;
		this.sslTrustStorePassword = httpsEnabled ? props.getString("sslTrustStorePassword") : null;
	}

}