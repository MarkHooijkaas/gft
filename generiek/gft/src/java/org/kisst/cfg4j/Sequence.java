package org.kisst.cfg4j;

public interface Sequence extends Iterable<Object>{
	public int size();
	public Object get(int index);
}
