/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kisst.http4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.LongSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpCaller {
	private final static Logger logger=LoggerFactory.getLogger(HttpCaller.class); 

	public static class Response {
		final private int code;
		final private String response;
		public Response(CloseableHttpResponse response) {
            this.code=response.getStatusLine().getStatusCode();
            try {
				this.response = EntityUtils.toString(response.getEntity());
			}
            catch (ParseException e) { throw new RuntimeException(e); }
            catch (IOException e) {  throw new RuntimeException(e); } 
		}
		public int getCode() { return code; }
		public String getResponseString() {  return response;}
	}

    public static class Settings extends CompositeSetting {
        public Settings(CompositeSetting parent, String name) { super(parent, name); }
        public final StringSetting host = new StringSetting(this, "host","esb1");
        public final LongSetting closeIdleConnections = new LongSetting(this, "closeIdleConnections", -1);
        public final IntSetting timeout = new IntSetting(this, "timeout", 30000);
        public final StringSetting returnCodesToIgnore= new StringSetting(this, "returnCodesToIgnore", null);
        // public final StringSetting urlPostfix = new StringSetting(this, "urlPostfix", null);
    }

    private static final PoolingHttpClientConnectionManager connmngr = new PoolingHttpClientConnectionManager();
    private static final IdleConnectionMonitorThread idleThread = new IdleConnectionMonitorThread(connmngr);//can not be static because multiple classes use this, so there are multiple instances

	private static final CredentialsProvider credsProvider = new BasicCredentialsProvider();
    private static final CloseableHttpClient client = HttpClients.custom()
    	.setDefaultCredentialsProvider(credsProvider)
    	.setConnectionManager(connmngr)
    	.build();
    //private static final AuthCache authCache = new BasicAuthCache();
    //private static final BasicScheme basicAuth = new BasicScheme();
    //private static final HttpClientContext localContext = HttpClientContext.create();
    static {
        idleThread.setDaemon(true);
        idleThread.start();
        //localContext.setAuthCache(authCache);
    }
    
    protected final Props props;
    private final long closeIdleConnections;
    protected final HttpHost host;
    private final int timeout;
	private final int[] returnCodesToIgnore;
	private final Credentials credentials;
	private static final int[] EMPTY_INT_ARRAY = new int[0];

    // private final String urlPostfix;

    public HttpCaller(HttpHostMap hostMap, Props props) {
        this(hostMap, props, new Settings(null, null));
    }

    public HttpCaller(HttpHostMap hostMap, Props props, Settings settings) {
        this.props = props;
        closeIdleConnections = settings.closeIdleConnections.get(props);

        String hostname = settings.host.get(props);
        // if (hostname==null)
        // throw new RuntimeException("host config parameter should be set");
        host = hostMap.getHttpHost(hostname.trim());
        timeout = settings.timeout.get(props);
        // urlPostfix=settings.urlPostfix.get(props);
        credentials = host.getCredentials();
		if (credentials!=null) {
	        AuthScope scope;
        	if (credentials instanceof NTCredentials) 
        		scope= new AuthScope(getHostFromUrl(host.url), host.port, AuthScope.ANY_REALM, AuthSchemes.NTLM);
        	else
        		scope=new AuthScope(getHostFromUrl(host.url), host.port, AuthScope.ANY_REALM, AuthSchemes.BASIC);
    		credsProvider.setCredentials(scope, credentials);
        }
		
		String codes=settings.returnCodesToIgnore.get(props);
		if (codes==null)
			this.returnCodesToIgnore=EMPTY_INT_ARRAY;
		else {
			String[] codeList = codes.split(",");
			this.returnCodesToIgnore=new int[codeList.length];
			for (int i=0; i<codeList.length; i++) 
				this.returnCodesToIgnore[i]=Integer.parseInt(codeList[i]);
		}
    }

    public String getCompleteUrl(String url) {
        return host.url + url;
    } // TODO: make smarter with / and ? handling

    public String httpGet(String url) {
        HttpGet method = new HttpGet(getCompleteUrl(url));
		logger.info("Calling url: {}", url);
        return httpCall(method).getResponseString();
    }

    public String httpPost(String url, String body) {
        HttpPost method = new HttpPost(getCompleteUrl(url));
        method.setEntity(new StringEntity(body, ContentType.create("text/xml", "UTF-8")));
        return httpCall(method).getResponseString();
    }

    @SuppressWarnings("deprecation")
	protected Response httpCall(final HttpRequestBase method) {
    	try {
    		if (credentials instanceof UsernamePasswordCredentials)
			method.addHeader(new BasicScheme().authenticate(credentials, method));
		} 
    	catch (AuthenticationException e) { throw new RuntimeException(e);}
        method.setConfig(RequestConfig.custom().setSocketTimeout(timeout).build());// setStaleConnectionCheckEnabled()?
        try {
            if (closeIdleConnections >= 0) { // Hack because often some idle connections were closed which resulted in 401 errors
                connmngr.closeIdleConnections(closeIdleConnections, TimeUnit.SECONDS);
            }
            Response response = new Response(client.execute(method));
            
            checkResponseForErrors(response);
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection(); // TODO: what if connection not yet borrowed?
        }
    }

	protected void checkResponseForErrors(Response response) {
		if (response.getCode() >= 300) {
			boolean ignore=false;
			for (int responseCode: returnCodesToIgnore) {
				if (responseCode==response.getCode())
					ignore=true;
			}
			if (! ignore)
				throw new RuntimeException("HTTP call returned " + response.getCode() + "\n" + response.getResponseString());
		}
	}

    private String getHostFromUrl(String url) {
        String result = url;
        if (url.contains("http://")) {
            result = result.replaceAll("http://", "");
        }
        if (result.contains("/")) {
            result = result.substring(0, result.indexOf("/"));
        }

        return result;
    }


}
