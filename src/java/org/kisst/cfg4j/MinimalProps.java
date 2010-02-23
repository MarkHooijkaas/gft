package org.kisst.cfg4j;

public interface MinimalProps {
	public String getFullName();
	public Iterable<String> keys();
	public Object get(String key, Object defaultValue);
	public boolean hasKey(String key);
}
