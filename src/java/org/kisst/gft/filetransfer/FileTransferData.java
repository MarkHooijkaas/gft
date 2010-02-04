package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.gft.GftContainer;
import org.kisst.util.XmlNode;

public class FileTransferData {
	public final GftContainer gft;
	public final Channel channel;
	public final String file;
	public final Props props;
	public XmlNode message;
	
	public FileTransferData(GftContainer gft, String data) {
		this.gft=gft;
		message=new XmlNode(data);
		XmlNode input=message.getChild("Body/FileTransferRequest");
		
		this.channel=gft.getChannel(input.getChildText("kanaal"));
		this.file=input.getChildText("bestand");
		SimpleProps p = new SimpleProps();
		p.readXml(input);
		props=p;
	}

}
