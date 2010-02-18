package org.kisst.gft.mq.file;

import java.io.File;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.QueueListener;
import org.kisst.gft.mq.QueueSystem;

public class FileQueueSystem implements QueueSystem {
	private final File basedir;
	//private final Props props;
	public FileQueueSystem(Props props) {
		//this.props=props;
		this.basedir=new File(props.getString("basedir","queues"));
		if (this.basedir.exists()) {
			if (! this.basedir.isDirectory())
				throw new RuntimeException("Could not use queue directory "+basedir+" because it is not a directory");
		}
		else
			this.basedir.mkdirs();
	}
	File getBaseDir() {return basedir; }
	
	public FileQueue getQueue(String name) {
		return new FileQueue(this,name);
	}
	public QueueListener createListener(Props props) {
		return new FileListener(this, props);
	}
	public void stop() { }
}
