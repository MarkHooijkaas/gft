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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.kisst.cfg4j.Props;
import org.kisst.util.CryptoUtil;

public class HttpHost {
	public final String url; 
	public final String username;  
	public final String password;
	public final String ntlmhost; 
	public final String ntlmdomain; 


	public HttpHost(Props props) {
		url=props.getString("url", null);
		username=props.getString("username", null);
		if (props.getString("password", null)!=null)
			password=props.getString("password");
		else
			password=CryptoUtil.decrypt(props.getString("encryptedPassword"));
		ntlmhost=props.getString("ntlmhost", null);
		ntlmdomain=props.getString("ntlmdomain", null);
	}
	
	public String toString() { return "HttpHost("+username+","+url+")"; }
	public  Credentials getCredentials(){
		if (ntlmdomain==null)
			return new UsernamePasswordCredentials(username, password);
		else
			return new NTCredentials(username, password, ntlmhost, ntlmdomain);
	}
}