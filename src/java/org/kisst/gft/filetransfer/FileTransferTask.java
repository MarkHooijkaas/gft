package org.kisst.gft.filetransfer;

import java.io.File;
import java.util.regex.Pattern;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.task.JmsXmlTask;
import org.kisst.jms.JmsMessage;

public class FileTransferTask extends JmsXmlTask {
	public final Channel channel;
	public final String srcpath;
	public final String destpath;
	public final String filename;
	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");

	public FileTransferTask(Channel channel, JmsMessage msg) {
		super(channel.gft, channel, msg); 
		
		this.channel= channel; 
		if (channel==null)
			throw new FunctionalException("Could not find channel with name "+getContent().getChildText("kanaal"));
		// Strip preceding slashes to normalize the path.
		filename =getContent().getChildText("bestand");

		if ( filename.length()>1024)
			throw new FunctionalException("Filename length should not exceed 1024 characters");
		if (! validCharacters.matcher(filename).matches())
			throw new FunctionalException("Filename should only contain alphanumeric characters / . - or _");
		if (filename.indexOf("..")>=0)
			throw new FunctionalException("Filename ["+filename+"] is not allowed to contain .. pattern");
		this.srcpath=channel.getSrcPath(filename, this);
		this.destpath=channel.getDestPath(filename, this);
		for (String key : channel.getContext().keys())
			getContext().put(key,channel.getContext().get(key));
	}

	public String toString() { return "FileTransferTask("+srcpath+")"; } 
	public void run() { channel.run(this); }
	public File getTempFile() { return getTempFile(filename); }

}
