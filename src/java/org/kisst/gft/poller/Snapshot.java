package org.kisst.gft.poller;


public class Snapshot {
	private final long timestamp;
	private final String snapshot;
	
	public Snapshot(String snapshot) {
		timestamp=new java.util.Date().getTime();
		this.snapshot=snapshot;
	}

	public boolean equals(Snapshot other) {
		if (other==null)
			return false;
		return snapshot.equals(other.snapshot);
	}
	public long getTimestamp() { return timestamp; }
}
