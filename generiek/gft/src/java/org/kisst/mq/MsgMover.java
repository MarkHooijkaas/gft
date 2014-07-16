package org.kisst.mq;

import java.io.File;

import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;

//import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.constants.MQConstants;

public class MsgMover
{
	public static void main(String[] args) {
		if (args.length!=2)
			throw new RuntimeException("Correct syntax java -jar MsgMover.jar srcqueue dstqueue");
		String srcqueue=args[0];
		String destqueue=args[1];
		File configfile=new File("MsgMover.properties");
		SimpleProps props = new SimpleProps();
		props.load(configfile);
		
		try {
			MsgMover.moveAllMessages(props, srcqueue, destqueue);
		} 
		catch (MQException e) { e.printStackTrace(); }
	}


	public static void moveAllMessages(Props props, String src, String dest) throws MQException {
		QueueManager qm=new QueueManager(props);
		MQQueue srcq = qm.getQueue(src, 
				MQConstants.MQOO_INPUT_SHARED |
				MQConstants.MQOO_INQUIRE | 
				MQConstants.MQOO_SAVE_ALL_CONTEXT |
				MQConstants.MQOO_FAIL_IF_QUIESCING
				);
		MQQueue destq = qm.getQueue(dest, MQConstants.MQOO_OUTPUT| MQConstants.MQOO_SET_ALL_CONTEXT);
		try {
			MQGetMessageOptions gmo = new MQGetMessageOptions();
			gmo.options=
					MQConstants.MQGMO_NO_WAIT | 
					MQConstants.MQGMO_SYNCPOINT | // best practice MQConstants.MQGMO_SYNCPOINT_IF_PERSISTENT |
					MQConstants.MQGMO_FAIL_IF_QUIESCING // for nice shutdown. Not really necessary, but best practice in general
					; 
			gmo.matchOptions=MQConstants.MQGMO_NONE;

			MQPutMessageOptions pmo = new MQPutMessageOptions();
			pmo.options=MQConstants.MQPMO_SET_ALL_CONTEXT;

			int i=0;
			while (true) { //srcq.getCurrentDepth()>0) {    
				MQMessage msg = new MQMessage();
				try {
					srcq.get(msg, gmo);
				}
				catch(MQException e) {
					if (e.reasonCode==2033) { // MQRC_NO_MSG_AVAILABLE
						System.out.println("no more messages");
						break;
					}
					throw e;
				}
				System.out.print("Moving message "+i+" ... ");
				destq.put(msg, pmo); 
				qm.commit();
				System.out.println("OK");
				i++;
			}
		}
		finally {
			srcq.close();
			destq.close();
			qm.close();
		}
	}

}
