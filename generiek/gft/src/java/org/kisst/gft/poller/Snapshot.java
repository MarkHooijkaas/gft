package org.kisst.gft.poller;

public interface Snapshot {
	public boolean equals(Snapshot other);
	public long getTimestamp();
}
