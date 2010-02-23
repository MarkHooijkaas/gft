package org.kisst.flow4j;

import java.util.ArrayList;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.flow4j.TaskStep.Status;

public class Task {
	private final Flow flow;
	private final String id;
	private final History history;
	private final ArrayList<TaskStep> steps;

	public Task(Flow flow, String id) {
		this.flow = flow;
		this.id = id;
		this.history=new History();
		this.steps=new ArrayList<TaskStep>();
	}
	public Task(FlowRegistry flowregistry, SimpleProps props) {
		this.flow = flowregistry.getFlow(props.getString("flow"));
		this.id = props.getString("id");
		this.history=new History(props.getSequence("history"));
		this.steps=new ArrayList<TaskStep>();
		for (Object o: props.getSequence("steps")) 
			this.steps.add(new TaskStep(this, (Props) o));
	}

	public Flow getFlow() { return flow; }
	public String getId() { return id; }
	public TaskStep getCurrentStep() { 
		TaskStep result=null;
		for (TaskStep s:steps) {
			if (s.getStatus()!=Status.DONE) {
				if (result!=null)
					throw new RuntimeException("task "+id+" has two unfinished steps: "+result+" and "+s);
				result=s;
			}
		}
		return result;
	}
	
	public Iterable<TaskStep> getSteps() { return steps; }
	
	public Props toProps() {
		SimpleProps result=new SimpleProps();
		result.put("flow", flow.getName());
		result.put("id", id);
		result.put("history", history);
		result.put("steps", steps);
		return result;
	}
}
