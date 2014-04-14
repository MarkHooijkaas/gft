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
import java.util.concurrent.TimeUnit;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.kisst.gft.GftContainer;
import org.kisst.http4j.HttpHost;
import org.kisst.http4j.IdleConnectionMonitorThread;
import org.kisst.props4j.Props;
import org.kisst.util.SoapUtil;
import org.kisst.util.XmlNode;

public class HttpCaller {

    private static final PoolingHttpClientConnectionManager connmngr = new PoolingHttpClientConnectionManager();
    private final IdleConnectionMonitorThread idleThread = new IdleConnectionMonitorThread(connmngr);//can not be static because multiple classes use this, so there are multiple instances
    private static CloseableHttpClient client;

    {
        idleThread.setDaemon(true);
        idleThread.start();
    }

    protected final Props props;
    private final long closeIdleConnections;
    private final HttpHost[] hosts;
    private final int timeout;
    private final String urlPostfix;
    protected final GftContainer gft;

    protected HttpCaller(GftContainer gft, Props props) {
        this(gft, props, 30000, null);
    }

    protected HttpCaller(GftContainer gft, Props props, int defaultTimeout, String defaultPostfix) {
        this.gft = gft;
        this.props = props;
        closeIdleConnections = props.getLong("closeIdleConnections", -1);

        String[] hostnames = props.getString("hosts").split(",");
        hosts = new HttpHost[hostnames.length];
        int i = 0;
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        for (String hostname : hostnames) {
            HttpHost host = gft.getHttpHost(hostname.trim());
            hosts[i++] = host;
            if (host.getCredentials() instanceof NTCredentials) {
                credsProvider.setCredentials(
                        new AuthScope(getHostFromUrl(host.url), host.port, AuthScope.ANY_REALM, AuthSchemes.NTLM),
                        host.getCredentials());
            } else {
                credsProvider.setCredentials(
                        new AuthScope(getHostFromUrl(host.url), host.port, AuthScope.ANY_REALM, AuthSchemes.BASIC),
                        host.getCredentials());
            }
        }
        timeout = props.getInt("timeout", defaultTimeout);
        urlPostfix = props.getString("urlPostfix", defaultPostfix);

        client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setConnectionManager(connmngr)
                .build();

    }

    public XmlNode httpCall(XmlNode soap) {
        String response = httpCall(soap.toString());
        XmlNode result = new XmlNode(response);
        String fault = SoapUtil.getSoapFaultMessage(result);
        if (fault != null)
            throw new RuntimeException("SOAP:Fault: " + fault);
        return result;
    }

    public String httpCall(String body) {
        for (int i = 0; i < hosts.length; i++) {
            HttpHost host = hosts[i];
            HttpPost method = createPostMethod(host, body);
            try {
                String result = httpCall(method);
                return result;
            } catch (RuntimeException e) {
                if (i < hosts.length - 1) {
                }
                else
                    throw e;
            }
        }
        return null;
    }

    private HttpPost createPostMethod(HttpHost host, String body) {
        String url = host.url;
        if (urlPostfix != null)
            url += urlPostfix;

        HttpPost method = new HttpPost(url);
        method.setConfig(RequestConfig.custom().setSocketTimeout(timeout).build());// setStaleConnectionCheckEnabled()?
        method.setEntity(new StringEntity(body, ContentType.create("text/xml", "UTF-8")));
        return method;
    }

    private String httpCall(final HttpRequestBase method) {
        method.setConfig(RequestConfig.custom().setSocketTimeout(timeout).build());// setStaleConnectionCheckEnabled()?
        try {
            if (closeIdleConnections >= 0) { // Hack because often some idle connections were closed which resulted in 401 errors
                connmngr.closeIdleConnections(closeIdleConnections, TimeUnit.SECONDS);
            }
            CloseableHttpResponse response = client.execute(method);
            byte[] responseBody = new byte[(int) response.getEntity().getContentLength()];
            response.getEntity().getContent().read(responseBody);
            String result = new String(responseBody, "UTF-8");
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException("HTTP call returned " + response.getStatusLine().getStatusCode() + "\n" + result);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection(); // TODO: what if connection not yet borrowed?
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
