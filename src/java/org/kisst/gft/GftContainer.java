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
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.gft.TaskStarter.JmsTaskCreator;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionCreator;
import org.kisst.gft.action.DeleteLocalFileAction;
import org.kisst.gft.action.LocalCommandAction;
import org.kisst.gft.action.SendMessageFromFileAction;
import org.kisst.gft.admin.AdminServer;
import org.kisst.gft.admin.BaseServlet;
import org.kisst.gft.admin.status.QueueStatus;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileTransferModule;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.ssh.As400SshHost;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.ssh.WindowsSshHost;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.http4j.BasicHttpHostMap;
import org.kisst.http4j.HttpHost;
import org.kisst.http4j.HttpHostMap;
import org.kisst.jms.ActiveMqSystem;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MultiListener;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.MultiProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.CryptoUtil;
import org.kisst.util.JamonUtil;
import org.kisst.util.JamonUtil.JamonThread;
import org.kisst.util.JarLoader;
import org.kisst.util.JarLoader.ModuleInfo;
import org.kisst.util.ReflectionUtil;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;




public class GftContainer implements HttpHostMap, ActionCreator {
	final static Logger logger=LoggerFactory.getLogger(GftContainer.class); 
	
	public static class Settings extends CompositeSetting {
		public Settings(CompositeSetting parent, String name) { super(parent, name); }
		public final JarLoader.Settings modules=new JarLoader.Settings(this, "modules");
		public final BooleanSetting continueIfConfigBroken=new BooleanSetting(this, "continueIfConfigBroken", false);
	}

	private final Settings settings;
	private final String topname;
	private final TaskStarter starter = new TaskStarter(); 
	private final AdminServer admin;
	public final Props props;
	private final SimpleProps topProps;
	private final boolean configBroken;
	
	private final BasicHttpHostMap httpHosts;
	
	public final HashMap<String, TaskDefinition> channels= new LinkedHashMap<String, TaskDefinition>();
	private final HashMap<String, Class<?>>   actions= new LinkedHashMap<String, Class<?>>();
	//public final HashMap<String, HttpHost>   httphosts= new LinkedHashMap<String, HttpHost>();
	
	public final HashMap<String, SshFileServer>    sshhosts= new LinkedHashMap<String, SshFileServer>(); 	 // TODO: make private
	public final HashMap<String, MultiListener>  listeners= new LinkedHashMap<String, MultiListener>();
	private final HashMap<String,JmsSystem> queueSystem = new LinkedHashMap<String,JmsSystem>();
	private final HashMap<String, Module > modules=new LinkedHashMap<String, Module>();
	private final HashMap<String, Constructor<?>> channelTypes=new LinkedHashMap<String, Constructor<?>>();
	private final SimpleProps context = new SimpleProps();
	public final HashMap<String, Poller> pollers= new LinkedHashMap<String, Poller>();
	private final String hostName;
	private final String tempdir;
	private int directorySequenceNumber=0;
	private JamonThread jamonThread;
	
	private final  JarLoader jarloader;
	
	private final File configfile;
	public final Date startupTime = new Date();
	public Date getStartupTime() { return startupTime; }
	

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
		actions.put(name, cls);
	}
	public GftContainer(String topname, File configfile) {
		this.topname=topname;
		this.settings=new Settings(null, topname);
		
		TemplateUtil.init(configfile.getParentFile());
		context.put(topname, this);
	
		this.configfile = configfile;
		topProps = new SimpleProps(this.configfile);
		props=topProps.getProps(this.topname);
		
		addAction("local_command", LocalCommandAction.class);
		addAction("delete_local_file", DeleteLocalFileAction.class);
		addAction("send_message_from_file",SendMessageFromFileAction.class);
		
		try {
			this.hostName= java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) { throw new RuntimeException(e); }
		
		jarloader=new JarLoader(settings.modules, topProps);
		addDynamicModules(props);
		loadModuleSpecificCryptoKey();
		admin=new AdminServer(this);	
		httpHosts = new BasicHttpHostMap(props.getProps("http.host"));
		tempdir = props.getString("global.tempdir"); //MAYBE: provide default as working directory+"/temp"
		configBroken=init();
		if (configBroken) {
			boolean continueAnyways = settings.continueIfConfigBroken.get(topProps);
			String message = "Errors during initialization, using property "+settings.continueIfConfigBroken.getFullName()+"="+continueAnyways;
			if (continueAnyways)
				logger.error(message);
			else
				throw new RuntimeException("FATAL "+message);
		}
	}
	

	public Set<String> getFileServerNames() { return sshhosts.keySet(); } 
	public FileServer getFileServer(String name) { 
		SshFileServer result = sshhosts.get(name);
		if (result==null)
			throw new IllegalArgumentException("Unknown fileserver name "+name);
		return result;
	}

	public JmsSystem getQueueSystem(String name) { 
		JmsSystem result = queueSystem.get(name);
		if (result==null)
			throw new RuntimeException("Unknown queueSystem "+name);
		return result;
	}
	public SimpleProps getContext() {return context; }
	public ClassLoader getSpecialClassLoader() { return jarloader.getClassLoader(); }
	public String getTopname() { return topname; }
	public HttpHost getHttpHost(String name) { return httpHosts.getHttpHost(name); }
	public Set<String> getHttpHostNames() { return httpHosts.getHttpHostNames(); }
	public boolean isConfigBroken() { return configBroken; }
	public Props getGlobalProps() { return props.getProps("global"); }

	
	
	
	private boolean init() {
		boolean configBroken=false;
		context.put("global", props.get("global", null));
		//String omgeving = getGlobalProps().getString("omgeving",null);
		//SimpleProps var=new SimpleProps(); 
		//var.put("omgevingNaar", omgeving);
		//var.put("herkomstOmgeving", omgeving);
		Object defaultVars = props.get("defaultVars", null);
		if (defaultVars instanceof Props)
			context.put("var", defaultVars);
		
		for (Module mod: modules.values()) {
			if (settings.modules.module.get(mod.getName()).disabled.get(topProps)) {
				logger.warn("Skipping module initialisation for {}",mod.getName());
			}
			else
				mod.init(props);
		}

		if (props.get("ssh.host",null)!=null) {
			Props hostProps=props.getProps("ssh.host");
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
					throw new RuntimeException("property type for "+name+".ssh.host."+name+" should be WINDOWS, AS400 or UNIX, not "+type);
			}
		}

		Props qmhostProps=props.getProps("mq.host");
		for (String name: qmhostProps.keys()) {
			Props qmprops=qmhostProps.getProps(name);
			String type=qmprops.getString("type");
			if ("ActiveMq".equals(type))
				queueSystem.put(name, new ActiveMqSystem(qmprops));
			else if ("Jms".equals(type))
				queueSystem.put(name, new JmsSystem(qmprops));
			else 
				throw new RuntimeException("Unknown type of queueing system "+type);
		}
		
		for (String lname: props.getProps("listener").keys()) {
			Props listenerprops = props.getProps("listener."+lname);
			String queueSystemName = listenerprops.getString("queueSystem", "main");
			MultiListener listener = new MultiListener(getQueueSystem(queueSystemName), starter, listenerprops, context);
			listeners.put(lname, listener);
			admin.addStatusItem(new QueueStatus(this, listener, "input"));
			admin.addStatusItem(new QueueStatus(this, listener, "error"));
		}

		Props channelProps=props.getProps("channel");
		for (String name: channelProps.keys()) {
			try {
				addChannel(name, channelProps.getProps(name));
			}
			catch (RuntimeException e) {
				logger.error("Error when loading channel "+name, e);
				configBroken=true;
				channels.put(name, new BrokenChannel(this, props, e));
			}
			
		}

		if (props.hasKey("poller")) {
			Props pollerProps=props.getProps("poller");
			for (String name: pollerProps.keys()) {
				try {
					//pollers.put(name, new Poller(this, pollerProps.getProps(name)));
					pollers.put(name, new Poller(this, name, pollerProps.getProps(name)));
				}
				catch (RuntimeException e) {
					logger.error("Error when loading poller "+name, e);
					configBroken=true;
					channels.put(name, new BrokenChannel(this, props, e));
				}
			}

		}

		if (logger.isDebugEnabled()) {
			logger.debug("Using props "+props);
			for (String name: channels.keySet())
				logger.info("Channel {}\t{}",name,channels.get(name));
			for (String name: actions.keySet())
				logger.info("Action {}\t{}",name,actions.get(name));
			for (String name: getHttpHostNames())
				logger.info("HttpHost {}\t{}",name,getHttpHost(name));			
			for (String name: sshhosts.keySet())
				logger.info("SshHost {}\t{}",name,sshhosts.get(name));			
			for (String name: listeners.keySet())
				logger.info("Listener {}\t{}",name,listeners.get(name));
		}
		return configBroken;
	}

	private void addChannel(String name, Props channelprops) {
		MultiProps lprops=new MultiProps(channelprops,getGlobalProps());

		String type=lprops.getString("type","Default");
		Constructor<?> cons=channelTypes.get(type);
		if (cons==null) {
			String typenames="";
			String komma="";
			for (String name2: channelTypes.keySet()) {
				typenames+=komma+name2;
				komma=",";
			}
			throw new RuntimeException("Unknown Channel type in channel "+name+": "+type+" only the following types are allowed "+typenames);
		}
		TaskDefinition channel = (TaskDefinition) ReflectionUtil.createObject(cons, new Object[] {this, lprops} );
		channels.put(name, channel);
	}
	public TaskDefinition getTaskDefinition(String name) { return channels.get(name); }
	public String processTemplate(File template, Object context) { return TemplateUtil.processTemplate(template, context); }
	public String processTemplate(String templateText, Object context) { return TemplateUtil.processTemplate(templateText, context); }
	public String processTemplate(Template template, Object context) { return TemplateUtil.processTemplate(template, context); }

	public void addServlet(String url, BaseServlet servlet) { admin.addServlet(url, servlet); }

	public void start() {
		logger.info("Starting GftContainer on host "+hostName);
		LogService.log("info", "StartingContainer", getTopname().toUpperCase()+"-Service", hostName, "Starting "+getTopname().toUpperCase()+" Service on host "+ hostName);

		this.jamonThread = new JamonThread(props);
		Thread t = new Thread(jamonThread);
		t.setDaemon(true);
		t.start();
		
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
		logger.info("Resetting GftContainer on host "+hostName);
		LogService.log("info", "Resetting", getTopname().toUpperCase()+"-Service", hostName, "Reset called for "+getTopname().toUpperCase()+" Service on host "+ hostName);
		JamonUtil.jamonLog(props, "RESET called, dumping all statistics");
		jamonThread.reset();
		for (Poller p: pollers.values())
			p.reset();
	}
	
	public void stop() {
		logger.info("Stopping GftContainer on host "+hostName);
		LogService.log("info", "Stopping", getTopname().toUpperCase()+"-Service", hostName, "Stopping "+getTopname().toUpperCase()+" Service on host "+ hostName);
		JamonUtil.jamonLog(props, "STOP called, dumping all statistics");
		jamonThread.stop();
		for (MultiListener q : listeners.values() )
			q.stop();
		for (Poller p : pollers.values())
			p.stop();
		for (JmsSystem q : queueSystem.values())
			q.stop();
		admin.stopListening();
	}

	
	public void addModule(Class<? extends Module> cls) {
		boolean disabled = props.getBoolean("module."+cls.getSimpleName()+".disabled", false); 
		try {
			if (! disabled)
				addModule(cls, null);
		} catch (Exception e) {
			throw new RuntimeException("Could not load module class "+cls.getSimpleName(), e);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void addDynamicModules(Props props) {
		//modules.put("filetransfer", new FileTransferModule(this, props));
		addModule(FileTransferModule.class);

		for (Class<?> cls: jarloader.getMainClasses()) {
			addModule((Class<? extends Module>) cls);
		}
	}
	private void addModule(Class<?> cls,  Props props) {
		Constructor<?> cons=ReflectionUtil.getConstructor(cls, new Class<?>[] {GftContainer.class, Props.class});
		Module mod= (Module) ReflectionUtil.createObject(cons, new Object[] {this, props});
		modules.put(mod.getName(), mod);
	}
	// This method is to give modules a way to set a cryptographic key very early in startup process.
	// especially before the Host list with encrypted passwords is initialized 
	private void loadModuleSpecificCryptoKey() {
		for (Module mod: modules.values()) {
			CryptoUtil.checkKeySetter(mod);
		}
	}
	
	private synchronized int getUniqueSequenceNumber() {
		directorySequenceNumber++;
		if (directorySequenceNumber>1000000)
			directorySequenceNumber=0;
		return directorySequenceNumber;
	}


	public File createUniqueDir(String subdir){
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		String date = formatter.format(new Date());
		int volgnummer=getUniqueSequenceNumber();
		File file = new File(tempdir +"/"+ subdir +"/"+ date + "-" + volgnummer);
		file.mkdirs();
		return file;
		
	}

	public void registerDefinitionType(Class<?> cls) { registerDefinitionType(cls.getSimpleName(), cls); }
	public void registerDefinitionType(String name, Class<?> cls) {
		if (channelTypes.get(name)!=null)
			throw new RuntimeException("TaskDefinitionType "+name+" is already registerd to class "+channelTypes.get(name)+" when trying to register it for class "+cls);
		Constructor<?> cons = ReflectionUtil.getConstructor(cls, new Class<?>[] { GftContainer.class, Props.class} );
		channelTypes.put(name, cons);
	}
	
	public void appendJmsTaskCreator(JmsTaskCreator creator) {starter.appendCreator(creator); }

	public List<ModuleInfo> getModuleInfo() {
		return jarloader.getModuleInfo();
	}


	public String getMainQueue() {
		for (MultiListener l : listeners.values())
			return l.getInputQueue();
		throw new RuntimeException("No main queue defined");
	}
	
	
	
	private LayeredProps getActionProps(Props channelprops, Class<?> clz, String actionname) {
		LayeredProps lprops=new LayeredProps(getGlobalProps());
		SimpleProps top=new SimpleProps();
		//top.put("action",taskdef.gft.actions.get(name));
		top.put("channel",channelprops);
		lprops.addLayer(top);
		if (channelprops.get(clz.getSimpleName(),null) instanceof Props)
			lprops.addLayer(channelprops.getProps(clz.getSimpleName()));

		if (actionname!=null && channelprops.get(actionname,null) instanceof Props)
			lprops.addLayer(channelprops.getProps(actionname));
		lprops.addLayer(channelprops);
		//lprops.addLayer(taskdef.gft.props.getProps("gft.global"));
		return lprops;
	}

	
	public Action createAction(TaskDefinition taskdef, String actionname) {
		return createAction(taskdef, actions.get(actionname), actionname);
	}
	public Action createAction(TaskDefinition taskdef, Class<?> cls) {
		return createAction(taskdef, cls, cls.getSimpleName());
	}
	
	private Action createAction(TaskDefinition taskdef, Class<?> clz, String actionname) {
		if (clz==null)
			throw new IllegalArgumentException("Unknown action name "+actionname+" when definining "+taskdef.getName());
		Props actionprops=getActionProps(taskdef.getProps(), clz, actionname);
		Constructor<?> c = ReflectionUtil.getFirstCompatibleConstructor(clz, new Class<?>[] {TaskDefinition.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {taskdef, actionprops} );

		c = ReflectionUtil.getConstructor(clz, new Class<?>[] {GftContainer.class, Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {this, actionprops} );

		c = ReflectionUtil.getConstructor(clz, new Class<?>[] {Props.class} );
		if (c!=null)
			return (Action) ReflectionUtil.createObject(c, new Object[] {actionprops} );
		
		return (Action) ReflectionUtil.createObject(clz);
	}
}
