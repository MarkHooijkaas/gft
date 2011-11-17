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

package org.kisst.gft.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;


public class HttpAction  implements Action {
	private static final MultiThreadedHttpConnectionManager connmngr = new MultiThreadedHttpConnectionManager();
	private static final HttpClient client = new HttpClient(connmngr);

	public final Props props;
	private final long closeIdleConnections;
	private final HttpHost[] hosts;
	private final int timeout;
	private final String templateName;
	protected final GftContainer gft;
	private final boolean safeToRetry;
	
	public HttpAction(GftContainer gft, Props props) {
		this.gft=gft;
		this.props=props;
		closeIdleConnections=props.getLong("closeIdleConnections",-1);
		
		String[] hostnames = props.getString("hosts").split(",");
		hosts=new HttpHost[hostnames.length];
		int i=0;
		for (String hostname: hostnames)
			hosts[i++]=gft.getHost(hostname.trim());
		timeout = props.getInt("timeout", 30000);
		templateName=props.getString("template",null);
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	public boolean safeToRetry() { return safeToRetry; }

	protected String getBody(Task task) {
		BasicTask basictask= (BasicTask) task;
		return gft.processTemplate(templateName, basictask.getActionContext(this));
	}

	public Object execute(Task task) {
		String body=getBody(task);

		for (int i=0; i<hosts.length; i++) {
			HttpHost host=hosts[i];
			PostMethod method = createPostMethod(host, body);
			HttpState state=createState(host);
			if (state!=null)
				method.setDoAuthentication(true);
			try {
				String result = httpCall(method, state);
				if (result==null) // TODO check for SOAP Fault
					throw new RuntimeException("SOAP Fault");
				return null;
			}
			catch(RuntimeException e) {
				if (i<hosts.length-1) {
					//RelayTrace.logger.log(Severity.WARN, "Error trying to call host "+hostname+" trying next host "+hostnames[i+1],e);
				}
				else
					throw e;
			}
		}
		return null;
	}
	
	private PostMethod createPostMethod(HttpHost host, String body) {
	    PostMethod method = new PostMethod(host.url);
	    method.getParams().setSoTimeout(timeout);
		try {
			method.setRequestEntity(new StringRequestEntity(body, "text/xml", "UTF-8"));
		}
		catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
	    return method;
	}

	private HttpState createState(HttpHost host) {
		if (host.username == null)
			return null;
		HttpState state=new HttpState();
		state.setCredentials(AuthScope.ANY, host.getCredentials());
		return state;
	}

	private String httpCall(final PostMethod method, HttpState state) {
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
}
