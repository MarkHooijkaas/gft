package org.kisst.gft.mq.file;

import java.io.File;

import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MqSystem;

public class FileQueueSystem implements MqSystem {
	private final File basedir;
	private final Props props;
	public FileQueueSystem(Props props) {
		this.props=props;
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
		return new FileQueue(this,props.getProps("queue."+name));
	}
}
