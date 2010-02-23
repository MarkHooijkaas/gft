package org.kisst.flow4j;

public interface Flow {
	Iterable<FlowStep> getSteps();

	Object getName();
}
