package org.kisst.gft.admin;

import org.kisst.gft.StatusItem;
import org.kisst.jms.JmsSystem;
import org.kisst.mq.QueueManager;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;



public class QueueStatus extends StatusItem {
	private final MQQueueManager qmgr;
	private final String baseName;
	
	public QueueStatus(JmsSystem jmsSystem, String queuename) {
		super(queuename.substring(queuename.lastIndexOf('/')+1));
		
		qmgr=jmsSystem.createMQQueueManager();
		QueueManager qm = new QueueManager(qmgr);
		MQQueue q1 = qm.getQueue(queuename, MQConstants.MQOO_INQUIRE);
		this.baseName=qm.getTargetQueue(q1);
	}

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

	
	
	@Override public void refreshData() {
		this.problemCount=depth();
	}

}
