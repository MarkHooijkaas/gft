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
	
	private static Cli cli=new Cli();
	private static Cli.StringOption config = cli.stringOption("c","config","configuration file", "conf/gft.props");
	private static Cli.Flag help =cli.flag("h", "help", "show this help");
	public static void main(String[] args) {
		cli.parse(args);
		if (help.isSet()) {
			showHelp();
			return;
		}
		File configfile=new File(config.get());
		PropertyConfigurator.configure(configfile.getParent()+"/log4j.properties");
		GftRunner runner= new GftRunner(configfile);
		runner.run();
		
		System.out.println("GFT stopped");
	}

	private static void showHelp() {
		System.out.println("usage: java -jar gft.jar [options]");
		System.out.println(cli.getSyntax(""));
	}


}
