package org.kisst.props4j;

public interface Sequence extends Iterable<Object>{
	public int size();
	public Object get(int index);
}
