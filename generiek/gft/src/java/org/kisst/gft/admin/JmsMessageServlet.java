package org.kisst.gft.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kisst.gft.GftContainer;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.MultiListener;

public class JmsMessageServlet extends BaseServlet {
	public JmsMessageServlet(GftContainer gft) { super(gft);	}

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
			session = ((JmsSystem)gft.queueSystem).getConnection().createSession(true, Session.AUTO_ACKNOWLEDGE);
			String q;
			if ("input".equals(queuename))
				q=lstnr.getQueue();
			else if ("error".equals(queuename))
				q=lstnr.getErrorQueue();
			else if ("retry".equals(queuename))
				q=lstnr.getRetryQueue();
			else
				throw new RuntimeException("Invalid queuename "+queuename);

			out.println("<pre>");
			Queue destination = session.createQueue(q);
			QueueBrowser browser = session.createBrowser(destination, "JMSMessageID = '"+msgid+"'");
			Enumeration<?> e = browser.getEnumeration();
			while (e.hasMoreElements()) {
				Object o = e.nextElement();
				if ( o instanceof TextMessage) {
					TextMessage msg = (TextMessage) o; 
					String txt=msg.getText();
					txt= txt.replaceAll("&", "&amp;");
					txt= txt.replaceAll(">", "&gt;");
					txt= txt.replaceAll("<", "&lt;");
					out.println(txt);
				}
			}
			out.println("</pre>");
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
