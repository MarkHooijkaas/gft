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

package org.kisst.gft.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileTransferData;
import org.kisst.gft.task.Task;


public class LogCompleted  extends HttpAction {
	
	private static String template="<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
		+"<SOAP:Body>\n"
		+"  <vastleggenLogging xmlns=\"esb-intern:Logging-1.0\">\n"
		+"    <Header>\n"
		+"      <securitycode>1</securitycode>\n"
		+"      <gebruikerscode>2</gebruikerscode>\n"
		+"      <datum>${datum}</datum>\n"
		+"      <tijd>${tijd}</tijd>\n"
		+"      <omgevingscode>${omgeving}</omgevingscode>\n"
		+"      <transactiecode>3</transactiecode>\n"
		+"    </Header>\n"
		+"    <parameters>\n"
		+"      <systeem>GFT</systeem>\n"
		+"      <moment>${moment}</moment>\n"
		+"      <func_proc>${func}</func_proc>\n"
		+"      <tech_proc>${tech}</tech_proc>\n"
		+"      <uitvoerder>Srv_GFT</uitvoerder>\n"
		+"      <eventType_code>${event}</eventType_code>\n"
		+"      <organisatie>ICT</organisatie>\n"
		+"      <details>${details}</details>\n"
		+"      <niveau>${niveau}</niveau>\n"
		+"    </parameters>\n"
		+"  </vastleggenLogging>\n"
		+"</SOAP:Body>\n"
		+"</SOAP:Envelope>";
	
	public LogCompleted(GftContainer gft, Props props) {
		super(gft, props);
	}

	private static SimpleDateFormat momentFormatter=new SimpleDateFormat("yyyyMMddHHmmssSSS000"); 
	private static SimpleDateFormat dateFormatter=new SimpleDateFormat("yyyyMMdd"); 
	private static SimpleDateFormat timeFormatter=new SimpleDateFormat("HHmmss"); 
	
	public String getBody(Task task) {
		FileTransferData ftdata = (FileTransferData) task.getData();
		HashMap<String,Object> context=new HashMap<String,Object>();
		Date now=new Date();
		synchronized (momentFormatter) {
			context.put("datum", dateFormatter.format(now));
			context.put("tijd", timeFormatter.format(now));
			context.put("moment", momentFormatter.format(now));
		}
		context.put("omgeving", props.get("omgeving"));
		context.put("func", ftdata.channel.name);
		fillContext(context, task, ftdata); 
		
		return gft.processTemplate(template, context);
	}

	protected void fillContext(HashMap<String,Object> context, Task task, FileTransferData ftdata) {
		context.put("details", "GFT geslaagd, kanaal: "+ftdata.channel.name+", bestand: "+ftdata.srcpath+ ", van: "+ftdata.channel.src+"/"+ftdata.srcpath+" naar: "+ftdata.channel.dest+"/"+ftdata.destpath);
		context.put("event", "completed");
		context.put("niveau", "info");
		context.put("tech", "done");
	}
}
