package org.kisst.gft.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.admin.security.WindowsAuthentication;
import org.kisst.util.Base64;
import org.kisst.util.CryptoUtil;



public abstract class BaseServlet {
	
	private final WindowsAuthentication ntlm; 

	protected final GftContainer gft;
	private final String adminPassword; 
	private final boolean useNtlm;
	
	public BaseServlet(GftContainer gft) {
		this.gft=gft;
		String password=gft.props.getString("admin.password", null);
		useNtlm=gft.props.getBoolean("admin.useNtlm", false);
		
		if (password==null)
			password=CryptoUtil.decrypt(gft.props.getString("admin.encryptedPassword"));
		this.adminPassword=password;
		if (useNtlm)
			ntlm=new WindowsAuthentication();
		else
			ntlm=null;
	}

	abstract public void handle(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException;

	protected String getUser(HttpServletRequest req, HttpServletResponse res) {
		if (useNtlm)
			return ntlm.getUser(req,res);
		else
			return getBasicUser(req,res);
	}

	private  String getBasicUser(HttpServletRequest req, HttpServletResponse res) {
		String authhead=req.getHeader("Authorization");

		if(authhead!=null)
		{
			String usernpass;
			try {
				usernpass = new String(Base64.decode(authhead.substring(6)));
			} catch (IOException e) { throw new RuntimeException(e);}
			String user=usernpass.substring(0,usernpass.indexOf(":"));
			String password=usernpass.substring(usernpass.indexOf(":")+1);


			if (user.equals("admin") && password.equals(adminPassword))
				return user;
		}
		res.setHeader("WWW-Authenticate","Basic realm=\"Authorisation test servlet\"");
		try {
			res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "");
		} catch (IOException e) { throw new RuntimeException(e);}
		return null;
	}
}
