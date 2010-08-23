package org.kisst.jms;

import org.kisst.cfg4j.Props;
import org.kisst.gft.admin.rest.Representable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiListener implements Representable {
	private final static Logger logger=LoggerFactory.getLogger(MultiListener.class); 

	private final String name;
	private final Props props;
	private final JmsListener[] threads;

	public MultiListener(JmsSystem system, MessageHandler handler, Props props, Object context) {
		this.name=props.getLocalName();
		this.props=props;
		int nrofThreads = props.getInt("nrofThreads",2);
		this.threads =new JmsListener[nrofThreads];
		for (int i=0; i<nrofThreads; i++)
			threads[i]=new JmsListener(system, handler, props, context);
	}
	
	public boolean listening() { return threads!=null; }
	public String getQueue() { return threads[0].queue; }
	public String getErrorQueue() { return threads[0].errorqueue; }
	public String getRetryQueue() { return threads[0].retryqueue; }
	public String getRepresentation() { return props.toString(); }

	public void stop() {
		logger.info("Stopping MultiListener {}", name);
		for (JmsListener t:threads)
			t.stop();
	}
	public void start()  {
		logger.info("Starting MultiListener {}", name);
		for (JmsListener t:threads)
			t.start();
	}
}
