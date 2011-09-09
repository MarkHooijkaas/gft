package org.kisst.gft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import nl.duo.gft.odwek.OnDemandHost;
import nl.duo.gft.poller.Poller;

import org.kisst.gft.action.HttpHost;
import org.kisst.gft.admin.AdminServer;
import org.kisst.gft.filetransfer.As400SshHost;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.SshHost;
import org.kisst.gft.filetransfer.StartFileTransferTask;
import org.kisst.gft.filetransfer.WindowsSshHost;
import org.kisst.jms.ActiveMqSystem;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MessageHandler;
import org.kisst.jms.MultiListener;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.ReflectionUtil;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class GftContainer {
	final static Logger logger=LoggerFactory.getLogger(GftContainer.class); 

	private final MessageHandler starter = new StartFileTransferTask(this); 
	private final AdminServer admin=new AdminServer(this);
	public Props props;

	public final HashMap<String, Channel> channels= new LinkedHashMap<String, Channel>();
	public final HashMap<String, Props>   actions= new LinkedHashMap<String, Props>();
	public final HashMap<String, HttpHost>   httphosts= new LinkedHashMap<String, HttpHost>();
	public final HashMap<String, SshHost>    sshhosts= new LinkedHashMap<String, SshHost>();
	public final HashMap<String, OnDemandHost>    ondemandhosts= new LinkedHashMap<String, OnDemandHost>();
	public final HashMap<String, MultiListener>  listeners= new LinkedHashMap<String, MultiListener>();
	private final HashMap<String, Module > modules=new LinkedHashMap<String, Module>();
	private final SimpleProps context = new SimpleProps();
	public final HashMap<String, Poller> pollers= new LinkedHashMap<String, Poller>();
	private final String hostName;



	private final File configfile;

	public JmsSystem queueSystem;
	public String getVersion() {
		InputStream in = GftContainer.class.getResourceAsStream("/version.properties");
		if (in==null)
			return "unknown-version";
		Properties props=new Properties();
		try {
			props.load(in);
		} catch (IOException e) { throw new RuntimeException(e);}
		return props.getProperty("project.version");
	}
	public void addAction(String name, String classname) {
		SimpleProps props=new SimpleProps();
		props.put("class", classname);
		actions.put(name, props);
	}
	public GftContainer(File configfile) {
		TemplateUtil.init(configfile.getParentFile());
		context.put("gft", this);

		this.configfile = configfile;
		addAction("check_src","CheckSourceFile");
		addAction("check_dest","CheckDestFileDoesNotExist");
		addAction("copy","CopyFile");
		addAction("check_copy","CheckCopiedFile");
		addAction("remove","DeleteSourceFile");
		addAction("notify","NotifyReceiver");
		addAction("reply","SendReplyAction");
		addAction("fix_permissions","FixPermissions");
		addAction("sendGftMessage", "SendGftMessageAction");
		addAction("localCommand", "LocalCommandAction");
		addAction("archive", "ArchiveAction");
		try {
			this.hostName= java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) { throw new RuntimeException(e); }
	}
	public JmsSystem getQueueSystem() { return queueSystem; }
	public SimpleProps getContext() {return context; }
	
	public void init(Props props) {
		this.props=props;
		context.put("global", props.get("gft.global", null));
		addDynamicModules(props);
		for (Module mod: modules.values())
			mod.init(props);

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

		if (props.get("gft.ondemand.host",null)!=null) {
			Props hostProps=props.getProps("gft.ondemand.host");
			for (String name: hostProps.keys()) {
				Props p=hostProps.getProps(name);
				ondemandhosts.put(name, new OnDemandHost(p));
			}
		}
		
		Props qmprops=props.getProps("gft.queueSystem");
		String type=qmprops.getString("type");
		if ("ActiveMq".equals(type))
			queueSystem=new ActiveMqSystem(qmprops);
		else if ("Jms".equals(type))
			queueSystem=new JmsSystem(qmprops);
		else 
			throw new RuntimeException("Unknown type of queueing system "+type);

		for (String lname: props.getProps("gft.listener").keys()) {
			listeners.put(lname, new MultiListener(queueSystem, starter, props.getProps("gft.listener."+lname), context));
		}


		if (props.hasKey("gft.action")) {
			Props actionProps=props.getProps("gft.action");
			for (String name: actionProps.keys()) {
				Props p=actionProps.getProps(name);
				actions.put(name, p);
			}
		}

		Props channelProps=props.getProps("gft.channel");
		for (String name: channelProps.keys())
			channels.put(name, new Channel(this, channelProps.getProps(name)));

		if (props.hasKey("gft.poller")) {
			Props pollerProps=props.getProps("gft.poller");
			for (String name: pollerProps.keys())
				//pollers.put(name, new Poller(this, pollerProps.getProps(name)));
				pollers.put(name, new Poller(this, name, pollerProps.getProps(name)));
		}

		
		
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
	public String processTemplate(Object template, Object context) { return TemplateUtil.processTemplate(template, context); }

	public void start() {
		SimpleProps props=new SimpleProps();
		props.load(configfile);
		init(props);
		logger.info("Starting GftContainer on host "+hostName);
		if (logger.isDebugEnabled()){
			logger.debug("Starting GftContainer with props {}", props.toString());
		}
		for (MultiListener q : listeners.values() )
			q.start();
		for (Poller p : pollers.values())
			p.start();
		admin.startListening();
	}
	public void join() {
		admin.join();
	}

	public void stop() {
		for (MultiListener q : listeners.values() )
			q.stop();
		for (Poller p : pollers.values())
			p.stop();
		queueSystem.stop();
		admin.stopListening();
	}

	private void addDynamicModules(Props props) {
		Object moduleProps = props.get("gft.modules",null);
		if (! (moduleProps instanceof Props))
			return;
		Props modules = (Props) moduleProps;
		for (String name:modules.keys()) {
			try {
				addModule(name, modules.getProps(name));
			} catch (Exception e) {
				throw new RuntimeException("Could not load module class "+name, e);
			}
		}
	}
	private void addModule(String name, Props props) {
		String classname=props.getString("class");
		Constructor<?> cons=ReflectionUtil.getConstructor(classname, new Class<?>[] {GftContainer.class, String.class, Props.class});
		Module mod= (Module) ReflectionUtil.createObject(cons, new Object[] {this, name, props});
		modules.put(name, mod);
	}
}
