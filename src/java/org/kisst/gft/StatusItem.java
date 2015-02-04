package org.kisst.gft;

import java.util.Date;

public abstract class StatusItem {
	private final String name;
	private final String description;

	protected String timestamp;
	protected int problemCount;
	protected String message;
	
	protected StatusItem(String name) { this(name, name); }
	protected StatusItem(String name, String description) {
		this.name=name;
		this.description=description;
	}
	abstract public void refreshData();

	public String getName() { return name; }
	public String getDescription() { return description; }
	public String getLastUpdated() { return timestamp; } 
	public String getMessage() { return message; } 
	
	public String getColor() {
		if (problemCount==0)
			return "white"; 
		else
			return "orange";
	}
	public String getDetails() { 
		return "<h1>"+description+"</h1>\n"; 
	}

	public void autoRefresh() { refresh(); }
	public void refresh() {
		refreshData();
		this.timestamp=new Date().toString();
	}
	public int getProblemCount() { return problemCount; } 
	
}
