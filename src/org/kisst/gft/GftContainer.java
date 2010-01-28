package org.kisst.gft;

import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.filetransfer.RemoteScpAction;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.QueuePoller;
import org.kisst.gft.mq.file.FileSystem;

public class GftContainer {
	private FileSystem fs=new FileSystem("testdata");
	private MqQueue incoming=fs.getQueue("incoming");
	private QueuePoller poller=new QueuePoller(incoming, new RemoteScpAction());
	
	
	public void run() {
		while (true) {
			//System.out.println("polling");
			poller.pollTillEmpty();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length!=1)
			throw new RuntimeException("usage: GftContainer <config file>");
		SimpleProps props=new SimpleProps();
		props.load(args[0]);
		Channel.init(props);
		new GftContainer().run();
	}
}
