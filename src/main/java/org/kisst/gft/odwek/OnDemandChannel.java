package org.kisst.gft.odwek;

import java.io.PrintWriter;

import org.kisst.gft.action.Action;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.DummyFileServer;
import org.kisst.gft.filetransfer.action.DestinationFile;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.BasicGftFlow;
import org.kisst.gft.task.BasicTaskDefinition;


public abstract class OnDemandChannel extends BasicTaskDefinition implements SourceFile, DestinationFile {
	private final Action flow;
	private final OnDemandDefinition def;
	private final FileLocation dest;

	public OnDemandChannel(BasicGftFlow flow) { this(flow, null); }
	public OnDemandChannel(BasicGftFlow flow, OnDemandDefinition def) {
		super(flow.getGft(), flow.getProps());
		this.flow=flow;
		if (def==null)
			this.def = new OnDemandDefinition(props.getProps("ondemand",null));
		else
			this.def = new OnDemandDefinition(def, props.getProps("ondemand",null));
		this.dest=new FileLocation(new DummyFileServer("OnDemand"), this.def.odfolder+","+this.def.odapplgroup+","+this.def.odapplication);
	}

	public OnDemandDefinition getOnDemandDefinition() { return def; }
	
	@Override protected void writeHtmlBody(PrintWriter out) {
		def.writeHtml(out);
	}
	
	@Override public FileLocation getDestinationFile() { return dest; }
	@Override public FileLocation getFinalDestinationFile() { return dest; }

	@Override public Action getFlow() {  return this.flow; }
}
