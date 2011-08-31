package org.kisst.gft.filetransfer;

import java.util.regex.Pattern;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.util.XmlNode;

public class FileTransferTask extends BasicTask {
	public final Channel channel;
	public final String srcpath;
	public final String destpath;
	public final XmlNode message;
	public final String replyTo;
	public final String correlationId;
	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");

	public FileTransferTask(GftContainer gft, String data, String replyTo, String correlationId) {
		super(gft, null); // TODO
		message=new XmlNode(data);
		XmlNode input=message.getChild("Body/transferFile");
		
		this.channel=gft.getChannel(input.getChildText("kanaal"));
		if (channel==null)
			throw new FunctionalException("Could not find channel with name "+input.getChildText("kanaal"));
		// Strip preceding slashes to normalize the path.
		String file=input.getChildText("bestand");

		if (file.length()>1024)
			throw new FunctionalException("Filename length should not exceed 1024 characters");
		if (! validCharacters.matcher(file).matches())
			throw new FunctionalException("Filename should only contain alphanumeric characters / . - or _");
		if (file.indexOf("..")>=0)
			throw new FunctionalException("Filename ["+file+"] is not allowed to contain .. pattern");

		this.srcpath=channel.getSrcPath(file, this);
		this.destpath=channel.getDestPath(file, this);
		this.replyTo=replyTo;
		this.correlationId=correlationId;
		for (String key : channel.getContext().keySet())
			getContext().put(key,channel.getContext().get(key));
	}

	public void run() { channel.run(this); }
}
