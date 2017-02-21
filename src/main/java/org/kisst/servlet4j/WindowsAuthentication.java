package org.kisst.servlet4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
import waffle.util.AuthorizationHeader;
import waffle.util.Base64;
import waffle.util.NtlmServletRequest;
import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsSecurityContext;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;
*/

/**
 * A negotiate security filter provider.
 * @author dblock[at]dblock[dot]org
 */
public class WindowsAuthentication {
/*
    private static final Logger _log = LoggerFactory.getLogger(WindowsAuthentication.class);
	private final List<String> _protocols = new ArrayList<String>();
	private final IWindowsAuthProvider _auth= new WindowsAuthProviderImpl();

	public WindowsAuthentication() {
		_protocols.add("Negotiate");
		_protocols.add("NTLM");
	}

	public void sendUnauthorized(HttpServletResponse response) {
		Iterator<String> protocolsIterator = _protocols.iterator();
		while(protocolsIterator.hasNext()) {
			response.addHeader("WWW-Authenticate", protocolsIterator.next());
		}
	}

	public boolean isPrincipalException(HttpServletRequest request) {
		AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
		boolean ntlmPost = authorizationHeader.isNtlmType1PostAuthorizationHeader();
		_log.debug("authorization: " + authorizationHeader.toString() + ", ntlm post: " + ntlmPost);
		return ntlmPost;
	}

	public IWindowsIdentity doFilter(HttpServletRequest request,
			HttpServletResponse response)  {

		AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
		boolean ntlmPost = authorizationHeader.isNtlmType1PostAuthorizationHeader();

		// maintain a connection-based session for NTLM tokns
		String connectionId = NtlmServletRequest.getConnectionId(request);
		String securityPackage = authorizationHeader.getSecurityPackage();
		_log.debug("security package: " + securityPackage + ", connection id: " + connectionId);


		if (ntlmPost) {
			// type 2 NTLM authentication message received
			_auth.resetSecurityToken(connectionId);
		}

		byte[] tokenBuffer = authorizationHeader.getTokenBytes();
		_log.debug("token buffer: " + tokenBuffer.length + " byte(s)"+tokenBuffer.toString());
		IWindowsSecurityContext securityContext = _auth.acceptSecurityToken(connectionId, tokenBuffer, securityPackage);

		byte[] continueTokenBytes = securityContext.getToken();
		if (continueTokenBytes != null) {
			String continueToken = new String(Base64.encode(continueTokenBytes));
			_log.debug("continue token: " + continueToken);
			response.addHeader("WWW-Authenticate", securityPackage + " " + continueToken);
		}

		_log.debug("continue required: " + securityContext.getContinue());
		if (securityContext.getContinue() || ntlmPost) {
			response.setHeader("Connection", "keep-alive");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			try {
				response.flushBuffer();
			} 
			catch (IOException e) { throw new RuntimeException(e);}
			return null;
		}

        final IWindowsIdentity identity = securityContext.getIdentity();
        securityContext.dispose();
        return identity;
	}
	
	public String getUser(HttpServletRequest req, HttpServletResponse res) {
		String authhead=req.getHeader("Authorization");
		System.out.println("Header "+authhead);
		if(authhead!=null)
		{
			IWindowsIdentity user2 = doFilter(req, res);
			if (user2!=null) {
				System.out.println(user2.getFqn());
				System.out.println(user2.getSidString());
				for ( IWindowsAccount g : user2.getGroups())
					System.out.println(g.getFqn());
				return user2.getFqn();
			}
			else
				System.out.println("geen windows user");
		}
		try {
			//res.setHeader("WWW-Authenticate","Negotiate NTLM");
			res.addHeader("WWW-Authenticate", "NTLM");
			res.setHeader("Connection", "keep-alive");
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			//res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
			res.flushBuffer();
		} catch (IOException e) { throw new RuntimeException(e);}
		return null;
	}
*/
}