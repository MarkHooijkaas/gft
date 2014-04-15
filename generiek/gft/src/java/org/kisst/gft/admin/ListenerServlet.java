package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.jms.ControlMessage;
import org.kisst.jms.JmsListener;
import org.kisst.jms.MultiListener;
import org.kisst.util.XmlNode;

public class ListenerServlet extends BaseServlet {
	public ListenerServlet(GftContainer gft) { super(gft);	}

	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
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
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		out.println("<h1>Listener "+name+"</h1>");


		
		MultiListener lstnr = (MultiListener) gft.listeners.get(name);
		out.println("<h2>Status of Listener threads</h2>");
		out.println("<ul>");
		for (JmsListener l :lstnr.listeners)
			out.println("<li>"+l.getStatus()+"</l>");
		out.println("</ul>");
		
		Session session=null;
		try {
			session = lstnr.getQueueSystem().getConnection().createSession(true, Session.AUTO_ACKNOWLEDGE);
			for (String qname:queuenames.split("[,]")) {
				String q;
				if ("input".equals(qname))
					q=lstnr.getQueue();
				else if ("error".equals(qname))
					q=lstnr.getErrorQueue();
				//else if ("retry".equals(qname))
				//	q=lstnr.getRetryQueue();
				else
					throw new RuntimeException("Invalid queuename "+qname);

				out.println("<h2>"+q+"</h2>");
				out.println("<table>");
				out.println("<tr><td><b>time</b></td><td><b>channel</b></td><td><b>id</b></td><td><b>action</b></td><td><b>error</b></td></tr>");
				Queue destination = session.createQueue(q);
				QueueBrowser browser = session.createBrowser(destination);
				Enumeration<?> e = browser.getEnumeration();
				while (e.hasMoreElements()) {
					Message msg = (Message) e.nextElement();
					String channel=msg.getStringProperty("state_CHANNEL");
					String id=msg.getStringProperty("state_ID");

					try {
						if (ControlMessage.isStartMessage(msg))
							channel="Start message";
						else if (ControlMessage.isStopMessage(msg))
							channel="Stop message";
						else {
							if (channel==null) {
								// TODO: handle all type of messages
								XmlNode xml=new XmlNode(((TextMessage)msg).getText()).getChild("Body").getChildren().get(0);
								channel=xml.getChildText("kanaal");
								id=xml.getChildText("bestand");
							}
						}
					}
					catch (RuntimeException ex) {
						channel="Unknown format";
						id=msg.getJMSMessageID();
					}
					out.println("<tr><td width=150> "+format.format(new Date(msg.getJMSTimestamp()))+"</td>");
					out.println("<td>"+channel+"</td>");
					out.println("<td><a href=\"/message/"+name+"/"+qname+"/"+msg.getJMSMessageID()+"\">"+id+"</a></td>");
					out.println("<td>"+msg.getStringProperty("state_LAST_ACTION")+"</td>");
					out.println("<td>"+msg.getStringProperty("state_LAST_ERROR")+"</td>");
					out.println("</tr>");
					
				}
				out.println("</table>");
			}
		}
		catch (JMSException e) {throw new RuntimeException(e); }
		finally {
			try {
				if (session!=null)
					session.close();
			}
			catch (JMSException e) {throw new RuntimeException(e); }
		}
	}

}
