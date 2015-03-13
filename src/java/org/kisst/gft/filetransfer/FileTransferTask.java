package org.kisst.gft.filetransfer;

import java.io.File;
import java.util.regex.Pattern;

import org.kisst.gft.filetransfer.action.DestinationFile;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.JmsXmlTask;
import org.kisst.jms.JmsMessage;
import org.kisst.util.exception.BasicFunctionalException;

public abstract class FileTransferTask extends JmsXmlTask implements SourceFile, DestinationFile {
	public final Channel channel;
	//public final String srcpath;
	//public final String destpath;
	public final String filename;
	
	private final FileLocation src;
	private final FileLocation dest;
	private final FileLocation finaldest;
	
	private static Pattern validCharacters = Pattern.compile("[A-Za-z0-9./_-]*");

	public FileTransferTask(Channel channel, JmsMessage msg) {
		super(channel.gft, channel, msg); 
		
		this.channel= channel; 
		//if (channel==null)
		//	throw new FunctionalException("Could not find channel with name "+getContent().getChildText("kanaal"));
		// Strip preceding slashes to normalize the path.
		this.filename = getFilename(); 

		if ( filename.length()>1024)
			throw new BasicFunctionalException("Filename length should not exceed 1024 characters, in channel "+channel.getName()+", filename "+filename);
		if (! validCharacters.matcher(filename).matches())
			throw new BasicFunctionalException("Filename should only contain alphanumeric characters / . - or _  in channel "+channel.getName()+", filename ["+filename+"]");
		if (filename.indexOf("..")>=0)
			throw new BasicFunctionalException("Filename ["+filename+"] is not allowed to contain .. pattern, , in channel "+channel.getName());
		this.src=new FileLocation(channel.getSourceFile(), filename);
		this.dest=new FileLocation(channel.getDestinationFile(), filename);
		if (channel.getFinalDestinationFile()==null)
			this.finaldest=null;
		else
			this.finaldest=new FileLocation(channel.getFinalDestinationFile(),filename);
	}

	abstract protected  String getFilename();
	@Override public String getIdentification() { return getFilename(); }


	public String toString() { return toString(" from:"+src+", to: "+dest);
} 
	public void run() { channel.run(this); }
	public File getTempFile() { return getTempFile(filename); }

	@Override public FileLocation getSourceFile() { return src;}
	@Override public FileLocation getDestinationFile() { return dest; }
	@Override public FileLocation getFinalDestinationFile() { return finaldest; }
}
