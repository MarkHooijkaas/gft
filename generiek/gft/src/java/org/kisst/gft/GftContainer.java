package org.kisst.gft;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.log4j.PropertyConfigurator;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.action.HttpHost;
import org.kisst.gft.admin.AdminServer;
import org.kisst.gft.filetransfer.As400SshHost;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.filetransfer.StartFileTransferTask;
import org.kisst.gft.filetransfer.WindowsSshHost;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.QueueListener;
import org.kisst.gft.mq.QueueSystem;
import org.kisst.gft.mq.file.FileQueueSystem;
import org.kisst.gft.mq.jms.ActiveMqSystem;
import org.kisst.gft.mq.jms.JmsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;


public class GftContainer {
	private final static Logger logger=LoggerFactory.getLogger(GftContainer.class); 

	private final MessageHandler starter = new StartFileTransferTask(this); 
	private final AdminServer admin=new AdminServer(this);
	public Props props;
	
	public final HashMap<String, Channel> channels= new LinkedHashMap<String, Channel>();
	public final HashMap<String, Props>   actions= new LinkedHashMap<String, Props>();
	public final HashMap<String, HttpHost>   httphosts= new LinkedHashMap<String, HttpHost>();
	public final HashMap<String, SshHost>    sshhosts= new LinkedHashMap<String, SshHost>();
	public final HashMap<String, QueueSystem> queuemngrs= new LinkedHashMap<String, QueueSystem>();
	public final HashMap<String, QueueListener>  listeners= new LinkedHashMap<String, QueueListener>();

	private final File configfile;
	private final GftRunner runner;
	private final Configuration freemarkerConfig= new Configuration();

	public GftContainer(GftRunner runner, File configfile) {
		this.runner=runner;
		this.configfile = configfile;
		freemarkerConfig.setTemplateLoader(new GftTemplateLoader(configfile.getParentFile()));
		freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
	}
	public void init(Props props) {
		this.props=props;
		//actions.put("copy", new RemoteScpAction());
		if (props.get("gft.http.host",null)!=null) {
			Props hostProps=props.getProps("gft.http.host");
			for (String name: hostProps.keys())
				httphosts.put(name, new HttpHost(hostProps.getProps(name)));
		}

		if (props.get("gft.ssh.host",null)!=null) {
			Props hostProps=props.getProps("gft.ssh.host");
			for (String name: hostProps.keys()) {
				Props p=hostProps.getProps(name);
				String type=p.getString("type",null);
				if ("WINDOWS".equals(type))
					sshhosts.put(name, new WindowsSshHost(p));
				else if ("UNIX".equals(type))
					sshhosts.put(name, new SshHost(p));
				else if ("AS400".equals(type))
					sshhosts.put(name, new As400SshHost(p));
				else 
					throw new RuntimeException("property type for gft.ssh.host."+name+" should be WINDOWS, AS400 or UNIX, not "+type);
			}
		}

		if (props.get("gft.qmgr",null)!=null) {
			Props pollerProps=props.getProps("gft.qmgr");
			for (String name: pollerProps.keys()) {
				QueueSystem sys=null;
				Props p=pollerProps.getProps(name);
				String type=p.getString("type");
				if ("File".equals(type))
					sys=new FileQueueSystem(p);
				else if ("ActiveMq".equals(type))
					sys=new ActiveMqSystem(p);
				else if ("Jms".equals(type))
					sys=new JmsSystem(p);
				else 
					throw new RuntimeException("Unknown type of queueing system "+type);
				queuemngrs.put(name, sys);
				for (String lname: p.getProps("listener").keys()) {
					listeners.put(lname, sys.createListener(p.getProps("listener."+lname)));
				}
			}
		}


		Props actionProps=props.getProps("gft.action");
		for (String name: actionProps.keys()) {
			Props p=actionProps.getProps(name);
			actions.put(name, p);
		}

		Props channelProps=props.getProps("gft.channel");
		for (String name: channelProps.keys())
			channels.put(name, new Channel(this, channelProps.getProps(name)));

		if (logger.isDebugEnabled()) {
			logger.debug("Using props "+props);
			for (String name: channels.keySet())
				logger.info("Channel {}\t{}",name,channels.get(name));
			for (String name: actions.keySet())
				logger.info("Action {}\t{}",name,actions.get(name));
			for (String name: httphosts.keySet())
				logger.info("HttpHost {}\t{}",name,httphosts.get(name));			
			for (String name: sshhosts.keySet())
				logger.info("SshHost {}\t{}",name,sshhosts.get(name));			
			for (String name: listeners.keySet())
				logger.info("Listener {}\t{}",name,listeners.get(name));
		}
	}
	public Channel getChannel(String name) { return channels.get(name); }
	public HttpHost getHost(String name) { return httphosts.get(name); }
	public Template getTemplate(String name) { 
		try {
			return freemarkerConfig.getTemplate(name);
		}
		catch (IOException e) { throw new RuntimeException(e);}
	}
	public void processTemplate(String templateName, Object context, Writer out) {
		try {
			Template templ=freemarkerConfig.getTemplate(templateName);
			templ.process(context, out);
		}
		catch (IOException e) { throw new RuntimeException(e);} 
		catch (TemplateException e) {  throw new RuntimeException(e);}
	}
	public String processTemplate(String templateName, Object context) {
		StringWriter out=new StringWriter();
		processTemplate(templateName, context, out);
		return out.toString();
	}
	
	public void run() {
		SimpleProps props=new SimpleProps();
		props.load(configfile);
		init(props);
		logger.info("Starting GftContainer");
		if (logger.isDebugEnabled()){
			logger.debug("Starting GftContainer with props {}", props.toString());
		}
		for (QueueListener q : listeners.values() )
			q.listen(starter);
		admin.run();
	}

	public void stop() {
		for (QueueListener q : listeners.values() )
			q.stopListening();
		for (QueueSystem sys: queuemngrs.values())
			sys.stop();
		admin.stopListening();
	}

	public void shutdown() { runner.shutdown();	}
	public void restart() { runner.restart(); }

	
	public static void main(String[] args) {
		if (args.length!=1)
			throw new RuntimeException("usage: GftContainer <config file>");
		File configfile=new File(args[0]);
		PropertyConfigurator.configure(configfile.getParent()+"/log4j.properties");
		GftRunner runner= new GftRunner(configfile);
		runner.start();
		logger.info("GFT stopped");
	}
}
