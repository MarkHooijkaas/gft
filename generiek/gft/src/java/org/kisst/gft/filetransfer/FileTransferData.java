package org.kisst.gft.filetransfer;

import org.kisst.cfg4j.LayeredProps;
import org.kisst.cfg4j.Props;
import org.kisst.cfg4j.SimpleProps;
import org.kisst.cfg4j.XmlNodeProps;
import org.kisst.gft.GftContainer;
import org.kisst.util.XmlNode;

public class FileTransferData {
	public final GftContainer gft;
	public final Channel kanaal;
	public final String bestand;
	public final Props props;
	public final XmlNode message;
	
	public FileTransferData(GftContainer gft, String data) {
		this.gft=gft;
		message=new XmlNode(data);
		XmlNode input=message.getChild("Body/transferFile");
		
		this.kanaal=gft.getChannel(input.getChildText("kanaal"));
		this.bestand=input.getChildText("bestand");
		SimpleProps p = new SimpleProps();
		p.put("message", message);
		p.put("bestand", bestand);
		p.put("kanaal", kanaal.props);
		p.put("soap", new XmlNodeProps(message));
		props=p;
	}
	
	public Props getProps(Props actionProps) {
		SimpleProps p=new SimpleProps();
		p.put("action", actionProps);
		LayeredProps result = new LayeredProps();
		result.addLayer(p);
		result.addLayer(props);
		result.addLayer(actionProps);
		result.addLayer(kanaal.props);
		return result;
	}

}
