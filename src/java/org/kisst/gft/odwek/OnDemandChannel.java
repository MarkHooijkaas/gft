package org.kisst.gft.odwek;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.task.AbstractTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;


public class OnDemandChannel extends AbstractTaskDefinition {
	private final OnDemandDefinition def;

	public OnDemandChannel (GftContainer gft, Action flow, Props props) { this(gft, flow, props, null); }
	public OnDemandChannel(GftContainer gft, Action flow, Props props, OnDemandDefinition def) {
		super(gft, flow, props);
		if (def==null)
			this.def = new OnDemandDefinition(props.getProps("ondemand"));
		else
			this.def = new OnDemandDefinition(def, props.getProps("ondemand"));
	}

	public OnDemandDefinition getOnDemandDefinition() { return def; }
	
	@Override public void writeHtml(PrintWriter out) {
		writeHtmlHeader(out);
		def.writeHtml(out);
		writeHtmlFooter(out);
	}

	public String getDestDescription() { return def.toString();}

	@Override protected String getLogDetails(Task task) { return task.toString();}

}
