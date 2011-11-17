package org.kisst.gft.task;

import java.io.File;

import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.jms.JmsMessage;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.XmlNode;


public class JmsXmlTask extends BasicTask {
	public final JmsMessage msg;
	public final XmlNode message;
	public final XmlNode content; 		
	
	public JmsXmlTask(GftContainer gft, TaskDefinition taskdef, JmsMessage msg) {
		super(gft, taskdef);
		this.message=new XmlNode(msg.getData());
		this.content=message.getChild("Body").getChildren().get(0);
		this.msg=msg;
	}
	
	// TODO: public TaskDefinition getTaskDefinition() { return taskdef; }

	public void save() {  throw new RuntimeException("save not implemented yet"); }
	
	public SimpleProps getActionContext(Action action) {
		SimpleProps result=getContext().shallowClone();
		result.put("action", action);
		return result;
	}
	
	private File  tempFile=null;
	public File getTempFile(String filename) {
		if (tempFile!=null)
			return tempFile;
		File nieuwTempDir = gft.createUniqueDir(getTaskDefinition().getName());
		tempFile = new File(nieuwTempDir,filename);
		return tempFile;
	}
}
