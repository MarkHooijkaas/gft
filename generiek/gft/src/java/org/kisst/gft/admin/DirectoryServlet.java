package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.GftContainer;

public class DirectoryServlet extends BaseServlet {
	public DirectoryServlet(GftContainer gft) { super(gft);	}

	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");


	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String name=url.substring("/dir/".length());
		int pos=name.indexOf("/");
		String dir="";
		if (pos>0) {
			dir=name.substring(pos+1);
			name=name.substring(0,pos);
		}
		if (dir.length()>1024)
			throw new FunctionalException("Dirname length should not exceed 1024 characters");
		if (! validCharacters.matcher(dir).matches())
			throw new FunctionalException("Dirname should only contain alphanumeric characters / . - or _");
		if (dir.indexOf("..")>=0)
			throw new FunctionalException("Dirname ["+dir+"] is not allowed to contain .. pattern");

		out.println("<h1>Directory "+name+"</h1>");
		out.println("<pre>");
		String txt = gft.sshhosts.get(name).ls(dir);
		txt=txt.replaceAll("&", "&amp;");
		txt=txt.replaceAll("<", "&lt;");
		txt=txt.replaceAll(">", "&gt;");
		out.println(txt);
		out.println("</pre>");
	}

}
