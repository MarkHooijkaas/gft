package org.kisst.gft.odwek;

import java.io.PrintWriter;

import org.kisst.gft.action.Action;
import org.kisst.gft.task.BasicGftFlow;
import org.kisst.gft.task.BasicTaskDefinition;


public abstract class OnDemandChannel extends BasicTaskDefinition {
	private final Action flow;
	private final OnDemandDefinition def;

	public OnDemandChannel(BasicGftFlow flow) { this(flow, null); }
	public OnDemandChannel(BasicGftFlow flow, OnDemandDefinition def) {
		super(flow.getGft(), flow.getProps());
		this.flow=flow;
		if (def==null)
			this.def = new OnDemandDefinition(props.getProps("ondemand",null));
		else
			this.def = new OnDemandDefinition(def, props.getProps("ondemand",null));
	}

	public OnDemandDefinition getOnDemandDefinition() { return def; }
	
	@Override protected void writeHtmlBody(PrintWriter out) {
		def.writeHtml(out);
	}
	public String getDestDescription() { return def.toString();}
	@Override public Action getFlow() {  return this.flow; }
}
