package org.kisst.flow4j;

public interface Flow {
	String getName();
	Step getStep(String name);
}
