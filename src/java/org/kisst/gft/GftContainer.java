package org.kisst.gft;

import freemarker.template.Template;
import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.gft.TaskStarter.JmsTaskCreator;
import org.kisst.gft.action.*;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.poller.Poller;
import org.kisst.gft.ssh.As400SshHost;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.ssh.WindowsSshHost;
import org.kisst.gft.task.TaskDefinition;
import org.kisst.http4j.BasicHttpHostMap;
import org.kisst.http4j.HttpHost;
import org.kisst.http4j.HttpHostMap;
import org.kisst.jms.JmsMessage;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MessageHandler;
import org.kisst.props4j.LayeredProps;
import org.kisst.props4j.MultiProps;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.JamonUtil;
import org.kisst.util.JamonUtil.JamonThread;
import org.kisst.util.ReflectionUtil;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;




public class GftContainer implements HttpHostMap, ActionCreator, MessageHandler {
	final static Logger logger=LoggerFactory.getLogger(GftContainer.class);

	public static class Settings extends CompositeSetting {
		public Settings(CompositeSetting parent, String name) { super(parent, name); }
		public final BooleanSetting continueIfConfigBroken=new BooleanSetting(this, "continueIfConfigBroken", false);
	}

	private final GftWrapper wrapper;
	private final Settings settings;
	private final TaskStarter starter = new TaskStarter();
	public final Props props;
	private final Props topProps;
	public final boolean configBroken;
	
	private final BasicHttpHostMap httpHosts;
	
	public final HashMap<String, TaskDefinition> channels= new LinkedHashMap<String, TaskDefinition>();
	private final HashMap<String, Class<?>>   actions= new LinkedHashMap<String, Class<?>>();
	//public final HashMap<String, HttpHost>   httphosts= new LinkedHashMap<String, HttpHost>();
	
	public final HashMap<String, SshFileServer>    sshhosts= new LinkedHashMap<String, SshFileServer>(); 	 // TODO: make private
	//private final HashMap<String, Module > modules=new LinkedHashMap<String, Module>();
	private final HashMap<String, Constructor<?>> channelTypes=new LinkedHashMap<String, Constructor<?>>();
	private final SimpleProps context = new SimpleProps();
	public final HashMap<String, Poller> pollers= new LinkedHashMap<String, Poller>();
	private final String tempdir;
	private int directorySequenceNumber=0;
	private JamonThread jamonThread;
	
	public final Date startupTime = new Date();
	public Date getStartupTime() { return startupTime; }

	public void addAction(String name, Class<?> cls) {
		actions.put(name, cls);
	}
	public GftContainer(GftWrapper wrapper, String topname, Props config) {
		this.wrapper = wrapper;
		this.settings=new Settings(null, topname);
		
		context.put(topname, this);
	
		topProps = config;
		props=topProps.getProps(wrapper.getTopname());

		addAction("local_command", LocalCommandAction.class);
		addAction("delete_local_file", DeleteLocalFileAction.class);
		addAction("send_message_from_file",SendMessageFromFileAction.class);
		wrapper.initGftFromModules(this);

		httpHosts = new BasicHttpHostMap(props.getProps("http.host"));
		tempdir = props.getString("global.tempdir"); //MAYBE: provide default as working directory+"/temp"
		configBroken=init();
	}
	
	//public String getHostName() { return hostName; }

	public Set<String> getFileServerNames() { return sshhosts.keySet(); } 
	public FileServer getFileServer(String name) { 
		SshFileServer result = sshhosts.get(name);
		if (result==null)
			throw new IllegalArgumentException("Unknown fileserver name "+name);
		return result;
	}


	public SimpleProps getContext() {return context; }
	public String getTopname() { return wrapper.getTopname(); }
	public String getHostName() { return wrapper.getHostName(); }
	public HttpHost getHttpHost(String name) { return httpHosts.getHttpHost(name); }
	public Set<String> getHttpHostNames() { return httpHosts.getHttpHostNames(); }
	public boolean isConfigBroken() { return configBroken; }
	public Props getGlobalProps() { return props.getProps("global"); }
	public JmsSystem getQueueSystem(String queueSystemName) { return wrapper.getQueueSystem(queueSystemName);}
	public String getMainQueue() { return wrapper.getMainQueue(); }
	@Override public boolean handle(JmsMessage msg) { return starter.handle(msg); }





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
			//for (String name: listeners.keySet())
			//	logger.info("Listener {}\t{}",name,listeners.get(name));
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
	public TaskDefinition getTaskDefinition(String name) { if (name==null) return null; else return channels.get(name.trim()); }
	public String processTemplate(File template, Object context) { return TemplateUtil.processTemplate(template, context); }
	public String processTemplate(String templateText, Object context) { return TemplateUtil.processTemplate(templateText, context); }
	public String processTemplate(Template template, Object context) { return TemplateUtil.processTemplate(template, context); }

	public void start() {
		logger.info("Starting GftContainer on host "+getHostName());
		logger.info("Trying to log to LogService, if this hangs the database user might be disabled");
		LogService.log("info", "StartingContainer", getTopname().toUpperCase()+"-Service", getHostName(), "Starting "+getTopname().toUpperCase()+" Container");
		logger.info("Succeeded to log to LogService");

		this.jamonThread = new JamonThread(props);
		Thread t = new Thread(jamonThread);
		t.setDaemon(true);
		t.start();
		
		if (logger.isDebugEnabled()){
			logger.debug("Starting new GftContainer with props {}", props.toString());
		}
		for (Poller p : pollers.values())
			p.start();
	}
	public void reset() {
		logger.info("Resetting GftContainer on host "+getHostName());
		LogService.log("info", "ResettingContainer", getTopname().toUpperCase()+"-Service", getHostName(), "Reset called for "+getTopname().toUpperCase()+" Container");
		JamonUtil.jamonLog(props, "RESET called, dumping all statistics");
		jamonThread.reset();
		for (Poller p: pollers.values())
			p.reset();
	}
	
	public void stop() {
		logger.info("Stopping existing GftContainer on host "+getHostName());
		LogService.log("info", "StoppingContainer", getTopname().toUpperCase()+"-Service", getHostName(), "Stopping "+getTopname().toUpperCase()+" Container");
		for (Poller p : pollers.values())
			p.stop();
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
