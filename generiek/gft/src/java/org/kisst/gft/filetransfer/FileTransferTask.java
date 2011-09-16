package org.kisst.gft.filetransfer;

import java.io.File;
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
	public final String filename;
	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");

	public FileTransferTask(GftContainer gft, String data, String replyTo, String correlationId) {
		super(gft, null); // TODO
		message=new XmlNode(data);
		XmlNode input=message.getChild("Body/transferFile");
		
		this.channel=gft.getChannel(input.getChildText("kanaal"));
		if (channel==null)
			throw new FunctionalException("Could not find channel with name "+input.getChildText("kanaal"));
		// Strip preceding slashes to normalize the path.
		filename =input.getChildText("bestand");

		if ( filename.length()>1024)
			throw new FunctionalException("Filename length should not exceed 1024 characters");
		if (! validCharacters.matcher(filename).matches())
			throw new FunctionalException("Filename should only contain alphanumeric characters / . - or _");
		if (filename.indexOf("..")>=0)
			throw new FunctionalException("Filename ["+filename+"] is not allowed to contain .. pattern");
		this.srcpath=channel.getSrcPath(filename, this);
		this.destpath=channel.getDestPath(filename, this);
		this.replyTo=replyTo;
		this.correlationId=correlationId;
		for (String key : channel.getContext().keys())
			getContext().put(key,channel.getContext().get(key));
	}

	public void run() { channel.run(this); }
	
	private File  tempFile=null;
	public File getTempFile() {
		if (tempFile!=null)
			return tempFile;
		File nieuwTempDir = gft.createUniqueDir(channel.name);
		tempFile = new File(nieuwTempDir,filename);
		return tempFile;
	}
}
