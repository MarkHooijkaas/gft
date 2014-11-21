package org.kisst.http4j;

import org.kisst.props4j.Props;
import org.kisst.util.XmlNode;

public class HttpSoapCaller extends HttpCaller {
	public HttpSoapCaller(HttpHostMap hostMap, Props props) { super(hostMap, props); }

	public XmlNode soapCall(XmlNode body) {
		String xmlText = body.getPretty();
		String response = httpPost("", xmlText);
		XmlNode result = new XmlNode(response);
		return result.getChild("Body").getChildren().get(0);
	}
	
	public XmlNode createMethod(String name, String namespace) {
		XmlNode envelope = new XmlNode("Envelope", "http://schemas.xmlsoap.org/soap/envelope/");
		envelope.setPrefix("SOAP");
		//XmlNode header = envelope.add("Header");

		XmlNode body = envelope.add("Body");
		XmlNode method=body.add(name, namespace);
		return method;
	}
}
