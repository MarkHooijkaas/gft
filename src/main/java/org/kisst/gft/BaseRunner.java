package org.kisst.gft;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;

public class BaseRunner {
	public final File configfile;
	private final Class<? extends Module> [] modules;
	private boolean running=false;
	public final String topname;
	private GftWrapper gft;


	protected BaseRunner(String topname, String configfilename, Class<? extends Module> ... modules) {
		this.modules=modules;
		this.topname=topname;
		if (configfilename==null)
			this.configfile=findConfigFile(topname);
		else
			this.configfile = new File(configfilename);
		PropertyConfigurator.configure(this.configfile.getParent()+"/"+topname+".log4j.properties");
	}
	
	private File findConfigFile(String topname) {
		File result=new File("config."+topname+"/"+topname+".properties");
		if (result.exists())
			return result;
		return new File("config/"+topname+".properties");
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
