package org.kisst.util;

import java.util.Date;
import java.util.HashMap;

public class Cache<K,V> {
	private final HashMap<K,V> map=new HashMap<K,V>();

	private Date firstModificationTime=null; 
	private Date expirationTime=null; 
	private int autoRefreshInterval=300;
	
	/*
	public void initJmx(IManagedComponent parent) {
		IManagedComponent mc = parent.createSubComponent("CacheType", "Cache", 
				new Message("Een simpel caching mechanisme"), this);
		ISettingsCollection s =  mc.getSettingsCollection();
		s.defineHotSetting("autoRefreshInterval", new Message("interval waarna hele cache leeggegooid wordt (0=geen caching, -1=oneindig)"), 
				"autoRefreshInterval",	this, null,	new Integer(300));
		mc.createPropertyBasedValueCounter("numberOfEntries", new Message("currently number of cached items"), "numberOfEntries", this);
		mc.defineOperation("clear", new Message("clear the cache"), 
				"clear", this, OperationImpact.ACTION); //, new IParameterDefinition[]{});
	}
	*/
	
	public int getAutoRefreshInterval() {
		return autoRefreshInterval;
	}

	public int getNumberOfEntries() {	return map.size();	}
	
	public void setAutoRefreshInterval(int autoRefreshInterval) {
		this.autoRefreshInterval = autoRefreshInterval;
		if (autoRefreshInterval < 0 ) {
			expirationTime=null;
		}
		else if (firstModificationTime!=null) {
			expirationTime=new Date(firstModificationTime.getTime()+autoRefreshInterval);
		}
	}


	private void expireIfNecessary() {
		if (autoRefreshInterval<0)
			return; // a negative interval means that cache will never expire
		if (expirationTime==null)
			return; // cacheshould be empty
		if (expirationTime.compareTo(new Date())<0) {
			clear();
			firstModificationTime=null;
		}
	}
	private void setModificationTime() {
		if (autoRefreshInterval<0)
			return;
		if (firstModificationTime==null) {
			firstModificationTime=new Date();
			expirationTime=new Date(firstModificationTime.getTime()+autoRefreshInterval*1000);
		}
	}
	
	
	public synchronized void clear() {
		map.clear();
		expirationTime = null;
		firstModificationTime = null;
	}
	public synchronized void put(K key, V value) {
		if (autoRefreshInterval==0)
			return; // refresh is immediate, so no use to remember this value

		// if cache is too old, clear it first, before somthing is put in
		// otherwise the cached value will be cleared anyway next time
		expireIfNecessary();
		setModificationTime();
		map.put(key, value);
	}

	public synchronized V get(K key) {
		if (autoRefreshInterval==0)
			return null; // refresh is immediate, so should be expired anyway
		expireIfNecessary();
		return map.get(key);
	}
}
