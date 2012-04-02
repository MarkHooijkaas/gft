package org.kisst.gft.filetransfer;

import java.io.File;
import java.util.regex.Pattern;

import org.kisst.gft.FunctionalException;
import org.kisst.gft.filetransfer.action.DestinationFile;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.JmsXmlTask;
import org.kisst.jms.JmsMessage;

public abstract class FileTransferTask extends JmsXmlTask implements SourceFile, DestinationFile {
	public final Channel channel;
	public final String srcpath;
	public final String destpath;
	public final String filename;
	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");

	public FileTransferTask(Channel channel, JmsMessage msg) {
		super(channel.gft, channel, msg); 
		
		this.channel= channel; 
		//if (channel==null)
		//	throw new FunctionalException("Could not find channel with name "+getContent().getChildText("kanaal"));
		// Strip preceding slashes to normalize the path.
		this.filename = getFilename(); 

		if ( filename.length()>1024)
			throw new FunctionalException("Filename length should not exceed 1024 characters");
		if (! validCharacters.matcher(filename).matches())
			throw new FunctionalException("Filename should only contain alphanumeric characters / . - or _");
		if (filename.indexOf("..")>=0)
			throw new FunctionalException("Filename ["+filename+"] is not allowed to contain .. pattern");
		this.srcpath=calcPath(channel.srcdir, filename);
		this.destpath=calcPath(channel.destdir, filename);
		for (String key : channel.getContext().keys())
			getContext().put(key,channel.getContext().get(key));
	}

	abstract protected  String getFilename();

	public String toString() { return "FileTransferTask("+srcpath+")"; } 
	public void run() { channel.run(this); }
	public File getTempFile() { return getTempFile(filename); }

	@Override public String getSourceFilePath() { return srcpath; }
	@Override public FileServer getSourceFileServer() { return channel.src;}
	@Override public String getDestinationFilePath() { return destpath; }
	@Override public FileServer getDestinationFileServer() { return channel.dest;}

}
