package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FileServerConnection.FileAttributes;
import org.kisst.util.TemplateUtil;
import org.kisst.util.exception.BasicFunctionalException;

public class DirectoryServlet extends BaseServlet {
	public DirectoryServlet(GftWrapper wrapper) {
		super(wrapper);
	}

	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");


	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String path="";
		if (! url.endsWith("/"))
			path=url.substring(url.lastIndexOf('/')+1)+"/";
		String name=url.substring("/dir/".length());
		while (name.startsWith("/"))
			name=name.substring(1);
		int pos=name.indexOf(":");
		int pos2=name.indexOf("/");
		if (pos<=0)
			pos=pos2;
		if (pos2>0 && pos2<pos)
			pos=pos2;
		String dir="";
		if (pos>0) {
			dir=name.substring(pos+1);
			name=name.substring(0,pos);
		}
		while (name.startsWith("/"))
			name=name.substring(1);
		// TODO: this hack makes dynamic path names sometimes work in a static context
		if (dir.startsWith("dynamic:")) {
			dir=dir.substring(8);
			dir=dir.replaceAll("%7B", "{");
			dir=dir.replaceAll("%7D", "}");
			dir=TemplateUtil.processTemplate(dir, wrapper.getCurrentGft().getContext());
		}
		
		
		if (name.endsWith(":"))
			name=name.substring(0,pos-1);
		if (dir.length()>1024)
			throw new BasicFunctionalException("Dirname length should not exceed 1024 characters");
		if (! validCharacters.matcher(dir).matches())
			throw new BasicFunctionalException("Dirname should only contain alphanumeric characters / . - or _");
		if (dir.indexOf("..")>=0)
			throw new BasicFunctionalException("Dirname ["+dir+"] is not allowed to contain .. pattern");

		out.println("<h1>Directory "+name+":"+dir+"</h1>");
		out.println("<table>");
		out.println("<tr><td><b>filename</b></td><td width=100 ALIGN=RIGHT><b>filesize</b></td><td><b>modification date</b></td></tr>");
		FileServerConnection conn = wrapper.getCurrentGft().getFileServer(name).openConnection();
		int count = 0;
		try {
			LinkedHashMap<String, FileAttributes> entries = conn.getDirectoryEntries(dir);
			for (String filename: entries.keySet()) {
				if (filename.equals(".") || filename.equals(".."))
					continue;
				FileAttributes attr = entries.get(filename);
				filename=filename.replaceAll("&", "&amp;");
				filename=filename.replaceAll("<", "&lt;");
				filename=filename.replaceAll(">", "&gt;");
				String txt = null;
				if (attr.isDirectory)
					txt = "<tr><td><a href=\""+path+filename+"\">" + filename + "</td><td ALIGN=RIGHT>DIR</td>";
				else
					txt = "<tr><td>" + filename + "</td><td ALIGN=RIGHT>"+attr.size+"</td>";
				txt=txt+"<td>"+new Date(attr.modifyTimeMilliSecs*1000)+ "</td></tr>";
				out.println(txt);
				count++;
			}
		}
		finally { conn.close(); }
		out.println("</table>");
		out.println(count+" entries found");
		
	}

}
