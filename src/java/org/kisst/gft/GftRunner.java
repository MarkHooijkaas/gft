package org.kisst.gft;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import nl.duo.gft.algemeen.GftDuoAlgemeenModule;
import nl.duo.gft.chesicc.ChesiccModule;
import nl.duo.gft.dasf.ArchiveerDasfModule;
import nl.duo.gft.filetransfer.DuoFileTransferModule;
import nl.duo.gft.gas.GasModule;
import nl.duo.gft.klaarzettenBestand.KlaarzettenBestandModule;
import nl.duo.gft.scanstraat.ScanstraatModule;
import nl.duo.gft.vzub.VzubModule;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.pgm.Main;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.FileTransferModule;
import org.kisst.gft.ssh.GenerateKey;
import org.kisst.jms.ActiveMqSystem;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.JmsUtil;
import org.kisst.mq.MsgMover;
import org.kisst.mq.QueueManager;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.CryptoUtil;
import org.kisst.util.FileUtil;
import org.kisst.util.JarLoader;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.MQException;

public class GftRunner {
	public static void main(String[] args) {
		GftCli.main("gft",args,
				FileTransferModule.class,
				DuoFileTransferModule.class,
				GftDuoAlgemeenModule.class,
				KlaarzettenBestandModule.class,
				VzubModule.class,
				ChesiccModule.class
		);
	}
	//public static void main(String[] args) { GftCli.main("gft", args); }

	public final File configfile;
	private final Class<? extends Module> [] modules;
	private boolean running=false;
	private final String topname;
	private GftWrapper gft;

	public GftRunner(String topname, String configfilename) {
		this("gft", configfilename,
				FileTransferModule.class,
				DuoFileTransferModule.class,
				GftDuoAlgemeenModule.class,
				KlaarzettenBestandModule.class,
				VzubModule.class,
				ChesiccModule.class
		);
	}

	//public GftRunner(String topname, Class<? extends Module> ... modules) { this(topname,null, modules); }
	public GftRunner(String topname, String configfilename, Class<? extends Module> ... modules) {
		this.modules=modules;
		this.topname=topname;
		if (configfilename==null)
			this.configfile=findConfigFile(topname);
		else
			this.configfile = new File(configfilename);
		PropertyConfigurator.configure(this.configfile.getParent()+"/"+topname+".log4j.properties");
	}
	
	private static File findConfigFile(String topname) {
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
