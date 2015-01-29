package org.kisst.gft.poller;

import java.io.PrintWriter;
import java.util.HashMap;

import nl.duo.gft.filetransfer.SendGftMessageAction;

import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.gft.filetransfer.FileCouldNotBeMovedException;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FoundFileTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollerJob extends BasicTaskDefinition implements WritesHtml {
	private static final Logger logger = LoggerFactory.getLogger(PollerJob.class);
	
	public static interface Transaction {
		public void prepareTransaction(FoundFileTask task);
		public void commitTransaction(FoundFileTask task);
		public void rollbackTransaction(FoundFileTask task);
	}

	

	private final Action flow;
	private final Poller parent;
	private final String dir;
	private final String moveToDir;
	private final int maxNrofMoveTries;
	private final boolean deleteInProgressFile;
	private final boolean pollForEntireDirectories;
	// TODO: the map of known files should be cleared once a file disappears outside of the poller,
	// e.g. when it has been handled by a poller on another machine.

	private final HashMap<String, Snapshot> known = new HashMap<String, Snapshot>();
	private final HashMap<String, Integer> retries = new HashMap<String, Integer>();
	private final FileServer fileserver;
	private final String logname;

	private int delay;
	private boolean paused = false;

	private int runs = 0;
	private int successes = 0;
	private int errors = 0;
	private int nrofDetectedFiles=0;
	private int nrofIgnoredFiles=0;
	
	public String currentFile;
	
	private PollerJobListener listener = new DummyListener();


	public PollerJob(Poller parent,Props props, FileServer fileserver) {
		super(parent.gft, props);
		if (props.getString("actions",null)==null)
			this.flow= new SendGftMessageAction(gft, props); 
		else 
			this.flow=new ActionList(this, props);
		this.parent=parent;
		this.fileserver = fileserver;
		delay = props.getInt("delay", 10000);
		dir = TemplateUtil.processTemplate(props.getString("pollerDirectory"),gft.getContext());
		moveToDir = TemplateUtil.processTemplate(props.getString("moveToDirectory"),gft.getContext());
		deleteInProgressFile = props.getBoolean("deleteInProgressFile",	true);
		pollForEntireDirectories = props.getBoolean("pollForEntireDirectories",	false);
		paused = props.getBoolean("paused", false);
		maxNrofMoveTries=props.getInt("maxNrofMoveTries", 3);
		if (pollForEntireDirectories)
			logname="directory";
		else
			logname="file";
			
	}

	@Override public Action getFlow() { return this.flow;}
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
	public String getFullName() { return parent.getName()+"."+name; }
	public String getName() { return name; }
	public String getDir() { return dir; }
	public String getMoveToDir() { return moveToDir; }
	public int getRuns() { return runs; }
	public int getSuccesses() { return successes; }
	public int getErrors() { return errors; }
	public boolean isPaused() { return paused; }
	public int getNumberOfDetectedFiles() { return nrofDetectedFiles; }
	public int getNumberOfProblematicFiles() { return nrofIgnoredFiles; }

	public void setListener(PollerJobListener listener) { this.listener = listener; }

	private void pollOnce(FileServerConnection fsconn) {
		// Verwerking voor een bestand in een directory
		logger.info("getting {}-list in directory {}", logname, dir);
		int tmpnrofIgnoredFiles=0;
		int tmpnrofDetectedFiles=0;
		for (String filename : fsconn.getDirectoryEntries(dir).keySet()) {
			if (".".equals(filename) || "..".equals(filename))
				continue;
			tmpnrofDetectedFiles++;

			if (shouldFileBeIgnored(fsconn,filename)) {
				tmpnrofIgnoredFiles++;
				continue;
			}
			
			logger.info(name+ " - {} {} gevonden, controleren tot er geen wijzigingen meer zijn.", logname, filename);
			if (isFileNotChangingAnymore(fsconn, filename))
				processFile(fsconn, filename);
		}
		this.nrofDetectedFiles=tmpnrofDetectedFiles;
		this.nrofIgnoredFiles=tmpnrofIgnoredFiles;
	}

	private void processFile(FileServerConnection fsconn, String filename) {
		logger.debug(name+" - {} {} is klaar om verplaatst te worden.",logname, dir + "/" + filename);
		FoundFileTask task=null;
		if (pollForEntireDirectories)
			task=new FoundFileTask(gft, this, fsconn, dir + "/" + filename);
		else
			task=new FoundFileTask(gft,this, fsconn, filename);
		
		task.startTransaction();
		boolean completed=false;
		try {
			tryToMove(fsconn, filename);
			logger.info(name + " - "+logname+" " + filename + " is verplaatst naar " + moveToDir);
			run(task);
			completed = true;
			known.remove(filename);
			retries.remove(filename);
			listener.updateGuiSuccess(name, successes++);
		}
		finally {
			if (completed) {
				task.commit();
				if (deleteInProgressFile)
					fsconn.deleteFile(task.filename);
			}
			else
				task.rollback();
		}
	}
	
	private boolean shouldFileBeIgnored(FileServerConnection fsconn, String filename) {
		if (getTryCount(filename) >= maxNrofMoveTries ) {
			return true; // this file has been tried to move too many times (probably a file is in the way), it should not clog the logfile
		}
		if (fsconn.isDirectory(dir + "/" + filename) && ! pollForEntireDirectories){
			logger.info("directory {} gevonden, deze wordt overgeslagen bij alleen file verwerking.", dir + "/" + filename);
			return true;
		}
		return false;
	}

	private boolean isFileNotChangingAnymore(FileServerConnection fsconn, String filename) {
		Snapshot snapshot; 
		if (pollForEntireDirectories)
			snapshot= new DirectorySnapshot(fsconn, dir + "/"+ filename);
		else
			snapshot= new FileSnapshot(fsconn, dir + "/"+ filename);
		Snapshot otherSnapshot = known.get(filename);
		if (otherSnapshot == null) {
			known.put(filename, snapshot);
			retries.put(filename, 1);
			return false;
		} 
		if (snapshot.equals(otherSnapshot)) {
			long timestamp = new java.util.Date().getTime();
			if (otherSnapshot.getTimestamp() + delay < timestamp)
				return true;
		} else {
			known.put(filename, snapshot);
			retries.put(filename, 1);
		}
		return false;
	}
	
	
	private void tryToMove(FileServerConnection fsconn, String filename) throws FileCouldNotBeMovedException {
		try {
			fsconn.move(dir + "/" + filename,	moveToDir + "/" + filename);
		}
		catch (RuntimeException e) { 
			logger.warn("Could not move "+logname+" "+filename+" to " +moveToDir, e);
			rememberFailedMove(filename);
			if (e instanceof FileCouldNotBeMovedException)
				throw e;
			throw new FileCouldNotBeMovedException(filename, e);
		}
	}

	private int getTryCount(String filename) {
		Integer tmp = retries.get(filename); 
		if (tmp==null)// test for null, because of unboxing
			return 0;
		return tmp;
	}
	
	private void rememberFailedMove(String filename) {
		int trycount=getTryCount(filename);
		listener.updateGuiErrors(name, errors++);
		logger.debug("retrynummer {} van {}", trycount, filename);
		if (trycount < maxNrofMoveTries) {
			logger.warn(name + " - verplaatsen van file " + filename + " naar " + moveToDir + " is niet gelukt. Dit wordt later nog een keer geprobeerd.");
		} else {
			logger.error(name + " - verplaatsen van file " + filename + " naar " + moveToDir + " is niet gelukt niet na " + trycount + " keer proberen.");
			known.remove(filename); // Zodat het weer vanaf begin opnieuw gaat, maar er is wel en Error gegeven.
		}
		retries.put(filename, trycount + 1);
	}

	
	@Override public void writeHtml(PrintWriter out) {
		out.println("<H1>PollerJob</H1>");
		out.println("No specific data");
	}

	@Override public String getSrcDescription() { return dir; }
	@Override public String getDestDescription() { return moveToDir;}
	public String getKanaalNaam() {
		if (flow instanceof SendGftMessageAction)
			return ((SendGftMessageAction)flow).getKanaalNaam();
		else
			return "";
	}
}