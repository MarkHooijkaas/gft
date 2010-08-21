package org.kisst.gft;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;

import nl.duo.gft.GftDuoModule;

import org.apache.log4j.PropertyConfigurator;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.mq.QueueSystem;
import org.kisst.gft.mq.file.FileQueueSystem;
import org.kisst.gft.mq.jms.ActiveMqSystem;
import org.kisst.gft.mq.jms.JmsSystem;
import org.kisst.gft.ssh.GenerateKey;
import org.kisst.util.CryptoUtil;
import org.kisst.util.FileUtil;
import org.kisst.util.TemplateUtil;

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
	private static Cli.Flag putmsg = cli.flag("p","putmsg", "puts a message on the input queue");
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
		if (putmsg.isSet()) {
			// TODO: refactor this code dupplication
			SimpleProps props=new SimpleProps();
			props.load(configfile);
			QueueSystem queueSystem;

			Props qmprops=props.getProps("gft.queueSystem");
			String type=qmprops.getString("type");
			if ("File".equals(type))
				queueSystem=new FileQueueSystem(qmprops);
			else if ("ActiveMq".equals(type))
				queueSystem=new ActiveMqSystem(qmprops);
			else if ("Jms".equals(type))
				queueSystem=new JmsSystem(qmprops);
			else 
				throw new RuntimeException("Unknown type of queueing system "+type);
			HashMap<String, Object> context = new HashMap<String, Object>();
			context.put("global", props.get("gft.global", null));
			String queuename = TemplateUtil.processTemplate(props.getString("gft.listener.main.queue"), context);
			String data=FileUtil.loadString(new InputStreamReader(System.in));
			queueSystem.getQueue(queuename).send(data);
			System.out.println("send the following message to the queue "+queuename);
			System.out.println(data);
			return;
		}
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
