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
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.cfg4j.DefaultSpecification;
import org.kisst.cfg4j.IntSetting;
import org.kisst.cfg4j.LongSetting;
import org.kisst.cfg4j.StringSetting;
import org.kisst.props4j.Props;


public class HttpCaller {
	public static class Settings extends CompositeSetting {
		public final StringSetting host = new StringSetting(this, "host");
		public final LongSetting closeIdleConnections = new LongSetting(this, "closeIdleConnections", -1);  
		public final IntSetting timeout = new IntSetting(this, "timeout", 30000);  
		public final StringSetting urlPostfix = new StringSetting(this, "urlPostfix", null);

		public Settings(CompositeSetting parent, String name, DefaultSpecification... args) { super(parent, name, args); }
	}
	
	public static final Settings defaults= new Settings(null, null);

	private static final MultiThreadedHttpConnectionManager connmngr = new MultiThreadedHttpConnectionManager();
	private static final HttpClient client = new HttpClient(connmngr);

	protected final Props props;
	private final long closeIdleConnections;
	protected final HttpHost host;
	private final int timeout;
	private final String urlPostfix;
	

	public HttpCaller(HttpHostMap hostMap,  Props props) {
		this(hostMap, props, defaults);
	}
	
	public HttpCaller(HttpHostMap hostMap, Props props, Settings settings) {
		this.props=props;
		closeIdleConnections=settings.closeIdleConnections.get(props);
		
		String hostname = settings.host.get(props);
		//if (hostname==null)
		//	throw new RuntimeException("host config parameter should be set");
		host=hostMap.getHttpHost(hostname.trim());
		timeout = settings.timeout.get(props);
		urlPostfix=settings.urlPostfix.get(props);
	}

	public String getCompleteUrl(String url) { return host.url+url+urlPostfix; } // TODO: make smarter with / and ? handling

	public String httpGet(String url) {
	    GetMethod method = new GetMethod(getCompleteUrl(url)); 
		return httpCall(method);
	}
	
	public String httpPost(String body) {
	    PostMethod method = new PostMethod(getCompleteUrl(""));
		try {
			method.setRequestEntity(new StringRequestEntity(body, "text/xml", "UTF-8"));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
		return httpCall(method);
	}
	
	
	
	private String httpCall(final HttpMethodBase method) {
		HttpState state = createState(method);
	    method.getParams().setSoTimeout(timeout);
	    try {
			if (closeIdleConnections>=0) // Hack because often some idle connections were closed which resulted in 401 errors
				connmngr.closeIdleConnections(closeIdleConnections);
	    	//int statusCode = client.executeMethod(method.getHostConfiguration(), method, state);
	    	int statusCode = client.executeMethod(null, method, state);
			byte[] response=method.getResponseBody();
			String result = new String(response, "UTF-8");
			if (statusCode >= 300) {
				throw new RuntimeException("HTTP call returned "+statusCode+"\n"+result);
			}
			return result;
	    }
	    catch (HttpException e) { throw new RuntimeException(e); } 
	    catch (IOException e) {  throw new RuntimeException(e); }
	    finally {
	    	method.releaseConnection(); // TODO: what if connection not yet borrowed?
	    }
	}
	
	private HttpState createState(HttpMethodBase method) {
		if (host.username == null)
			return null;
		HttpState state=new HttpState();
		state.setCredentials(AuthScope.ANY, host.getCredentials());
		method.setDoAuthentication(true);
		return state;
	}

}
