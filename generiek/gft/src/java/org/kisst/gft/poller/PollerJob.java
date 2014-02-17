package org.kisst.gft.poller;

import java.io.PrintWriter;
import java.util.HashMap;

import org.kisst.gft.filetransfer.FileCouldNotBeMovedException;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FoundDirectoryTask;
import org.kisst.gft.filetransfer.FoundFileTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollerJob extends BasicTaskDefinition {
	private static final Logger logger = LoggerFactory.getLogger(PollerJob.class);

	private final Poller parent;
	private final String dir;
	private final String moveToDir;
	private final int maxNrofMoveTries;
	private final boolean pollForEntireDirectories;
	private final HashMap<String, Snapshot> known = new HashMap<String, Snapshot>();
	private final HashMap<String, Integer> retries = new HashMap<String, Integer>();
	private final FileServer fileserver;
	private int delay;
	private boolean paused = false;

	private int runs = 0;
	private int successes = 0;
	private int errors = 0;

	public String currentFile;
	
	private PollerJobListener listener = new DummyListener();

	public PollerJob(Poller parent,Props props, FileServer fileserver) {
		super(parent.gft, props, "send_gft_message");
		this.parent=parent;
		this.fileserver = fileserver;
		delay = props.getInt("delay", 10000);
		dir = TemplateUtil.processTemplate(props.getString("pollerDirectory"),gft.getContext());
		moveToDir = TemplateUtil.processTemplate(props.getString("moveToDirectory"),gft.getContext());
		pollForEntireDirectories = props.getBoolean("pollForEntireDirectories",	false);
		paused = props.getBoolean("paused", false);
		maxNrofMoveTries=props.getInt("maxNrofMoveTries", 3);
	}

	public PollerJob(Poller parent,Props props) {
		this(parent, props, null);
	}

	public FileServer getFileServer() { 
		if (fileserver==null)
			return parent.getFileServer();
		else
			return this.fileserver;
	}

	public void runOnce(FileServerConnection parentfsconn) {
		if (paused)
			return;

		listener.updateGuiStatus(name, true);
		listener.updateGuiRuns(name, runs++);

		FileServerConnection fsconn = parentfsconn;
		if (fileserver != null)
			fsconn = fileserver.openConnection();
		try {
			logger.info("pollForEntireDirectories = {}", pollForEntireDirectories);
			pollOnce(fsconn);
			listener.updateGuiStatus(name, false);
		} finally {
			if (fileserver != null && fsconn != null)
				fsconn.close();
		}

	}

	public void setPaused(boolean paused) {
		this.paused = paused;
		logger.info("Folder {} pause = {}", dir, paused);
	}
	public String getName() { return name; }
	public String getDir() { return dir; }
	public String getMoveToDir() { return moveToDir; }
	public int getRuns() { return runs; }
	public int getSuccesses() { return successes; }
	public int getErrors() { return errors; }
	public boolean isPaused() { return paused; }
	public int getNumberOfDetectedFiles() { return known.size(); }
	public int getNumberOfProblematicFiles() { return retries.size() - known.size(); }

	public void setListener(PollerJobListener listener) { this.listener = listener; }

	private void pollOnce(FileServerConnection fsconn) {
		String logname="file";
		if (pollForEntireDirectories)
			logname="directory";
			
		// Verwerking voor een bestand in een directory
		logger.info("getting {}-list in directory {}", logname, dir);
		 
		for (String f : fsconn.getDirectoryEntries(dir).keySet()) {
			if (".".equals(f) || "..".equals(f))
				continue;
			
			int trycount = 0;
			Integer tmp = retries.get(f); 
			if (tmp!=null)// test for null, because of unboxing
				trycount=tmp;
			if (trycount >= maxNrofMoveTries )
				continue; // this file has been tried to move too many times (probably a file is in the way), it should not clog the logfile
			if (fsconn.isDirectory(dir + "/" + f) && ! pollForEntireDirectories){
				logger.info("directory {} gevonden, deze wordt overgeslagen bij alleen file verwerking.", dir + "/" + f);
				continue;
			}
				
			logger.info(name+ " - {} {} gevonden, controleren tot er geen wijzigingen meer zijn.", logname, f);
			Snapshot snapshot; 
			if (pollForEntireDirectories)
				snapshot= new DirectorySnapshot(fsconn, dir + "/"+ f);
			else
				snapshot= new FileSnapshot(fsconn, dir + "/"+ f);
			Snapshot otherSnapshot = known.get(f);
			if (otherSnapshot == null) {
				known.put(f, snapshot);
				retries.put(f, 1);
			} else {
				if (snapshot.equals(otherSnapshot)) {
					long timestamp = new java.util.Date().getTime();
					if (otherSnapshot.getTimestamp() + delay < timestamp) {
						logger.debug(name+" - {} {} is klaar om verplaatst te worden.",logname, dir + "/" + f);
						boolean moved = false;
						try {
							fsconn.move(dir + "/" + f,	moveToDir + "/" + f);
							moved=true;
						}
						catch (FileCouldNotBeMovedException e) { 
							logger.warn("Could not move "+logname+" "+f+" to " +moveToDir, e);
						}
						if (moved) {
							logger.info(name + " - "+logname+" " + f + " is verplaatst naar " + moveToDir);
							Task task;
							if (pollForEntireDirectories)
								task=new FoundDirectoryTask(gft, this, fsconn, dir + "/" + f);
							else
								task=new FoundFileTask(gft,this, fsconn, f);
							run(task);
							known.remove(f);
							retries.remove(f);
							listener.updateGuiSuccess(name, successes++);
						} else {
							listener.updateGuiErrors(name, errors++);
							logger.debug("retrynummer {} van {}", trycount, f);
							if (trycount < maxNrofMoveTries) {
								logger.warn(name + " - verplaatsen van file " + f + " naar " + moveToDir + " is niet gelukt. Dit wordt later nog een keer geprobeerd.");
							} else {
								logger.error(name + " - verplaatsen van file " + f + " naar " + moveToDir + " is niet gelukt niet na " + trycount + " keer proberen.");
								known.remove(f); // Zodat het weer vanaf begin opnieuw gaat, maar er is wel en Error gegeven.
							}
							retries.put(f, trycount + 1);
						}
					}
				} else {
					known.put(f, snapshot);
					retries.put(f, 1);
				}
			}
		}
	}

	@Override
	public void writeHtml(PrintWriter out) {
		out.println("<H1>PollerJob</H1>");
		out.println("No specific data");
	}

}