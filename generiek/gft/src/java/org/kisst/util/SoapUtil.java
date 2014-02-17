/**
Copyright 2008, 2009 Mark Hooijkaas

This file is part of the RelayConnector framework.

The RelayConnector framework is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The RelayConnector framework is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.kisst.util;


public class SoapUtil {
	//public static final String SoapNamespace ="http://www.w3.org/2003/05/soap-envelope";
	public static final String soapNamespace="http://schemas.xmlsoap.org/soap/envelope/";
	// public static final String SoapNamespace ="http://www.w3.org/2001/12/soap-envelope";

	public static final String wsaNamespace="http://www.w3.org/2005/08/addressing";
	//public static final String wsaAnonymous="http://www.w3.org/2005/08/addressing/anonymous";


	/** Returns true is the NOM node is a SOAP:Fault element
	 *  This is a helper routine that is generally called on the Envelope node, but can
	 *  also be used on the Body node, or the Fault node directly 
	 *  
	 * @param node the NOM node of a SOAP Envelope, Body or Fault
	 * @return the NOM node of the SOAP Fault element, or 0 if it is not 
	 */
	public static boolean isSoapFault(XmlNode node) {
		return getSoapFault(node)!=null;
	}

	/** Returns the NOM node of the SOAP:Fault element, or 0 if XML is not a SOAP:Fault
	 *  This is a helper routine that is generally called on the Envelope node, but can
	 *  also be used on the Body node, or the Fault node directly (in which case it does nothing).
	 *  
	 * @param node the NOM node of a SOAP Envelope, Body or Fault
	 * @return the NOM node of the SOAP Fault element, or 0 if it is not 
	 */
	public static XmlNode getSoapFault(XmlNode node) {
		if ("Envelope".equals(node.getName()))
			node=getBody(node);
		if ("Body".equals(node.getName()))
			node=node.getChild( /*soapNamespace,*/ "?Fault");
		if ("Fault".equals(node.getName()) && soapNamespace.equals(node.getNamespace()))
			return node;
		else 
			return null;
	}

	/** Returns the faultstring of the SOAP:Fault element, or null if XML is not a SOAP:Fault
	 *  This is a helper routine that is generally called on the Envelope node, but can
	 *  also be used on the Body node, or the Fault node directly.
	 *  
	 * @param node the NOM node of a SOAP Envelope, Body or Fault
	 * @return the faultstring, or null if it is not 
	 */
	public static String getSoapFaultMessage(XmlNode  node) {
		XmlNode fault=SoapUtil.getSoapFault(node);
		if (fault==null)
			return null;
		return fault.getChildText("faultstring");
	}

	
	/** Returns the NOM node of the SOAP:Body element
	 *  This is a helper routine that is called on the Envelope node
	 *  
	 * @param node the NOM node of a SOAP Envelope
	 * @return the NOM node of the SOAP Body element, or 0 if it is not 
	 */
	public static XmlNode  getBody(XmlNode  envelope) {
		return envelope.getChild(/* soapNamespace,*/  "Body");
	}

	/** Returns the NOM node of the SOAP:Header element
	 *  This is a helper routine that is called on the Envelope node
	 *  
	 * @param node the NOM node of a SOAP Envelope
	 * @return the NOM node of the SOAP Header element, or 0 if it is not 
	 */
	public static XmlNode  getHeader(XmlNode  envelope) {
		return envelope.getChild(/* soapNamespace,*/ "Header");
	}
	
	/** Returns the NOM node of the first child of the SOAP:Body element
	 *  This is a helper routine that is called on the Envelope node
	 *  
	 * @param node the NOM node of a SOAP Envelope
	 * @return the NOM node of the first child of the SOAP Body. 
	 */
	public static XmlNode  getContent(XmlNode  envelope) {
		return getBody(envelope).getChildren().get(0);
	}

}
