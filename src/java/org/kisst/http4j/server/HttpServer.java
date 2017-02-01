package org.kisst.http4j.server;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.kisst.http4j.server.HttpCall.UnauthorizedException;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HttpServer extends AbstractHandler {
	public static class PageRedirectedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public final String url;
		public PageRedirectedException(String url) { super("call redirected to "+url); this.url=url; }
	}
	
	
	private final static Logger logger=LoggerFactory.getLogger(HttpServer.class);

	private Server server=null;
	protected final HttpCallHandler handler;

	public final HttpServerConfiguration config;

	public HttpServer(Props props, HttpCallHandler handler) {
		this.config=new HttpServerConfiguration(props);
		this.handler=handler;
	}

	public void startListening() {
		server=createServer();
        try {
			server.start();
		} catch (Exception e) { throw new RuntimeException(e);}
	}

	private Server createServer() {
		Server server = new Server();

		// http
		if (this.config.httpEnabled) {
			ServerConnector httpServerConnector = new ServerConnector(server);
			httpServerConnector.setHost(this.config.host);
			httpServerConnector.setPort(config.port);
			httpServerConnector.setIdleTimeout(this.config.idleTimeout);
			server.addConnector(httpServerConnector);
			logger.info("HTTP enabled on port: " + config.port);
		} else
			logger.info("HTTP disabled");

		// https
		if (this.config.httpsEnabled) {
			HttpConfiguration httpConfiguration = new HttpConfiguration();
			httpConfiguration.setSecurePort(this.config.sslPort);
			httpConfiguration.addCustomizer(new SecureRequestCustomizer());
			HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfiguration);
			SslContextFactory sslContextFactory = createSslContextFactory();
			SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());

			ServerConnector sslServerConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
			sslServerConnector.setHost(this.config.host);
			sslServerConnector.setPort(this.config.sslPort);
			sslServerConnector.setIdleTimeout(this.config.idleTimeout);
			server.addConnector(sslServerConnector);
			logger.info("HTTPS enabled on port: " + config.sslPort);
		} else
			logger.info("HTTPS disabled");

		NCSARequestLog requestLog = new NCSARequestLog("logs/jetty-yyyy_mm_dd.request.log");
		requestLog.setAppend(true);
		requestLog.setExtended(false);
		//requestLog.setLogTimeZone("GMT");
		requestLog.setLogTimeZone("CET");
		server.setRequestLog(requestLog);
		
		server.setHandler(this);
		return server;
	}

	/**
	 * These settings are advised at
	 * <a href="http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html">
	 *     http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html</a>
	 */
	private SslContextFactory createSslContextFactory() {

		SslContextFactory factory = new SslContextFactory();
		factory.setKeyStorePath(config.sslKeyStorePath);
		factory.setKeyStorePassword(config.sslKeyStorePassword);
		factory.setKeyManagerPassword(config.sslKeyManagerPassword);
		factory.setTrustStorePath(config.sslTrustStorePath);
		factory.setTrustStorePassword(config.sslTrustStorePassword);
		factory.setIncludeCipherSuites("TLS_DHE_RSA.*", "TLS_ECDHE.*");
		factory.setExcludeCipherSuites(".*NULL.*", ".*RC4.*", ".*MD5.*", ".*DES.*", ".*DSS.*");
		factory.setRenegotiationAllowed(false);
		return factory;
	}


	public void join() {
		try {
			server.join();
		} catch (Exception e) { throw new RuntimeException(e);}
		logger.info("web server stopped");
		server=null;
	}

	public void stopListening() {
		final Server server=this.server; // remember it, because it will set it self to null
		logger.info("Stopping web server");
		try {
			//server.setGracefulShutdown(1000);
			//Thread.sleep(1000);
			server.stop();
			//Thread.sleep(3000);
			for (Connector conn : server.getConnectors())
				conn.stop();
			server.destroy();
		} catch (Exception e) { throw new RuntimeException(e);}
	}

	public void handle(String path, Request baseRequest , HttpServletRequest request, HttpServletResponse response) {
		try {
			String host=request.getServerName();
			if (config.redirectToHttps && ! request.isSecure()) {
				StringBuilder url = new StringBuilder("https://");
				url.append(request.getServerName());
				if (request.getRequestURI() != null)
					url.append(request.getRequestURI());
				if (request.getQueryString() != null) 
					url.append("?").append(request.getQueryString());
				response.sendRedirect(url.toString());
				return;
			}
			if (config.restrictedToHost!=null) {
				boolean allowed = false;
				for (String h: config.restrictedToHost.split(","))
					allowed=allowed || h.equals(host);
				if (! allowed) {
					response.sendError(403, "Server "+host+" is closed");
					return;
				}
			}
			HttpCall call=new HttpCall(baseRequest, request,response);
			handler.handle(call,request.getRequestURI());
		}
        catch (PageRedirectedException e) { logger.info("redirect to {} from {}",e.url, request.getRequestURI()); }
        catch (UnauthorizedException e) {
        	try {
        		response.resetBuffer();
				response.sendError(403, e.getMessage());
			}
        	catch (IOException e1) { logger.error("IO exception when redirecting Unauthorized call",e); }
        }
		catch (Exception e) {
    		System.out.println("GENERAL");
        	try {
        		if (e instanceof HttpException) {
        			HttpException he=(HttpException) e;
        			response.sendError(he.code, e.getMessage()); 
        			e.printStackTrace();
        		}
        		else {
        			logger.error("Error when handling "+path,e);
        			StringWriter result = new StringWriter();
        			PrintWriter out = new PrintWriter(result);
        			out.println(e.getMessage());
        			out.println("<pre>");
        			e.printStackTrace(out);
        			out.println("</pre>");
        			response.sendError(500, result.toString());
        		}
			} catch (IOException e1) {
				// ignore the new error, and now write to the logfile anyway
				logger.error("Error when handling "+path, e);
			}
        }
		finally {
			try { 
				if (!response.isCommitted())
					response.flushBuffer();
			} 
			catch (EofException e) { logger.error("EOF error during FlushBuffer "+e); }
			catch (IOException e) { logger.error("IO error during FlushBuffer ",e); }
		}
	}
	
	public static class HttpException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final int code;
		public HttpException(int code, String msg) {super(msg); this.code=code; } 
		public HttpException(int code, String msg, Throwable e) {super(msg, e); this.code=code; } 
		public HttpException(int code, Throwable e) {super(e); this.code=code; } 
	}
}