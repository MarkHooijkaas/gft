package org.kisst.gft.admin.status;

import org.kisst.gft.GftWrapper;
import org.kisst.gft.admin.ListenerServlet;
import org.kisst.jms.JmsUtil;
import org.kisst.jms.MultiListener;

import javax.jms.*;
import java.io.PrintWriter;
import java.util.Enumeration;


public class QueueStatus extends StatusItem {
	private final String queuename;
	private final String url;
	private final MultiListener listener;
	
	public QueueStatus(GftWrapper wrapper, MultiListener listener, String queuename) {
		super(wrapper, listener.getQueue(queuename).substring(listener.getQueue(queuename).lastIndexOf('/')+1));
		this.url=listener.getName()+"/"+queuename;
		this.queuename=queuename;
		this.listener=listener;
	}

	public String getUrl() { return url; }

	@Override public void writeDetails(PrintWriter out) {
		super.writeDetails(out);
		ListenerServlet.writeQueueMessages(out, listener, queuename);
	}

	@Override public void refresh() {
		this.problemCount=countMessages();
	}

	@Override public String getMessage() { return problemCount+" messages in queue"; }

	public int countMessages() {
		Session session=null;
		try {
			session = listener.getQueueSystem().getConnection().createSession(true, Session.AUTO_ACKNOWLEDGE);
			String q=listener.getQueue(queuename);

			Queue destination = session.createQueue(q);
			QueueBrowser browser = session.createBrowser(destination);
			Enumeration<?> e = browser.getEnumeration();
			int count=0;
			while (e.hasMoreElements()) {
				count++;
				Message msg = (Message) e.nextElement();
			}
			return count;
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
