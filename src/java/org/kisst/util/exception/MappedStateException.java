package org.kisst.util.exception;

import java.util.LinkedHashMap;
import java.util.Set;

public class MappedStateException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final LinkedHashMap<String, String> state= new LinkedHashMap<String,String>();

	public MappedStateException(Throwable err) { super(err.getMessage(), err); }
	
	public Set<String> getKeys() { return state.keySet(); }
	public String getState(String key) {return state.get(key);}
	public MappedStateException addState(String key, String value) {state.put(key, value); return this;}
}