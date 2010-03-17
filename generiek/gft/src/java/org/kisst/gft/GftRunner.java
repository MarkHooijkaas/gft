package org.kisst.gft;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

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
		gft=new GftContainer(configfile);
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
	
	public static void main(String[] args) {
		System.out.println("starting GFT");
		if (args.length!=1)
			throw new RuntimeException("usage: GftRunner <config file>");
		File configfile=new File(args[0]);
		PropertyConfigurator.configure(configfile.getParent()+"/log4j.properties");
		GftRunner runner= new GftRunner(configfile);
		runner.run();
		
		System.out.println("GFT stopped");
	}


}
