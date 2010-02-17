package org.kisst.gft;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.PropertyConfigurator;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.HttpHost;
import org.kisst.gft.admin.AdminServer;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.RemoteScpAction;
import org.kisst.gft.filetransfer.StartFileTransferTask;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.QueueListener;
import org.kisst.gft.mq.QueueSystem;
import org.kisst.gft.mq.file.FileQueueSystem;
import org.kisst.gft.mq.jms.ActiveMqSystem;
import org.kisst.gft.mq.jms.JmsSystem;
import org.kisst.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GftContainer {
	private final static Logger logger=LoggerFactory.getLogger(GftContainer.class); 

	private final MessageHandler starter = new StartFileTransferTask(this); 
	private final AdminServer admin=new AdminServer(this);
	public Props props;
	
	public final HashMap<String, Channel> channels= new LinkedHashMap<String, Channel>();
	public final HashMap<String, Action>   actions= new LinkedHashMap<String, Action>();
	public final HashMap<String, HttpHost>   hosts= new LinkedHashMap<String, HttpHost>();
	public final HashMap<String, QueueSystem> queuemngrs= new LinkedHashMap<String, QueueSystem>();
	public final HashMap<String, QueueListener>  listeners= new LinkedHashMap<String, QueueListener>();

	public void init(Props props) {
		this.props=props;
		channels.clear();
		actions.put("copy", new RemoteScpAction());

		if (props.get("gft.http.host",null)!=null) {
			Props hostProps=props.getProps("gft.http.host");
			for (String name: hostProps.keys())
				hosts.put(name, new HttpHost(hostProps.getProps(name)));
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
			String classname=p.getString("class", name);
			if (classname.indexOf('.')<0)
				classname="org.kisst.gft.action."+classname;
			Constructor<?> c=ReflectionUtil.getConstructor(classname, new Class<?>[] {GftContainer.class, Props.class} );
			Action act;
			if (c==null)
				act=(Action) ReflectionUtil.createObject(classname);
			else
				act=(Action) ReflectionUtil.createObject(c, new Object[] {this, p} );
			actions.put(name, act);
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
			for (String name: hosts.keySet())
				logger.info("Host {}\t{}",name,hosts.get(name));			
			for (String name: listeners.keySet())
				logger.info("Listener {}\t{}",name,listeners.get(name));
		}
	}
	public Channel getChannel(String name) { return channels.get(name); }
	public Action getAction(String name) { return actions.get(name); }
	public HttpHost getHost(String name) { return hosts.get(name); }

	public void run() {
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

	
	
	public static void main(String[] args) {
		if (args.length!=1)
			throw new RuntimeException("usage: GftContainer <config file>");
		File configfile=new File(args[0]);
		PropertyConfigurator.configure(configfile.getParent()+"/log4j.properties");
		SimpleProps props=new SimpleProps();
		props.load(configfile);
		logger.info("Starting GftContainer");
		if (logger.isDebugEnabled()){
			logger.debug("Starting GftContainer with props {}", props.toString());
		}
		GftContainer gft= new GftContainer();
		gft.init(props);
		gft.run();
		logger.info("GFT stopped");
	}
}
