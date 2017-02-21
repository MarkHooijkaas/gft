package org.kisst.gft.poller;

public class DummyListener implements PollerJobListener {
	public void updateGuiErrors(String name, int value) {}
	public void updateGuiRuns(String name, int value) {}
	public void updateGuiStatus(String name, boolean running) {}
	public void updateGuiSuccess(String name, int value) {}
}
