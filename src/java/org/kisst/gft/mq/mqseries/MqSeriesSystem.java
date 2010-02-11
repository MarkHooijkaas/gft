package org.kisst.gft.mq.mqseries;

import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.kisst.cfg4j.Props;
import org.kisst.gft.mq.MessageHandler;
import org.kisst.gft.mq.MqQueue;
import org.kisst.gft.mq.MqSystem;

import com.ibm.mq.MQC;
import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;

public class MqSeriesSystem implements MqSystem {
	public class Queue implements MqQueue {
		private final MQQueue queue;
		public Queue(MQQueue queue) {this.queue=queue; } 
		public String getName() { return queue.name; }
		public void listen(MessageHandler handler) {
		}

		public void send(String data) {
		}

		public void stopListening() {
		}
		
	}
	
	private static final Logger log = Logger.getLogger(MqSeriesSystem.class);
	private final Props props;
	private MQQueueManager qmgr;
	
	public MqSeriesSystem(Props props) {
		this.props=props;
		this.qmgr=openConnection(this.props.getString("queuemanager"), 5000, 3);
	}

	public MqQueue getQueue(String name) {
		return new Queue(openQueue(qmgr, "", 0));
	}

/////////////////////////////////////////////////////////	

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#openConnection(java.lang.String, int, int)
	 */
	public MQQueueManager openConnection(String qm, int timeOut, int aantalPogingen) {

		MQQueueManager manager = null;
		// invoer controleren
		if (qm == null || qm.equals("")) {
			throw new RuntimeException("queue manager is niet gevuld, connectie kan niet geopend worden");
		}
		// aantalPogingen keer proberen connectie te maken
		int i = 1;
		boolean doorgaan = true;
		while (doorgaan) {
			try {

				manager = new MQQueueManager(qm);
				log.info("(MqUtilsDaoImpl:openConnection)Verbonden met " + qm);
				doorgaan = false;
			} catch (MQException e) {
				log.warn("(MqUtilsDaoImpl:openConnection)Queue manager niet beschikbaar: poging " + i);
				if (i >= aantalPogingen) {
					throw new RuntimeException(e);
				}
				try {
					Thread.sleep(timeOut);
				} catch (InterruptedException e1) {
					log.warn("(MqUtilsDaoImpl:openConnection)Fout tijdens de timeout " + e1.getMessage());
				}
				i++;
			}
		}
		return manager;
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#closeConnection(com.ibm.mq.MQQueueManager)
	 */
	public void closeConnection(MQQueueManager manager) {

		if (manager == null) {
			throw new RuntimeException("Queue manager niet gevuld, kan verbinding niet sluiten");
		}

		try {
			manager.disconnect();
			log.info("(MqUtilsDaoImpl:closeConnection)Verbinding verbroken met queue manager" + manager.name);
		} catch (MQException e) {
			log.error("(MqUtilsDaoImpl:closeConnection)Fout bij het sluiten van de connectie met de queue manager; " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#openQueue(com.ibm.mq.MQQueueManager, java.lang.String, int, java.lang.String)
	 */
	public MQQueue openQueue(MQQueueManager manager, String qname, int openOptions, String aliasQueueManagerName) {
		MQQueue queue = null;
		try {
			queue = manager.accessQueue(qname, openOptions, aliasQueueManagerName, "", "");
			log.info("(MqUtilsDaoImpl:openQueue)queue " + qname + " is geopend");
		} catch (MQException e) {
			switch (e.reasonCode) {
			case MQException.MQRC_UNKNOWN_OBJECT_NAME:
				log.error("(MqUtilsDaoImpl:openQueue)Onbekende Queue: " + qname);
				throw new RuntimeException(e);
			default:
				log.error("(MqUtilsDaoImpl:openQueue)Fout bij openQueue: " + e.getMessage());
			throw new RuntimeException(e);
			}
		}
		return queue;
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#openQueue(com.ibm.mq.MQQueueManager, java.lang.String, int)
	 */
	public MQQueue openQueue(MQQueueManager manager, String qname,int openOptions) {
		MQQueue queue = null;
		try {
			queue = manager.accessQueue(qname, openOptions);
			log.info("(MqUtilsDaoImpl:openQueue)queue " + qname + " is geopend");
		} catch (MQException e) {
			switch (e.reasonCode) {
			case MQException.MQRC_UNKNOWN_OBJECT_NAME:
				log.error("(MqUtilsDaoImpl:openQueue)Onbekende Queue");
				throw new RuntimeException(e);
			default:
				log.error("(MqUtilsDaoImpl:openQueue)Fout bij openQueue: " + e.getMessage());
			throw new RuntimeException(e);
			}
		}
		return queue;
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#closeQueue(com.ibm.mq.MQQueue)
	 */
	public void closeQueue(MQQueue queue) throws MQException {
		try {
			queue.close();
			log.info("(MqUtilsDaoImpl:closeQueue)queue " + queue.name + " is gesloten");
		} catch (MQException e) {
			log.error("(MqUtilsDaoImpl:closeQueue)Fout tijdens het sluiten van queue " + queue.name + ": + " + e.reasonCode);
			throw (e);
		}
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#putMessage(com.ibm.mq.MQQueueManager, com.ibm.mq.MQQueue, com.ibm.mq.MQQueue, java.lang.String, com.ibm.mq.MQPutMessageOptions, int, java.lang.String)
	 */
	public byte[] putMessage(MQQueueManager manager, MQQueue queue, MQQueue replyTo, String mes, MQPutMessageOptions mqpmo, int charset, String correlationId)
	throws MQException, IOException {
		try {
			log.debug("Correlation id = "+ correlationId);
			// creeer nieuwe bericht instantie
			MQMessage mqMessage = new MQMessage();
			mqMessage.characterSet = charset;
			mqMessage.persistence = MQC.MQPER_PERSISTENT;
			mqMessage.format = MQC.MQFMT_STRING;
			mqMessage.writeString(mes);
			// mqMessage.expiry = 3000; // bericht vervalt na 5 minuten

			if (correlationId != null) {
				byte[] mesId = correlationId.getBytes();
				mqMessage.correlationId = mesId;
				// dit moet er weer uit, oplossen in plex
				// positie 24 maken gelijk aan 0 omdat plex dit verwacht.
				// uitleg:
				// messageid:  12345678901234567890ABCDE (25 pos)
				// corr id:    12345678901234567890ABCD  (24 pos)
				// corr. id in plex:
				//             12345678901234567890ABC   (23 pos)
				// zero terminated dus:
				//             12345678901234567890ABC0  (24 pos)
				// voorlopig alleen als messageId langer dan 23 is				
				if (correlationId.length() >= 24){
					mqMessage.correlationId[23] = 0;
				}
				log.debug("Correlation ID gelijk gemaakt aan messageID vraagbericht: " + correlationId);
			}

			if(manager != null){
				mqMessage.replyToQueueManagerName = manager.name;
				// replyTo kan leeg zijn, in dat geval is Reply2q = queue
				if (replyTo != null){
					mqMessage.replyToQueueName = replyTo.name;
				} else{
					mqMessage.replyToQueueName = queue.name;
				}
			}

			queue.put(mqMessage, mqpmo);
			return mqMessage.messageId;
		} catch (MQException e1) {
			log.error("(MqUtilsDaoImpl:putMessage)MQ exception bij putMessage, reason code: "
					+ e1.reasonCode);
			throw (e1);
		} catch (IOException e) {
			log.error("(MqUtilsDaoImpl:putMessage)Fout bij het schrijven van een bericht op de queue "
					+ queue.name);
			throw (e);
		}
	}



	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#getMessage(com.ibm.mq.MQQueue, com.ibm.mq.MQGetMessageOptions, int, byte[])
	 */
	public String getMessage(MQQueue queue, MQGetMessageOptions messageOptions, int charSet, byte[] correlationId) 
	throws MQException {

		String result = null;
		try {
			MQMessage mqMessage = new MQMessage();
			mqMessage.characterSet = charSet;
			mqMessage.format = MQC.MQFMT_STRING;		

			if(correlationId != null && correlationId.length > 0){
				mqMessage.correlationId = correlationId;
				messageOptions.matchOptions = MQC.MQMO_MATCH_CORREL_ID;
			}

			queue.get(mqMessage, messageOptions);

			// We willen niet readUTF doen, dat werkt niet goed als het vanaf een AS400 komt
			// result = mqMessage.readUTF();

			result = mqMessage.readStringOfCharLength(mqMessage.getDataLength());

			// nu gaan we kijken of replyToQ en replyToQMgr gevuld zijn
			// zo ja, voegen we ze toe aan de plexString plus messageId
			if (! (mqMessage.replyToQueueManagerName == null && mqMessage.replyToQueueName == null)){
				String mesIdString = new String(mqMessage.messageId);
				log.debug("MessageId: " + mesIdString);
				result = result + "&" + mqMessage.replyToQueueManagerName + "&" + mqMessage.replyToQueueName + "&" + mesIdString;
			}

		} catch (MQException e) {
			if (e.reasonCode != MQException.MQRC_NO_MSG_AVAILABLE){
				log.error("(MqUtilsDaoImpl:getMessage)MQ exception bij getMessage, reason code: " + e.reasonCode);
			}
			// niets loggen bij 2033 (geen msg available)
			throw(e);
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#commitAll(com.ibm.mq.MQQueueManager)
	 */
	public void commitAll(MQQueueManager manager) throws MQException {
		try {
			manager.commit();
		} catch (MQException e) {
			log.error("(MqUtilsDaoImpl:commitAll)MQException bij het committen, reason code: "
					+ e.reasonCode);
			throw (e);
		}
	}


	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#backout(com.ibm.mq.MQQueueManager)
	 */
	public void backout(MQQueueManager manager) throws MQException {
		try{
			manager.backout();
		} catch (MQException e) {
			log.error("(MqUtilsDaoImpl:backout)Rollback uitgevoerd");
		}
	}

	/* (non-Javadoc)
	 * @see nl.ibgroep.pokanaal.dao.MqUtilsDao#commit(com.ibm.mq.MQQueueManager)
	 */
	public void commit(MQQueueManager manager) throws MQException {
		try{
			manager.commit();
		} catch (MQException e) {
			log.info("(MqUtilsDaoImpl:commit)Commit uitgevoerd");
		}
	}
}

