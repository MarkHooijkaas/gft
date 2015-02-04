package org.kisst.gft.poller;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.LocalFileServer;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Poller implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Poller.class);

	public final GftContainer gft;
	private final Props props;

	private final int interval;
	private final String name;
	private final PollerJob[] jobs;
	
	private boolean running=true;
	private boolean paused=false;
	private Thread thread=null;
	private boolean sleeping=false;
	//public final SshHost server;
	
	private final FileServer fileserver;
	
	public Poller(GftContainer gft, String name, Props props) {
		this.gft=gft;
		this.props=props;
		this.name=name;
		this.interval = props.getInt("interval", 1000);
		if (gft!=null) {
			String hostname=props.getString("host",null);
			if (hostname!=null) {
				logger.info("using remote host {}",hostname);
				fileserver= gft.getFileServer(hostname);
			}
			else {
				logger.info("using local host");
				fileserver=new LocalFileServer(props);
			}
		}
		else
			fileserver=new LocalFileServer(props);
		
		Props pollerProps=props.getProps("job");
		int count=0;
		for (@SuppressWarnings("unused") String jobname: pollerProps.keys())
			count++;
		jobs=new PollerJob[count];
		int i=0;
		for (String jobname: pollerProps.keys())
			jobs[i++]=new PollerJob(this, pollerProps.getProps(jobname));		
	}
	public Poller(String name, Props props) {
		this(null, name, props);
	}

	public String getName() { return name; }
	public boolean isPaused() { return paused; }
	public boolean isRunning() { return thread!=null; }
	public int getInterval() { return interval; }
	public Props getProps() { return props; }
	public FileServer getFileServer() { return this.fileserver; }

	
	public PollerJob[] getJobs() { return jobs; }
	
	public synchronized void start() {
		if (thread!=null)
			throw new RuntimeException("Poller "+name+"already started");
		thread=new Thread(this);
		thread.start();
	}
	public synchronized void stop() {
		if (thread==null)
			throw new RuntimeException("Poller "+name+" not running");
		logger.info("Stopping poller {}",name);
		running=false;
		if (sleeping) // TODO: not really thread safe
			thread.interrupt();
		thread=null;
	}
	public void pause() {
		logger.info("Pause poller {}",name);
		paused=true;
	}
	
	// TODO: This method could use some more precise exception handling
	public void run() {
		logger.info("Starting poller {}",name);
		while (running) {
			try {
				FileServerConnection fsconn=null;
				if (! paused){
					if (fileserver!=null)
						fsconn=fileserver.openConnection();
					for (PollerJob job: jobs) {
						try {
							job.runOnce(fsconn);
						}
						catch (Exception e) {
							logger.error("error when running PollerJob "+job.getName()+" in poller "+getName(),e );
						}
					}
				}
				if (fileserver!=null)
					fsconn.close();
				sleeping=true;
				Thread.sleep(interval);
				sleeping=false;
			} 
			catch (InterruptedException e) { /*IGNORE*/ }
			catch (Exception e) {
				logger.error("error in poller "+getName(), e);
			}
		}
		logger.info("Stopped poller {}",name);
		
	}

	public void resume() {
		logger.info("Resume poller {}",name);
		paused=false;
	}

	public void join() throws InterruptedException {
		if (thread==null)
			return;
		thread.join();
	}		
}
