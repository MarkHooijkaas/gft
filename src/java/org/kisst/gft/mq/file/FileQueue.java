package org.kisst.gft.mq.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kisst.gft.mq.MqMessage;
import org.kisst.gft.mq.MqQueue;

public class FileQueue implements MqQueue {
	private final FileQueueSystem system;
	//private final Props props;
	private final String queue;
	private final File fulldir;
	
	FileQueue(FileQueueSystem system, String queue) {
		this.system=system;
		//this.props=props;
		this.queue=queue;
		this.fulldir=new File(system.getBaseDir(),queue);
		if (fulldir.exists()) {
			if (! fulldir.isDirectory())
				throw new RuntimeException("Could not use queue directory "+fulldir+" because it is not a directory");
		}
		else
			fulldir.mkdirs();
	}
	@Override public String toString() { return "FileQueue("+queue+")"; }
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

	public void send(String data) {
		// TODO Auto-generated method stub
	}
}

