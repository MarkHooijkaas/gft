package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.LayeredProps;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.cfg4j.XmlNodeProps;
import org.kisst.gft.GftContainer;
import org.kisst.util.XmlNode;

public class FileTransferData {
	public final GftContainer gft;
	public final Channel channel;
	public final String file;
	public final Props props;
	public final XmlNode message;
	
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
	
	public Props getProps(Props actionProps) {
		SimpleProps props=new SimpleProps();
		props.put("action", actionProps);
		props.put("file", file);
		props.put("channel", channel.props);
		props.put("soap", new XmlNodeProps(message));
		LayeredProps result = new LayeredProps();
		result.addLayer(props);
		result.addLayer(actionProps);
		result.addLayer(channel.props);
		return result;
	}

}
