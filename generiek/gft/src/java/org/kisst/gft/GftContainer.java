package org.kisst.gft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;


import org.kisst.gft.TaskStarter.JmsTaskCreator;
import org.kisst.gft.action.DeleteLocalFileAction;
import org.kisst.gft.action.HttpHost;
import org.kisst.gft.action.LocalCommandAction;
import org.kisst.gft.action.LogCompleted;
import org.kisst.gft.action.LogError;
import org.kisst.gft.action.LogStart;
import org.kisst.gft.admin.AdminServer;
import org.kisst.gft.admin.BaseServlet;
import org.kisst.gft.filetransfer.FileTransferModule;
import org.kisst.gft.odwek.OnDemandHost;
import org.kisst.gft.odwek.OnDemandHostList;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.ssh.As400SshHost;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.ssh.WindowsSshHost;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.jms.ActiveMqSystem;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MultiListener;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.JamonUtil;
import org.kisst.util.JarLoader;
import org.kisst.util.ReflectionUtil;
import org.kisst.util.TemplateUtil;
import org.kisst.util.JamonUtil.JamonThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class GftContainer {
	final static Logger logger=LoggerFactory.getLogger(GftContainer.class); 

	private final TaskStarter starter = new TaskStarter(); 
	private final AdminServer admin=new AdminServer(this);
	public final SimpleProps props=new SimpleProps();

	public final HashMap<String, TaskDefinition> channels= new LinkedHashMap<String, TaskDefinition>();
	public final HashMap<String, Props>   actions= new LinkedHashMap<String, Props>();
	public final HashMap<String, HttpHost>   httphosts= new LinkedHashMap<String, HttpHost>();
	public final HashMap<String, SshFileServer>    sshhosts= new LinkedHashMap<String, SshFileServer>();
	public final HashMap<String, MultiListener>  listeners= new LinkedHashMap<String, MultiListener>();
	private final HashMap<String, Module > modules=new LinkedHashMap<String, Module>();
	private final HashMap<String, Module > channelTypes=new LinkedHashMap<String, Module>();
	private final SimpleProps context = new SimpleProps();
	public final HashMap<String, Poller> pollers= new LinkedHashMap<String, Poller>();
	private OnDemandHostList ondemandhosts = null;
	private final String hostName;
	private String tempdir;
	private int dirVolgnr;
	private JamonThread jamonThread;
	
	private final  JarLoader loader;
	
	private final File configfile;
	public final Date startupTime = new Date();
	public Date getStartupTime() { return startupTime; }
	
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
	
	public void addAction(String name, Class<?> cls) {
		SimpleProps props=new SimpleProps();
		props.put("class", cls.getName());
		actions.put(name, props);
	}
	public GftContainer(File configfile) {
		TemplateUtil.init(configfile.getParentFile());
		context.put("gft", this);
	
		this.configfile = configfile;
		props.load(this.configfile);

		addAction("local_command", LocalCommandAction.class);
		addAction("delete_local_file", DeleteLocalFileAction.class);
		addAction("log_start",LogStart.class);
		addAction("log_completed",LogCompleted.class);
		addAction("log_error",LogError.class);

		try {
			this.hostName= java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) { throw new RuntimeException(e); }
		loader=new JarLoader("./modules");
	}

	public JmsSystem getQueueSystem() { return queueSystem; }
	public SimpleProps getContext() {return context; }
	public ClassLoader getSpecialClassLoader() { return loader.getClassLoader(); }
	
	public void init() {
		this.jamonThread = new JamonThread(props);
		context.put("global", props.get("gft.global", null));
		
		tempdir = context.getString("global.tempdir");
		dirVolgnr = 0;
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
				//else if ("UNIX".equals(type))
				//	sshhosts.put(name, new RemoteFileServer(p));
				else if ("AS400".equals(type))
					sshhosts.put(name, new As400SshHost(p));
				else 
					throw new RuntimeException("property type for gft.ssh.host."+name+" should be WINDOWS, AS400 or UNIX, not "+type);
			}
		}

		Props ondemandhostProps=props.getProps("gft.ondemand.host");
		if (ondemandhostProps!=null) {
			ondemandhosts = new OnDemandHostList(ondemandhostProps);
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
		for (String name: channelProps.keys()) {
			Props p=channelProps.getProps(name);
			String type2=p.getString("type","Default");
			Module mod=channelTypes.get(type2);
			if (mod==null) {
				String typenames="";
				String komma="";
				for (String name2: channelTypes.keySet()) {
					typenames+=komma+name2;
					komma=",";
				}
				throw new RuntimeException("Unknown Channel type in channel "+name+": "+type2+" only the following types are allowed "+typenames);
			}
			TaskDefinition channel = mod.createDefinition(type2, p);
			channels.put(name, channel);
		}

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
	public TaskDefinition getTaskDefinition(String name) { return channels.get(name); }
	public HttpHost getHost(String name) { return httphosts.get(name); }
	public String processTemplate(File template, Object context) { return TemplateUtil.processTemplate(template, context); }
	public String processTemplate(String templateText, Object context) { return TemplateUtil.processTemplate(templateText, context); }

	public void addServlet(String url, BaseServlet servlet) { admin.addServlet(url, servlet); }

	public void start() {
		init();
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
	public void reset() {
		JamonUtil.jamonLog(props, "RESET called, dumping all statistics");
		jamonThread.reset();
	}
	
	public void stop() {
		JamonUtil.jamonLog(props, "STOP called, dumping all statistics");
		jamonThread.stop();
		for (MultiListener q : listeners.values() )
			q.stop();
		for (Poller p : pollers.values())
			p.stop();
		queueSystem.stop();
		admin.stopListening();
	}

	private void addDynamicModules(Props props) {
		modules.put("filetransfer", new FileTransferModule(this, props));
		Object moduleProps = props.get("gft.modules",null);
		if (moduleProps instanceof Props) {
			Props modules = (Props) moduleProps;
			for (String name:modules.keys()) {
				try {
					Props modprops = modules.getProps(name);
					String classname=modprops.getString("class");	
					Class<?> cls = loader.getClass(classname);
					addModule(cls, modprops);
				} catch (Exception e) {
					throw new RuntimeException("Could not load module class "+name, e);
				}
			}
		}

		for (Class<?> cls: loader.getMainClasses()) {
			try {
				addModule(cls, null);
			} catch (Exception e) {
				throw new RuntimeException("Could not load module class "+cls.getSimpleName(), e);
			}
		}
	}
	private void addModule(Class<?> cls,  Props props) {
		Constructor<?> cons=ReflectionUtil.getConstructor(cls, new Class<?>[] {GftContainer.class, Props.class});
		Module mod= (Module) ReflectionUtil.createObject(cons, new Object[] {this, props});
		modules.put(mod.getName(), mod);
	}
	
	private synchronized int getUniqueVolgnummer() {
		dirVolgnr++;
		if (dirVolgnr>1000000)
			dirVolgnr=0;
		return dirVolgnr;
	}


	public File createUniqueDir(String subdir){
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		String date = formatter.format(new Date());
		int volgnummer=getUniqueVolgnummer();
		File file = new File(tempdir +"/"+ subdir +"/"+ date + "-" + volgnummer);
		file.mkdirs();
		return file;
		
	}

	public void registerDefinitionType(String name, Module module) {
		if (channelTypes.get(name)!=null)
			throw new RuntimeException("TaskDefinitionType "+name+" is already registerd to module "+channelTypes.get(name)+" when trying to register it for module "+module);
		channelTypes.put(name, module);
	}
	
	public void appendJmsTaskCreator(JmsTaskCreator creator) {starter.appendCreator(creator); }

	public OnDemandHost getOnDemandHost(String name) {
		if (ondemandhosts==null)
			throw new RuntimeException("No OnDemand hosts defined when looking for host "+name);
		return ondemandhosts.getOnDemandHost(name);
	}
}
