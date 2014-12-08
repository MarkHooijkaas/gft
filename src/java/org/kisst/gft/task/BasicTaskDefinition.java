package org.kisst.gft.task;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.filetransfer.FileTransferTask;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasicTaskDefinition extends AbstractTaskDefinition {
	final static Logger logger=LoggerFactory.getLogger(BasicTaskDefinition.class); 
	//protected final Action action;

	public BasicTaskDefinition(GftContainer gft, Props props, String defaultActions) {
		super(gft, new ActionList(gft, props, defaultActions), props);
	}
	

}