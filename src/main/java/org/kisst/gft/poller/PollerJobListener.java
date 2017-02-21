package org.kisst.gft.poller;

public interface PollerJobListener {
	public void updateGuiStatus(String name, boolean running);
	public void updateGuiRuns(String name, int value);
	public void updateGuiSuccess(String name, int value);
	public void updateGuiErrors(String name, int value);
}
