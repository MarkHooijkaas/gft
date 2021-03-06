package org.kisst.gft.action;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.ssh.SshFileServer;
import org.kisst.gft.ssh.SshHost;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshAction implements Action {
	private final static Logger logger=LoggerFactory.getLogger(SshAction.class);
	private final GftContainer gft;
	private final String commandTemplate;
	private final SshHost host;
	private final boolean safeToRetry;
	
	public SshAction(GftContainer gft, Props props) {
		this.gft=gft;
		commandTemplate =props.getString("command");
		String hostname=props.getString("host");
		 FileServer server = gft.getFileServer(hostname);
		 if (server instanceof SshFileServer)
			this.host= ((SshFileServer)server).getSshHost();
		 else
			 throw new RuntimeException("Host "+hostname+" is not an SshFileServer");
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	@Override public boolean safeToRetry() { return safeToRetry; }

	@Override public void execute(Task task) {
		BasicTask basictask= (BasicTask) task;
		String command=gft.processTemplate(commandTemplate, basictask.getActionContext(this));
		logger.info("ssh call to {} with command {}", host, command);
		String result=host.call(command);
		logger.info("ssh result {}",result);
	}

}
