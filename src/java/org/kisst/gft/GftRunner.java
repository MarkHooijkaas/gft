package org.kisst.gft;

import java.io.File;

import nl.duo.gft.GftDuoModule;

import org.apache.log4j.PropertyConfigurator;
import org.kisst.gft.ssh.GenerateKey;
import org.kisst.util.CryptoUtil;

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
	private static Cli.StringOption config = cli.stringOption("c","config","configuration file", "config/gft.properties");
	private static Cli.Flag help =cli.flag("h", "help", "show this help");
	private static Cli.Flag keygen =cli.flag("k", "keygen", "generate a public/private keypair");
	private static Cli.StringOption encrypt = cli.stringOption("e","encrypt","key", null);
	public static void main(String[] args) {
		cli.parse(args);
		if (help.isSet()) {
			showHelp();
			return;
		}
		GftDuoModule.setKey();
		File configfile=new File(config.get());
		if (keygen.isSet()) {
			GenerateKey.generateKey(configfile.getParentFile().getAbsolutePath()+"/ssh/id_dsa_gft"); // TODO: should be from config file
			return;
		}
		if (encrypt.get()!=null) {
			System.out.println(CryptoUtil.encrypt(encrypt.get()));
			return;
		}

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
