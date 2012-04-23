package org.kisst.gft.poller;

import org.kisst.props4j.Props;

public class MultiPoller {
	private final Poller[] pollers;
	
	public MultiPoller(Props pollerProps) {
		int count=0;
		for (@SuppressWarnings("unused") String name: pollerProps.keys())
			count++;
		pollers=new Poller[count];
		int i=0;
		for (String name: pollerProps.keys()) {
			pollers[i]=new Poller(name, pollerProps.getProps(name));
			i++;
		}
	}
	public Poller[] getPollers() { return pollers; }
	
	public void start() {
		for (Poller p: pollers)
			p.start();
	}
	public void pause() {
		for (Poller p: pollers)
			p.pause();
	}
	public void resume() {
		for (Poller p: pollers)
			p.resume();
	}	
	public void stop() {
		for (Poller p: pollers)
			p.stop();
	}
	public void join() {
		for (Poller p: pollers) {
			try {
				p.join();
			} 
			catch (InterruptedException e) { throw new RuntimeException(e); }
		}
	}

}
