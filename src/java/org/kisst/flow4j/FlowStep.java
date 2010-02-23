package org.kisst.flow4j;

public interface FlowStep {
	public Flow getFlow();
	public boolean isSafelyRetryable();
}
