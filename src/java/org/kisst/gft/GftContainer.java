package org.kisst.gft;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.action.Action;
import org.kisst.gft.action.HttpHost;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.RemoteScpAction;
import org.kisst.gft.filetransfer.StartFileTransferTask;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.QueuePoller;
import org.kisst.gft.mq.file.FileSystem;
import org.kisst.util.ReflectionUtil;

public class GftContainer {
	private final FileSystem fs=new FileSystem("testdata");
	private final MessageHandler starter = new StartFileTransferTask(this); 
	public Props props;
	
	private final HashMap<String, Channel> channels= new LinkedHashMap<String, Channel>();
	private final HashMap<String, Action>   actions= new LinkedHashMap<String, Action>();
	private final HashMap<String, HttpHost>   hosts= new LinkedHashMap<String, HttpHost>();
	private final HashMap<String, QueuePoller> pollers= new LinkedHashMap<String, QueuePoller>();

	public void init(Props props) {
		this.props=props;
		channels.clear();
		actions.put("copy", new RemoteScpAction());

		if (props.get("gft.http.host",null)!=null) {
			Props hostProps=props.getProps("gft.http.host");
			for (String name: hostProps.keySet())
				hosts.put(name, new HttpHost(hostProps.getProps(name)));
		}

		if (props.get("gft.poller",null)!=null) {
			Props pollerProps=props.getProps("gft.poller");
			for (String name: pollerProps.keySet()) {
				pollers.put(name, new QueuePoller(name, fs,starter, pollerProps.getProps(name)));
			}
		}
		
		Props actionProps=props.getProps("gft.action");
		for (String name: actionProps.keySet()) {
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

		System.out.println(props);
		Props channelProps=props.getProps("gft.channel");
		for (String name: channelProps.keySet())
			channels.put(name, new Channel(this, channelProps.getProps(name)));

		for (String name: channels.keySet())
			System.out.println(name+"\t"+channels.get(name));
		for (String name: actions.keySet())
			System.out.println(name+"\t"+actions.get(name));
		for (String name: hosts.keySet())
			System.out.println(name+"\t"+hosts.get(name));			
		for (String name: pollers.keySet())
			System.out.println(name+"\t"+pollers.get(name));			
	}
	public Channel getChannel(String name) { return channels.get(name); }
	public Action getAction(String name) { return actions.get(name); }
	public HttpHost getHost(String name) { return hosts.get(name); }

	public void run() {
		for (QueuePoller p : pollers.values() )
			new Thread(p).start();
	}
	
	public static void main(String[] args) {
		if (args.length!=1)
			throw new RuntimeException("usage: GftContainer <config file>");
		SimpleProps props=new SimpleProps();
		props.load(args[0]);
		GftContainer gft= new GftContainer();
		gft.init(props);
		gft.run();
	}
}
