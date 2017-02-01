package org.kisst.gft;

import org.kisst.cfg4j.BooleanSetting;
import org.kisst.cfg4j.CompositeSetting;
import org.kisst.gft.admin.*;
import org.kisst.gft.admin.status.*;
import org.kisst.jms.*;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.servlet4j.AbstractServlet;
import org.kisst.util.*;
import org.kisst.util.JamonUtil.JamonThread;
import org.kisst.util.JarLoader.ModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.UnknownHostException;
import java.util.*;


public class GftWrapper implements MessageHandler {
	final static Logger logger = LoggerFactory.getLogger(GftWrapper.class);

	public final Props props;
	public Props getProps() {return props; }

	public static class Settings extends CompositeSetting {
		public Settings(CompositeSetting parent, String name) {
			super(parent, name);
		}

		public final JarLoader.Settings modules = new JarLoader.Settings(this, "modules");
		public final BooleanSetting continueIfConfigBroken = new BooleanSetting(this, "continueIfConfigBroken", false);
	}

	private final Settings settings;
	private final String topname;
	private final AdminServer admin;
	//public final Props props;
	//private final SimpleProps topProps;
	GftContainer gft=null;

	private final HashMap<String, Module> modules = new LinkedHashMap<String, Module>();
	private JamonThread jamonThread;
	private final JarLoader jarloader;

	private final String hostName;
	private final File configfile;
	public final Date startupTime = new Date();

	public final HashMap<String, MultiListener>  listeners= new LinkedHashMap<String, MultiListener>();
	private final HashMap<String,JmsSystem> queueSystem = new LinkedHashMap<String,JmsSystem>();
	private final ArrayList<StatusItem> statusItems =new ArrayList<StatusItem>();

	public GftWrapper(String topname, File configfile, Class<? extends Module>[] moduleClasses) {
		this.topname = topname;
		this.settings = new Settings(null, topname);
		this.configfile = configfile;
		this.hostName = determineHostName();

		TemplateUtil.init(configfile.getParentFile());
		Props topProps = new SimpleProps(this.configfile);
		props = topProps.getProps(this.topname);
		setLocale(props);
		admin = new AdminServer(props);

		jarloader = new JarLoader(settings.modules, topProps);
		addDynamicModules(props);
		for (Class<? extends Module> mod: moduleClasses)
			addModule(mod, props);
		loadModuleSpecificCryptoKey();
		for (Module mod : modules.values())
			mod.init(this, props);


		loadQueuesSystems(props);
		loadListeners(props);
		boolean continueIfConfigBroken = settings.continueIfConfigBroken.get(topProps);
		String message = loadGft(continueIfConfigBroken);
		if (message!=null) {
			message = "Errors during initialization, using property " + settings.continueIfConfigBroken.getFullName() + "=" + continueIfConfigBroken+": "+message;
			if (continueIfConfigBroken)
				logger.error(message);
			else
				throw new RuntimeException("FATAL " + message);
		}
		addServlets(props);
		startJamonThread(props);
	}

	public GftContainer getCurrentGft()	{ return gft; }
	public Date getStartupTime() { return startupTime; }
	//public ClassLoader getSpecialClassLoader() { return jarloader.getClassLoader();	}
	public String getTopname() { return topname; }
	public String getHostName() { return hostName; }
	@Override public boolean handle(JmsMessage msg) { return gft.handle(msg);}

	private String determineHostName() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private void setLocale(Props props) {
		if (props.getString("timezone", null) != null)
			TimeZone.setDefault(TimeZone.getTimeZone(props.getString("timezone")));
		if (props.getString("locale", null) != null)
			Locale.setDefault(new Locale(props.getString("locale")));
	}

	private void addServlets(Props props) {
		MultiListener listener = listeners.get("main");
		statusItems.add(new QueueStatus(this, listener, "input"));
		statusItems.add(new QueueStatus(this, listener, "error"));
		statusItems.add(new ProblematicPollerFiles(this));
		statusItems.add(new InProgressPollerFiles(this));
		statusItems.add(new NotListeningListenerThreads(this,listeners));
		addServlet("/listener", new ListenerServlet(listeners, props));
		addServlet("/channel", new ChannelServlet(this));
		addServlet("/poller", new PollerServlet(this));
		addServlet("/dir", new DirectoryServlet(this));
		addServlet("/message", new JmsMessageServlet(this, props));
		addServlet("/config", new ConfigServlet(this));
		//handlerMap.put("/restart", new RestartServlet(gft));
		addServlet("/reset", new ResetServlet(this));
		addServlet("/reload", new ReloadServlet(this));
		//handlerMap.put("/shutdown", new ShutdownServlet(gft));
		addServlet("/encrypt", new EncryptServlet(this));
		addServlet("default", new HomeServlet(this));
		for (StatusItem item: statusItems)
			addServlet("/"+item.getUrl(), item);
	}

	private void loadListeners(Props props) {
		SimpleProps context = new SimpleProps();
		context.put(topname, this);
		for (String lname : props.getProps("listener").keys()) {
			Props listenerprops = props.getProps("listener." + lname);
			String queueSystemName = listenerprops.getString("queueSystem", "main");
			MultiListener listener = new MultiListener(getQueueSystem(queueSystemName), this, listenerprops, context);
			listeners.put(lname, listener);
		}
	}

	private void loadQueuesSystems(Props props) {
		Props qmhostProps = props.getProps("mq.host");
		for (String name : qmhostProps.keys()) {
			Props qmprops = qmhostProps.getProps(name);
			String type = qmprops.getString("type");
			if ("ActiveMq".equals(type))
				queueSystem.put(name, new ActiveMqSystem(qmprops));
			else if ("Jms".equals(type))
				queueSystem.put(name, new JmsSystem(qmprops));
			else
				throw new RuntimeException("Unknown type of queueing system " + type);
		}
	}

	public void addContext(HashMap<String, Object> root) {
		root.put("wrapper", this);
		root.put("gft", getCurrentGft());
		root.put("channels", getCurrentGft().channels);
		root.put("pollers", getCurrentGft().pollers);
		root.put("listeners", listeners);
		root.put("modules", getModuleInfo());
		root.put("statusItems", statusItems);
		for (StatusItem item :statusItems)
			item.refresh();
	}

	public String getMainQueue() {
		for (MultiListener l : listeners.values())
			return l.getInputQueue();
		throw new RuntimeException("No main queue defined");
	}
	public JmsSystem getQueueSystem(String name) {
		JmsSystem result = queueSystem.get(name);
		if (result==null)
			throw new RuntimeException("Unknown queueSystem "+name);
		return result;
	}

	public String reload() {
		if (gft!=null) {
			LogService.log("info", "ReloadingContainer", getTopname().toUpperCase() + "-Service", getHostName(), "Reloading " + getTopname().toUpperCase());
			logger.info("Reloading GftContainer on host "+getHostName());
		}
		return loadGft(false);
	}

	private synchronized String loadGft(boolean continueIfBroken) {
		GftContainer oldGft=gft;

		Props topProps = new SimpleProps(this.configfile);
		Props props = topProps.getProps(this.topname);

		GftContainer newGft=new GftContainer(this, topname, topProps);

		if (newGft.configBroken && ! continueIfBroken) {
			return "TODO: reason";
		}
		gft=newGft;
		gft.start();
		if (oldGft!=null) {
			LogService.log("info", "ReloadingContainer", getTopname().toUpperCase() + "-Service", getHostName(), "Start of new Container successful, now stopping old Container");
			oldGft.stop();
		}
		return null;
	}

	public void addServlet(String url, AbstractServlet servlet) {
		admin.addServlet(url, servlet);
	}

	public void start() {
		logger.info("Starting GftWrapper on host " + getHostName());
		for (MultiListener q : listeners.values() )
			q.start();
		admin.startListening();
	}

	private void startJamonThread(Props props) {
		this.jamonThread = new JamonThread(props);
		Thread t = new Thread(jamonThread);
		t.setDaemon(true);
		t.start();
	}

	public void join() {
		admin.join();
	}

	public void reset() {
		logger.info("Resetting GftContainer on host " + getHostName());
		LogService.log("info", "ResettingContainer", getTopname().toUpperCase() + "-Service", getHostName(), "Reset called for " + getTopname().toUpperCase() + " Service");
		JamonUtil.jamonLog(gft.props, "RESET called, dumping all statistics");
		jamonThread.reset();
	}

	public void stop() {
		logger.info("Stopping GftContainer on host " + getHostName());
		LogService.log("info", "StoppingContainer", getTopname().toUpperCase() + "-Service", getHostName(), "Stopping " + getTopname().toUpperCase() + " Service");
		JamonUtil.jamonLog(gft.props, "STOP called, dumping all statistics");
		jamonThread.stop();
		for (MultiListener q : listeners.values() )
			q.stop();
		for (JmsSystem q : queueSystem.values())
			q.stop();
		admin.stopListening();
	}

	// This method is called from the constructor of the GFT, before it reads the channels etc
	void initGftFromModules(GftContainer gft) {
		for (Module mod: modules.values())
			mod.initGft(gft);
	}

	@SuppressWarnings("unchecked")
	private void addDynamicModules(Props props) {
		//addModule(FileTransferModule.class, props);
		for (Class<?> cls: jarloader.getMainClasses()) {
			addModule((Class<? extends Module>) cls, props);
		}
	}


	private void addModule(Class<? extends Module> cls, Props props) {
		boolean disabled = props.getBoolean("module."+cls.getSimpleName()+".disabled", false);
		try {
			if (disabled)
				return;
			Module mod=null;
			Constructor<?> cons=ReflectionUtil.getConstructor(cls, new Class<?>[] {GftWrapper.class, Props.class});
			if (cons!=null)
				mod= (Module) ReflectionUtil.createObject(cons, new Object[] {this, props});
			else {
				// use default constructor
				cons=ReflectionUtil.getConstructor(cls, new Class<?>[] {});
				mod= (Module) ReflectionUtil.createObject(cons, new Object[] {});
			}
			modules.put(mod.getName(), mod);
		} catch (Exception e) {
			throw new RuntimeException("Could not load module class "+cls.getSimpleName(), e);
		}
	}
	// This method is to give modules a way to set a cryptographic key very early in startup process.
	// especially before the Host list with encrypted passwords is initialized
	private void loadModuleSpecificCryptoKey() {
		for (Module mod: modules.values()) {
			CryptoUtil.checkKeySetter(mod);
		}
	}


	public List<ModuleInfo> getModuleInfo() {
		return jarloader.getModuleInfo();
	}


	public String getVersion() {
		InputStream in = GftContainer.class.getResourceAsStream("/version.properties");
		if (in == null)
			return "unknown-version";
		Properties props = new Properties();
		try {
			props.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return props.getProperty("project.version");
	}
}
