package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.util.CryptoUtil;

public class EncryptServlet extends BaseServlet {
	public EncryptServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		out.println("<form action=\"encrypt\" method=\"get\"><input input type=\"text\" name=\"password\"><input type=\"submit\"></form><br>");
		String txt=request.getParameter("password");
		if (txt!=null) {
			String encrypted=CryptoUtil.encrypt(txt); 
			out.println("encrypted="+encrypted+" decrypted="+CryptoUtil.decrypt(encrypted));
		}
		response.setStatus(HttpServletResponse.SC_OK);
		// The stopping is done in a separate thread, because it will also stop
		// the webserver running this servlet
		//Runnable r=new Runnable() {	public void run() {	gft.restart();	}};
		//new Thread(r).start();
	}

}
