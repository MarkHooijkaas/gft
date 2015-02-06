package org.kisst.mq;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;

import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.pcf.MQCFH;
import com.ibm.mq.pcf.MQCFIN;
import com.ibm.mq.pcf.MQCFST;
import com.ibm.mq.pcf.PCFParameter;

public class QueueManager {

	final MQQueueManager qmgr;

	public QueueManager(Props props) {
		MQException.log = null; // To prevent logging the 2033 tot stderr

		Hashtable<String, Object> mqprops = new Hashtable<String, Object>();
		mqprops.put(MQConstants.CHANNEL_PROPERTY, props.getString("channel","SYSTEM.ADMIN.SVRCONN")); // MQEnvironment.channel
		mqprops.put(MQConstants.PORT_PROPERTY, props.getInt("port", 1414)); // MQEnvironment.port
		mqprops.put(MQConstants.HOST_NAME_PROPERTY, props.get("hostName")); // MQEnvironment.hostname
		//mqprops.put(MQConstants.USER_ID_PROPERTY, props.getString("username",null)); // MQEnvironment.userID
		//mqprops.put(MQConstants.PASSWORD_PROPERTY, props.getString("password",null)); // MQEnvironment.password

		String qManager = props.getString("queueManager"); 
		try {
			this.qmgr=new MQQueueManager(qManager, mqprops);
		}
		catch (MQException e) { throw new RuntimeException(e); }
	}
	public QueueManager(MQQueueManager qmgr) { this.qmgr=qmgr; }
	
	public MQQueueManager getMQQueueManager() { return qmgr; }
	
    private static final String DEFAULT_MODEL_QUEUE ="SYSTEM.DEFAULT.MODEL.QUEUE";
    private static final String REPLYQUEUE = "REMOTE.MQSC.*";

	public void runCommand(String command)  {
		MQQueue adminQueue = null;
		MQQueue replyQueue = null;
		MQPutMessageOptions pmo = new MQPutMessageOptions();
		MQGetMessageOptions gmo = new MQGetMessageOptions();

		PCFParameter pcfParameter;
		MQMessage msg = new MQMessage();
		MQCFH mqcfh;

		try {
			// Disable all tracing
			MQEnvironment.disableTracing();
			MQException.log = null;

			// Access queues
			adminQueue = qmgr.accessQueue(qmgr.getCommandInputQueueName(), MQConstants.MQOO_OUTPUT);
			replyQueue = qmgr.accessQueue(DEFAULT_MODEL_QUEUE,
					MQConstants.MQOO_INPUT_EXCLUSIVE, null, REPLYQUEUE, null);

			msg.messageType = MQConstants.MQMT_REQUEST;
			msg.expiry = 100;
			msg.feedback = MQConstants.MQFB_NONE;
			msg.format = MQConstants.MQFMT_ADMIN;
			msg.replyToQueueName = replyQueue.getName();

			MQCFH.write(msg, MQConstants.MQCMD_ESCAPE, 2);
			MQCFIN.write(msg, MQConstants.MQIACF_ESCAPE_TYPE, MQConstants.MQET_MQSC);
			MQCFST.write(msg, MQConstants.MQCACF_ESCAPE_TEXT, command);

			// Put PCF message
			adminQueue.put(msg, pmo);

			// Wait for response
			gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_CONVERT;
			gmo.waitInterval = 10000;

			// Read the response messages
			do {
				msg.messageId = MQConstants.MQMI_NONE;
				replyQueue.get(msg, gmo);
				mqcfh = new MQCFH(msg);
				onMessage(mqcfh);

				for (int i = 0; i < mqcfh.parameterCount; i++) {
					pcfParameter = PCFParameter.nextParameter(msg);
					onParameter(pcfParameter);
				}
			} while (mqcfh.control != MQConstants.MQCFC_LAST);
		} 
		catch (MQException e) { throw new RuntimeException(e); }
		catch (IOException e) { throw new RuntimeException(e); }
		finally {
			try {
				if (replyQueue != null) {
					replyQueue.close();
				}
				if (adminQueue != null) {
					adminQueue.close();
				}
			} catch (MQException e) { throw new RuntimeException(e);}
		}
	}
	

    private void onMessage(MQCFH msg) {
        if (msg.reason != 0) {
            if (msg.reason > 4000)
				System.out.println("ERROR "+msg.reason);
            else
				System.out.println("SYNTAX ERROR "+msg.reason);
        }
    }
    private void onParameter(PCFParameter param) {
        if (param.getParameter() == 3014) {
            System.out.println(param.getStringValue());
        }
    }

	public MQQueue getQueue(String queuename, int options) {
		if (queuename.startsWith("queue://")) {
			int pos=queuename.indexOf('/', 8);
			String qmgrName=queuename.substring(8,pos);
			String qname=queuename.substring(pos+1);
			pos=qname.indexOf('?');
			if (pos>0)
				qname=qname.substring(0,pos-1);
			System.out.println(qmgrName+"\t"+qname);
			queuename=qname;
		}
		try {
			return qmgr.accessQueue(queuename, options);
		}
		catch (MQException e) { throw new RuntimeException(e); }
	}

	public void commit() { 
		try {
			qmgr.commit();
		}
		catch (MQException e) { throw new RuntimeException(e);}
	}

	public void backout() { 
		try {
			qmgr.backout();
		}
		catch (MQException e) { throw new RuntimeException(e);}
	}

	public void close() { 
		try {
			qmgr.backout();
			qmgr.disconnect();
		}
		catch (MQException e) { throw new RuntimeException(e);}
	}

	public static void main(String[] args) {
		File configfile=new File("MsgMover.properties");
		SimpleProps props = new SimpleProps();
		props.load(configfile);
		QueueManager qm = new QueueManager(props);
		qm.runCommand(args[0]);
	}

}
