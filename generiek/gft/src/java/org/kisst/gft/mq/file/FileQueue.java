package org.kisst.gft.mq.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.LockedBySomeoneElseException;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.mq.MqQueue;

public class FileQueue implements MqQueue, Runnable {
	private final FileQueueSystem system;
	private final Props props;
	private final String queue;
	private final File fulldir;
	private MessageHandler handler;
	
	FileQueue(FileQueueSystem system, Props props) {
		this.system=system;
		this.props=props;
		this.queue=props.getString("queue");
		this.fulldir=new File(system.getBaseDir(),queue);
		if (fulldir.exists()) {
			if (! fulldir.isDirectory())
				throw new RuntimeException("Could not use queue directory "+fulldir+" because it is not a directory");
		}
		else
			fulldir.mkdirs();
	}
	@Override public String toString() { return "FileSystemQueue("+queue+")"; }
	public String getName() { return queue;}
	public FileQueueSystem getSystem() { return system; }

	File getFile(String filename) {
		return new File(fulldir,filename);
	}
	public List<MqMessage> getAllMessages() {
		List<MqMessage> messages =new ArrayList<MqMessage>();
		File[] files=fulldir.listFiles();
		for (File f :files) {
			if (f.isFile() && ! f.getName().endsWith(".locked")) {
				messages.add(new FileMessage(this,f.getName())); 
				//System.out.println("adding message "+f);
			}
		}
		//System.out.println("returning "+messages.size()+" messages");
		return messages;
	}

	public void pollTillEmpty() {
		List<MqMessage> messages;
		do {
			messages= getAllMessages();
			for (MqMessage msg:messages) {
				handle(msg);
				if (! running)
					return;
			}
		}
		while (messages!=null && messages.size()>0);
	}

	
	private void handle(MqMessage msg) {
		try {
			msg.lock();
		} catch (LockedBySomeoneElseException e) {
			System.out.println("Could not lock "+msg);
			e.printStackTrace();
			return;
		}
		System.out.println(queue+" handling "+msg);
		handler.handle(msg);
		msg.done();
	}
	
	private boolean running=false;
	public void run() {
		long delay=props.getLong("delay",1000);
		running=true;
		while (running) {
			//System.out.println("polling");
			pollTillEmpty();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void send(String data) {
		// TODO Auto-generated method stub

	}
	public void stopListening() { running=false; }
	public void listen(MessageHandler handler) {
		this.handler=handler;
		new Thread(this).start();
	}
	
}

