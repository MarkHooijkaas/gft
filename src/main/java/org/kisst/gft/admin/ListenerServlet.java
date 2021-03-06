package org.kisst.gft.admin;

import org.kisst.jms.ControlMessage;
import org.kisst.jms.JmsListener;
import org.kisst.jms.JmsUtil;
import org.kisst.jms.MultiListener;
import org.kisst.props4j.Props;
import org.kisst.servlet4j.AbstractServlet;
import org.kisst.util.XmlNode;

import javax.jms.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

public class ListenerServlet extends AbstractServlet {
	private static int maxErroTextLength=200;
	private final HashMap<String, MultiListener> listeners;

	public ListenerServlet(HashMap<String, MultiListener> listeners, Props props) {
		super(props);
		this.listeners=listeners;
	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		boolean showListeners=true;
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		String name=url.substring("/listener/".length());
		//String queuenames="input,error,retry";
		String queuenames="input,error";
		int pos=name.indexOf("/");
		if (pos>0) {
			queuenames=name.substring(pos+1);
			name=name.substring(0,pos);
			showListeners=false;
		}

		MultiListener lstnr = listeners.get(name);
		if (showListeners) {
			out.println("<h1>Listener "+name+"</h1>");
			out.println("<h2>Status of Listener threads</h2>");
			out.println("<ul>");
			for (JmsListener l :lstnr.listeners)
				out.println("<li>"+l.getStatus()+"</l>");
			out.println("</ul>");
		}
		for (String qname:queuenames.split("[,]"))
			writeQueueMessages(out, lstnr, qname);
	}

	public static void writeQueueMessages(PrintWriter out, MultiListener listener, String qname) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Session session=null;
		try {
			session = listener.getQueueSystem().getConnection().createSession(true, Session.AUTO_ACKNOWLEDGE);
			String q=listener.getQueue(qname);

			int index=1;
			out.println("<h2>"+q+"</h2>");
			out.println("<table>");
			out.println("<tr><th>index</th>");
			out.println("<th style=\"width:200px\">time</th>");
			out.println("<th align=\"left\">channel</th>");
			out.println("<th align=\"left\">msgid</th>");
			out.println("<th align=\"left\">info</th>");
			out.println("<th align=\"left\">action</th>");
			out.println("<th align=\"left\">safe retry</th>");
			out.println("<th align=\"left\">error</th></tr>");
			Queue destination = session.createQueue(q);
			QueueBrowser browser = session.createBrowser(destination);
			Enumeration<?> e = browser.getEnumeration();
			int count=0;
			while (e.hasMoreElements()) {
				count++;
				Message msg = (Message) e.nextElement();
				String channel=msg.getStringProperty("state_CHANNEL");
				String id=msg.getStringProperty("state_ID");

				try {
					if (id!=null && id.startsWith("msgid:ID:")) {
						// fix: old messages were saved with their (original) messageid
						int pos=id.indexOf(',');
						if (pos>0 && pos+1<id.length())
							id=id.substring(pos+1);
						else
							id="";
					}
					if (ControlMessage.isStartMessage(msg))
						channel="Start message";
					else if (ControlMessage.isStopMessage(msg))
						channel="Stop message";
					else {
						if (channel==null) {
							// TODO: handle all type of messages
							XmlNode xml=new XmlNode(((TextMessage)msg).getText()).getChild("Body").getChildren().get(0);
							channel="???"+xml.getChildText("kanaal")+"???";
							id=xml.getChildText("bestand");
						}
					}
				}
				catch (RuntimeException ex) {
					channel="Unknown format";
				}
				String action = msg.getStringProperty("state_ACTION");
				if (action==null)
					action = msg.getStringProperty("state_LAST_ACTION");
				String retrySafely = msg.getStringProperty("state_RETRY_SAFELY");
				if (retrySafely==null)
					retrySafely="unknown";
				String errorText = msg.getStringProperty("state_ERROR");
				if (errorText==null)
					errorText = msg.getStringProperty("state_LAST_ERROR");
				if (errorText!=null) {
					errorText = errorText.replace("++++++++++++++++++++++++++ IE filler+++++++++++++++++++++++++++++++++++++++", "");
					errorText = errorText.replace("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++", "");
					errorText = errorText.replace("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++", "");
					if (errorText.length()>maxErroTextLength)
						errorText=errorText.substring(0,maxErroTextLength);
					errorText=quoteXml(errorText);
				}
				String msgid=msg.getJMSMessageID();
				long timestamp = msg.getJMSTimestamp();
				out.println("<tr>");
				out.println("<td>"+(index++)+"</td>");
				out.println("<td style=\"width: 120px\"><a href=\"/logging/minutes=2&endTime="+(timestamp+60000)+"\">"+format.format(new Date(timestamp))+"</td>");
				out.println("<td><a href=\"/channel/"+channel+"\">"+channel+"</a></td>");
				out.println("<td><a href=\"/message/"+listener.getName()+"/"+qname+"/"+msgid+"\">"+msgid+"</a></td>");
				out.println("<td><a href=\"/logging/hours=1&eventtype="+id+"\">"+id+"</a></td>");
				out.println("<td>"+action+"</td>");
				out.println("<td>"+retrySafely+"</td>");
				out.println("<td>"+errorText+"</td>");
				out.println("</tr>");

			}
			out.println("</table>");
			out.println("Found "+count+" messages");
		}
		catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
		finally {
			try {
				if (session!=null)
					session.close();
			}
			catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
		}
	}

}
