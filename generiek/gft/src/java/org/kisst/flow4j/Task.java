package org.kisst.flow4j;

import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;

public class Task {
	public enum Status { OPEN, RUNNING, WAITING, ABORTED, DONE }

	private final Flow flow;
	private final String id;
	private final History history;

	public Task(Flow flow, String id) {
		this.flow = flow;
		this.id = id;
		this.history=new History();
	}
	public Task(FlowRegistry flowregistry, SimpleProps props) {
		this.flow = flowregistry.getFlow(props.getString("flow"));
		this.id = props.getString("id");
		this.history=new History(props.getSequence("history"));
	}

	public Flow getFlow() { return flow; }
	public String getId() { return id; }
	
	
	public Props toProps() {
		SimpleProps result=new SimpleProps();
		result.put("flow", flow.getName());
		result.put("id", id);
		result.put("history", history);
		return result;
	}
}
