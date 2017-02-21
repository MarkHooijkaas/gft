package org.kisst.props4j;

import java.util.ArrayList;
import java.util.Iterator;

public class SimpleSequence implements Sequence {
	private final ArrayList<Object> list=new ArrayList<Object>();

	public void add(Object value) { list.add(value); }

	
	public int size() { return list.size();	}
	public Object get(int index) { return list.get(index); }
	public Iterator<Object> iterator() { return list.iterator();}
}
