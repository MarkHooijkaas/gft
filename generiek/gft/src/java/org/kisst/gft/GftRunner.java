package org.kisst.gft;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.log4j.PropertyConfigurator;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.ssh.GenerateKey;
import org.kisst.jms.ActiveMqSystem;
import org.kisst.jms.JmsSystem;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.CryptoUtil;
import org.kisst.util.FileUtil;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GftRunner {
	final static Logger logger=LoggerFactory.getLogger(Channel.class); 

	private final File configfile;
	private boolean running=false;
	private final String topname;
	private GftContainer gft;

	
	public GftRunner(String topname, File configfile) {
		this.topname=topname;
		this.configfile = configfile;
	}

	public void start() {
		if (gft!=null)
			throw new RuntimeException("Gft already running");
		running=true;
		gft=new GftContainer(topname, configfile);
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
	private static Cli.StringOption config;
	private static Cli.Flag putmsg = cli.flag("p","putmsg", "puts a message on the input queue");
	private static Cli.StringOption rmmsg = cli.stringOption("r","rmmsg","selector", null);
	private static Cli.Flag help =cli.flag("h", "help", "show this help");
	private static Cli.Flag keygen =cli.flag("k", "keygen", "generate a public/private keypair");
	private static Cli.StringOption encrypt = cli.stringOption("e","encrypt","key", null);
	private static Cli.StringOption decrypt = cli.stringOption("d","decrypt","key", null);

	public static void main(String[] args) { main("gft", args); }
	
	public static void main(String topname, String[] args) {
		config = cli.stringOption("c","config","configuration file", "config/"+topname+".properties");
		cli.parse(args);
		if (help.isSet()) {
			showHelp();
			return;
		}
		CryptoUtil.setKey("-P34{-[u-C5x<I-v'D_^{79'3g;_2I-P_L0£_j3__5`y§%M£_C");
		File configfile=new File(config.get());
		if (putmsg.isSet()) {		// TODO: refactor this code dupplication
			logger.info("gft put");
			logger.info("loading props");
			SimpleProps props=new SimpleProps();
			props.load(configfile);
			logger.info("opening queuesystem");
			JmsSystem queueSystem=getQueueSystem(topname, props);
			String queuename = getQueue((SimpleProps) props.getProps(topname));
			logger.info("loading data from standard input");

			String data=FileUtil.loadString(new InputStreamReader(System.in));
			logger.info("sending message");
			queueSystem.getQueue(queuename).send(data);
			logger.info("send the following message to the queue {}",queuename);
			logger.debug("data send was {}",data);
			queueSystem.close();
			return;
		}
		if (rmmsg.isSet()) {		// TODO: refactor this code dupplication
			SimpleProps props=new SimpleProps();
			props.load(configfile);
			JmsSystem queueSystem=getQueueSystem(topname,props);
			String queuename = getQueue(props);
			String selector=rmmsg.get();
			logger.info("removing the following message "+selector);
			try {
				Session session = queueSystem.getConnection().createSession(true, Session.SESSION_TRANSACTED);
				MessageConsumer consumer = session.createConsumer(session.createQueue(queuename), selector);
				Message msg = consumer.receive(5000);
				if (msg==null)
					logger.info("Could not find message "+selector);
				else {
					session.commit();
					logger.info("Removed message "+selector);
				}
				queueSystem.close();
			}
			catch (JMSException e) { throw new RuntimeException(e); }
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
		if (decrypt.get()!=null) {
			System.out.println("OPTION DISABLED");
			//System.out.println(CryptoUtil.decrypt(decrypt.get()));
			return;
		}

		PropertyConfigurator.configure(configfile.getParent()+"/log4j.properties");
		GftRunner runner= new GftRunner(topname, configfile);
		runner.run();
		
		System.out.println("GFT stopped");
	}

	private static String getQueue(SimpleProps props) {
		HashMap<String, Object> context = new HashMap<String, Object>();
		context.put("global", props.get("global", null));
		String queuename = TemplateUtil.processTemplate(props.getString("listener.main.queue"), context);
		return queuename;
	}

	private static void showHelp() {
		System.out.println("usage: java -jar gft.jar [options]");
		System.out.println(cli.getSyntax(""));
	}

	private static JmsSystem getQueueSystem(String topname, Props props) {
		Props qmprops=props.getProps(topname+".mq.host.main");
		String type=qmprops.getString("type");
		if ("ActiveMq".equals(type))
			return new ActiveMqSystem(qmprops);
		else if ("Jms".equals(type))
			return new JmsSystem(qmprops);
		else 
			throw new RuntimeException("Unknown type of queueing system "+type);
	}

}
