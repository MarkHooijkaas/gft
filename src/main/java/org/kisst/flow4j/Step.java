package org.kisst.flow4j;

public interface Step {
	public Flow getFlow();
	public boolean isSafelyRetryable();
}
