package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import org.kisst.jms.JmsUtil;
import org.kisst.jms.MultiListener;

public class JmsMessageServlet extends BaseServlet {
	public JmsMessageServlet(GftContainer gft) { super(gft);	}

	@SuppressWarnings("unchecked")
	public void handle(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		if (getUser(request, response)==null)
			return;
		response.setContentType("text/html;charset=utf-8");
		//response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String url=request.getRequestURI();
		url =url.substring("/message/".length());
		int pos=url.indexOf("/");
		int pos2=url.indexOf("/", pos+1);
		
		String listenername=url.substring(0,pos);
		String queuename=url.substring(pos+1,pos2);
		String msgid=url.substring(pos2+1);
		
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		out.println("<h1>Queue "+queuename+", msg"+msgid+"</h1>");
		
		MultiListener lstnr = (MultiListener) gft.listeners.get(listenername);
		Session session=null;
		try {
			session = lstnr.getQueueSystem().getConnection().createSession(true, Session.AUTO_ACKNOWLEDGE);
			String q=lstnr.getQueue(queuename);

			Queue destination = session.createQueue(q);
			QueueBrowser browser = session.createBrowser(destination, "JMSMessageID = '"+msgid+"'");
			Enumeration<?> e = browser.getEnumeration();
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				if (o instanceof Message) {
					Message msg=(Message) o;
					out.println("<table><tr><td><b>Property</b></td><td><b>Value</b></td><tr>");
					ArrayList<String> keys = Collections.list(msg.getPropertyNames());
					Collections.sort(keys);
					for (String key: keys) {
						if( key.startsWith( "state_" ) ) {
							Object value = msg.getObjectProperty(key);
							if (key.contains("ERROR") && value instanceof String) {
								String errorText = ((String) value).replace("++++++++++++++++++++++++++ IE filler+++++++++++++++++++++++++++++++++++++++", "");
								errorText = errorText.replace("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++", "");
								errorText = errorText.replace("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++", "");
								value=errorText;
							}
							out.println("<tr><td>"+key+"</td><td>"+value+"</td></tr>");
						}
					} 
					out.println("</table>");
				}
				
				if ( o instanceof TextMessage) {
					out.println("<pre>");
					TextMessage msg = (TextMessage) o; 
					out.println(quoteXml(msg.getText()));
					out.println("</pre>");
				}
			}
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
