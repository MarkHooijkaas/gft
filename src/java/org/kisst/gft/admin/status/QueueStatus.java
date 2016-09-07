package org.kisst.gft.admin.status;

import java.io.PrintWriter;

import org.kisst.gft.GftContainer;
import org.kisst.gft.GftWrapper;
import org.kisst.gft.admin.ListenerServlet;
import org.kisst.jms.MultiListener;
import org.kisst.mq.QueueManager;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;



public class QueueStatus extends StatusItem {
	private final MQQueueManager qmgr;
	private final String name;
	private final String baseName;
	private final String url;
	private final MultiListener listener;
	
	public QueueStatus(GftWrapper wrapper, MultiListener listener, String queuename) {
		super(wrapper, listener.getQueue(queuename).substring(listener.getQueue(queuename).lastIndexOf('/')+1));
		this.url=listener.getName()+"/"+queuename;
		this.name=queuename;
		qmgr=listener.getQueueSystem().createMQQueueManager();
		this.listener=listener;
		QueueManager qm = new QueueManager(qmgr);
		MQQueue q1 = qm.getQueue(listener.getQueue(queuename), MQConstants.MQOO_INQUIRE);
		this.baseName=qm.getTargetQueue(q1);
		
	}

	public String getUrl() { return url; } 

	public int depth() {
		if (qmgr==null)
			return -1;
    	int openOptions = MQConstants.MQOO_INQUIRE;
    	MQQueue q;
    	try {
    		q = qmgr.accessQueue(this.baseName, openOptions);
    		if (q.getQueueType() ==MQConstants.MQQT_ALIAS) {
    			return -1;
    		}
    		else
    		return q.getCurrentDepth();
    	} catch (MQException e) { e.printStackTrace(); return -1; } // Ignore
    }

	@Override public void writeDetails(PrintWriter out) {
		super.writeDetails(out);
		ListenerServlet.writeQueueMessages(out, listener, name);
	}

	
	
	@Override public void refresh() {
		this.problemCount=depth();
	}

	@Override public String getMessage() { return problemCount+" messages in queue"; }
}
