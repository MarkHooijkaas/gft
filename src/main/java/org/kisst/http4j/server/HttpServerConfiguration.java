package org.kisst.http4j.server;

import org.kisst.props4j.Props;
import org.kisst.util.CryptoUtil;

public class HttpServerConfiguration {

	public final String host;
	public final long idleTimeout;
	public final String restrictedToHost;
	public final int httpPort;
	public final boolean httpRedirectToHttps;
	public final boolean httpEnabled;
	public final boolean httpsEnabled;
	public final int httpsPort;
	public final String httpsKeyStorePath;
	public final String httpsKeyStorePassword;
	public final String httpsKeyManagerPassword;

	public HttpServerConfiguration(Props props) {
		this.host = props.getString("host", null);
		this.idleTimeout = props.getInt("idleTimeout", 30000);
		this.restrictedToHost = props.getString("restrictedToHost",null);
		this.httpPort = props.getInt("http.port", 8080);
		this.httpRedirectToHttps= props.getBoolean("http.redirectToHttps",false);

		this.httpEnabled = props.getBoolean("http.enabled", true);
		this.httpsEnabled = props.getBoolean("https.enabled", false);
		this.httpsPort = props.getInt("https.port", 8443);
		this.httpsKeyStorePath = props.getString("https.keyStorePath", null);
		this.httpsKeyStorePassword = CryptoUtil.parse(props.getString("https.keyStorePassword", null));
		this.httpsKeyManagerPassword = CryptoUtil.parse(props.getString("https.keyManagerPassword", null));
	}

}