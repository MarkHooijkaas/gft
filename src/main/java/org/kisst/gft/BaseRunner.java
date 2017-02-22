package org.kisst.gft;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;

public class BaseRunner {
	public final File configfile;
	private final Class<? extends Module> [] modules;
	private boolean running=false;
	public final String topname;
	private GftWrapper gft;


	public BaseRunner(String topname, File configfilename, Class<? extends Module> ... modules) {
		this.modules=modules;
		this.topname=topname;
		this.configfile = configfilename;
		PropertyConfigurator.configure(this.configfile.getParent()+"/"+topname+".log4j.properties");
	}
	
	public void start() {
		if (gft!=null)
			throw new RuntimeException("Gft already running");
		running=true;
		gft=new GftWrapper(topname, configfile, modules);
		gft.start();
	}

	public void run() {
		start();
		while (running) {
			gft.join();
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
