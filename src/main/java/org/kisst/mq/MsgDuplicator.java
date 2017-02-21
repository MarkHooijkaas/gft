package org.kisst.mq;

import com.ibm.mq.MQEnvironment;
//import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

public class MsgDuplicator
{
	public static void main(String[] args) {
		if (args.length<3 || args.length>5)
			throw new RuntimeException("Correct syntax java -jar MsgDuplicator.jar srcqueue dstqueue duplqueue");
		String srcqueue=args[0];
		String destqueue=args[1];
		String duplqueue=args[2];
		/*
		File configfile=new File("MsgDuplicator.properties");
		SimpleProps props = new SimpleProps();
		props.load(configfile);
		*/
		try {
		/*
			if (args.length>3)
				props=(SimpleProps) props.getProps("qm."+args[3]);
			if (args.length>4)
				MsgDuplicator.moveMessage(props, srcqueue, destqueue, duplqueue, args[4]);
			else
			*/
				MsgDuplicator.moveAllMessages(srcqueue, destqueue, duplqueue);
		} 
		catch (MQException e) { e.printStackTrace(); }	
	}


	public static void moveAllMessages(String src, String dest, String dupl) throws MQException {
		moveMessage(src, dest, dupl, "@all");
	}
	/*
	public static void moveMessage(String src, String dest, String dupl, String msgid) throws MQException {
		moveMessage(src, dest, dupl, msgid);
	}
*/	
	public static void moveMessage(String src, String dest, String dupl, String msgid) throws MQException {
		if (msgid==null)
			throw new RuntimeException("msgid==null is not allowed, use @all instead");
		boolean moveAll=(msgid.equals("@all"));
		
		MQEnvironment.hostname = "172.30.247.38";
        MQEnvironment.channel = "WINCLIENT";
		MQQueueManager qm = new MQQueueManager("WINONT");
		
		System.out.println("contact met queuemanager gelegd");
		System.out.println("nu openen leesqueue "+src);
		
		MQQueue srcq = qm.accessQueue(src, 
				MQConstants.MQOO_INPUT_SHARED |
				MQConstants.MQOO_INQUIRE | 
				MQConstants.MQOO_SAVE_ALL_CONTEXT |
				MQConstants.MQOO_FAIL_IF_QUIESCING
				);
		System.out.println("nu openen schrijfqueue "+dest);
		MQQueue destq = qm.accessQueue(dest, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_OUTPUT| MQConstants.MQOO_SET_ALL_CONTEXT);
		System.out.println("nu openen duplicatiequeue "+dupl);
		MQQueue duplq = qm.accessQueue(dupl, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_OUTPUT| MQConstants.MQOO_SET_ALL_CONTEXT);
		try {
			MQGetMessageOptions gmo = new MQGetMessageOptions();
			gmo.options=
					MQConstants.MQGMO_WAIT |  // wait for messages to appear on the queue
					MQConstants.MQGMO_SYNCPOINT | // best practice MQConstants.MQGMO_SYNCPOINT_IF_PERSISTENT |
					MQConstants.MQGMO_FAIL_IF_QUIESCING // for nice shutdown. Not really necessary, but best practice in general
					; 
			gmo.waitInterval = MQConstants.MQWI_UNLIMITED; // waiting indefinetely for a new message
			if (! moveAll)
				gmo.matchOptions = MQConstants.MQMO_MATCH_MSG_ID;

			MQPutMessageOptions pmo = new MQPutMessageOptions();
			pmo.options=MQConstants.MQPMO_SET_ALL_CONTEXT;

			int i=0;
			while (true) {     
				MQMessage msg = new MQMessage();
				try {
					srcq.get(msg, gmo);
				}
				catch(MQException e) {
					if (e.reasonCode==2033) { // MQRC_NO_MSG_AVAILABLE
						System.out.println("completion for reason 2033: no more messages");
						break;
					}
					throw e;
				}
				i++;
				System.out.print("Moving message "+i+" ... ");
				destq.put(msg, pmo); 
				msg.expiry=MQConstants.MQEI_UNLIMITED;
				duplq.put(msg, pmo);
				qm.commit();
				System.out.println("OK");
			}
			System.out.println("Moved "+i+" messages");
		}
		finally {
			srcq.close();
			destq.close();
			duplq.close();
			qm.close();
			qm.disconnect();
		}


	}

}
