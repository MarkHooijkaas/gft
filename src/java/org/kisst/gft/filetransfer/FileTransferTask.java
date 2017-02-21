package org.kisst.gft.filetransfer;

import java.io.File;
import java.util.regex.Pattern;

import org.kisst.gft.filetransfer.action.DestinationFile;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.JmsXmlTask;
import org.kisst.jms.JmsMessage;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.StringUtil;
import org.kisst.util.XmlNode;
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

	public FileTransferTask(Channel channel, String id, JmsMessage msg, XmlNode content) {
		super(channel.gft, channel, id, msg, content); 
		
		this.channel= channel; 
		this.filename = getFilename(); 

		if ( filename.length()>1024)
			throw new BasicFunctionalException("Filename length should not exceed 1024 characters, in channel "+channel.getName()+", filename "+filename);
		if (! validCharacters.matcher(filename).matches())
			throw new BasicFunctionalException("Filename should only contain alphanumeric characters / . - or _  in channel "+channel.getName()+", filename ["+filename+"]");
		if (filename.indexOf("..")>=0)
			throw new BasicFunctionalException("Filename ["+filename+"] is not allowed to contain .. pattern, , in channel "+channel.getName());
		this.src=new FileLocation(channel.getSourceFile(), filename);
		String destfilename=filename;
		if (channel.renamePattern!=null)
			destfilename=replaceFileName(channel.renamePattern, filename);
		this.dest=new FileLocation(channel.getDestinationFile(), destfilename);
		if (channel.getFinalDestinationFile()==null)
			this.finaldest=null;
		else
			this.finaldest=new FileLocation(channel.getFinalDestinationFile(),destfilename);
	}

	private String replaceFileName(String renamePattern, String filename) {
		SimpleProps props=new SimpleProps();
		props.put("filename", filename);
		int pos=filename.lastIndexOf('.');
		if (pos>0) {
			props.put("extension", filename.substring(pos + 1));
			props.put("basename", filename.substring(0, pos));
		}
		renamePattern = StringUtil.substituteDate(renamePattern);
		return StringUtil.substitute(renamePattern,props);
	}

	abstract protected String getFilename();

	@Override public String toString() { return toString(src.getShortString()+"==>"+dest.getShortString()); } 
	@Override public void run() { channel.run(this); }
	@Override public File getTempFile() { return getTempFile(filename); }

	@Override public FileLocation getSourceFile() { return subsituteDynamicPath(src); }
	@Override public FileLocation getDestinationFile() { return subsituteDynamicPath(dest); }
	@Override public FileLocation getFinalDestinationFile() { return subsituteDynamicPath(finaldest); }
}
