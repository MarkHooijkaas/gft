package org.kisst.gft.filetransfer;

import java.util.HashMap;
import java.util.Map;

import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.gft.action.Action;
import org.kisst.gft.task.BasicTask;
import org.kisst.util.XmlNode;

public class FileTransferTask extends BasicTask {
	public final GftContainer gft;
	private final SimpleProps vars=new SimpleProps();;
	private final HashMap<String, Object> context=new HashMap<String, Object>();

	public final Channel channel;
	public final String srcpath;
	public final String destpath;
	public final XmlNode message;
	public final String replyTo;
	public final String correlationId;
	
	public FileTransferTask(GftContainer gft, String data, String replyTo, String correlationId) {
		this.gft=gft;
		message=new XmlNode(data);
		XmlNode input=message.getChild("Body/transferFile");
		
		this.channel=gft.getChannel(input.getChildText("kanaal"));
		if (channel==null)
			throw new RuntimeException("Could not find channel with name "+input.getChildText("kanaal"));
		// Strip preceding slashes to normalize the path.
		String file=input.getChildText("bestand");
		this.srcpath=channel.getSrcPath(file);
		this.destpath=channel.getDestPath(file);
		this.replyTo=replyTo;
		this.correlationId=correlationId;
		context.put("global", gft.props.get("gft.global", null));
		context.put("var", vars);
		context.put("task", this);
	}

	public void run() { channel.run(this); }
	
	public Map<String, Object> getContext() { return context; }
	public Map<String, Object> getActionContext(Action action) {
		Map<String, Object> result=new HashMap<String, Object>(context);
		result.put("action", action);
		return result;
	}
}
