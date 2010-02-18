package org.kisst.gft;

import java.io.File;

public class GftRunner {
	private final File configfile;
	private boolean running=false;
	private GftContainer gft;
	
	public GftRunner(File configfile) {
		this.configfile = configfile;
	}

	public void start() {
		if (gft!=null)
			throw new RuntimeException("Gft already running");
		running=true;
		while (running) {
			gft=new GftContainer(this,configfile);
			gft.run();
		}
		gft=null;
	}
	public void shutdown() {
		running=false;
		if (gft==null)
			return;
		gft.stop();
	}
	public void restart() {
		if (gft==null)
			return;
		gft.stop();
	}
	

}
