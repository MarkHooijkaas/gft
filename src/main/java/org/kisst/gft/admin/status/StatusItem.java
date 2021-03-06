package org.kisst.gft.admin.status;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.gft.admin.BaseServlet;

public abstract class StatusItem extends BaseServlet {
	private final String name;
	private final String description;

	private String timestamp;
	protected int problemCount;
	
	protected StatusItem(GftWrapper wrapper, String name) { this(wrapper, name, name); }
	protected StatusItem(GftWrapper wrapper, String name, String description) {
		super(wrapper);
		this.name=name;
		this.description=description;
	}

	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getLastUpdated() { return timestamp; } 
	abstract public String getMessage(); 
	public String getUrl() { return this.getClass().getSimpleName(); } 
	
	public String getColor() {
		if (problemCount==0)
			return "white"; 
		else if (problemCount<0)
			return "lightgrey";
		else
			return "orange";
	}
	public void writeDetails(PrintWriter out) { 
		out.append("<h1>"+description+"</h1>\n"); 
	}

	public void refresh() {
		this.timestamp=new Date().toString();
	}
	public int getProblemCount() { return problemCount; } 
	
	@Override public void handle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		writeDetails(out);
	}
}
