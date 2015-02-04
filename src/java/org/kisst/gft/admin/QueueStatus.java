package org.kisst.gft.admin;

import javax.jms.ConnectionFactory;

import org.kisst.gft.StatusItem;
import org.kisst.jms.JmsSystem;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.jms.MQConnectionFactory;



public class QueueStatus extends StatusItem {
	//private final JmsSystem jmsSystem;
	private final String queueName;
	private final MQQueueManager qmgr;
	
	@SuppressWarnings("unchecked")
	public QueueStatus(JmsSystem jmsSystem, String queuename) {
		super(queuename.substring(queuename.lastIndexOf('/')+1));
		//this.jmsSystem=jmsSystem;
		
		this.queueName=getName();
		ConnectionFactory connfac = jmsSystem.getConnectionFactory();
		MQQueueManager tmp=null;
		if (connfac instanceof MQConnectionFactory) {
			MQConnectionFactory mqconnfac =(MQConnectionFactory) connfac;
			MQEnvironment.channel = mqconnfac.getChannel();
			MQEnvironment.port = mqconnfac.getPort();
			MQEnvironment.hostname = mqconnfac.getHostName();
			MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
			try {
				tmp= new MQQueueManager(mqconnfac.getQueueManager());
			} catch (MQException e) { }
		}
		qmgr=tmp;
		//System.out.println(jmsSystem.getQueue(queuename).getClass());
	}

	/*
	private String getBaseName(String name) {
		Session session=null;
		try {
			session = jmsSystem.getConnection().createSession(true, Session.SESSION_TRANSACTED);
			Queue q = session.createQueue(name);
			System.out.println(name);
			System.out.println(q.getClass());
			System.out.println(((com.ibm.mq.jms.MQQueue)q).getBaseQueueName());

			if (q instanceof com.ibm.mq.jms.MQQueue)
				return ((com.ibm.mq.jms.MQQueue)q).getBaseQueueName();
			else
				return name;
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
	*/
	
    @SuppressWarnings("deprecation")
	public int depth() {
		if (qmgr==null)
			return -1;
    	int openOptions = MQC.MQOO_INQUIRE;
    	MQQueue q;
    	try {
    		q = qmgr.accessQueue(this.queueName, openOptions);
    		if (q.getQueueType() ==MQC.MQQT_ALIAS) {
    			System.out.println(q.getName()+"-->"+q.getResolvedObjectString());
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
