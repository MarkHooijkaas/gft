package org.kisst.gft.poller;

import java.io.PrintWriter;
import java.util.HashMap;

import org.kisst.gft.LogService;
//import org.kisst.gft.action.Action;
import org.kisst.gft.action.ActionList;
import org.kisst.gft.admin.WritesHtml;
import org.kisst.gft.filetransfer.FileCouldNotBeMovedException;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.PollerTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollerJob extends BasicTaskDefinition implements WritesHtml {
	private static final Logger logger = LoggerFactory.getLogger(PollerJob.class);
	private static Class<?> defaultAction=null; // TODO: better defaultAction
	public static void setDefaultAcion(Class<?> defaultAction) { PollerJob.defaultAction=defaultAction; }
	
	public interface Action extends org.kisst.gft.action.Action {
		public boolean deleteInProgressFile();
	}
	
	private final Action flow;
	private final Poller parent;
	private final String dir;
	private final String moveToDir;
	private final String extension;
	private final int maxNrofMoveTries;
	private final boolean deleteInProgressFile;
	private final boolean pollForEntireDirectories;
	private final boolean checkIfFileIsLocked;
	private final long minimumSize;
	// TODO: the map of known files should be cleared once a file disappears outside of the poller,
	// e.g. when it has been handled by a poller on another machine.

	private final HashMap<String, Snapshot> known = new HashMap<String, Snapshot>();
	private final HashMap<String, Integer> retries = new HashMap<String, Integer>();
	private final FileServer fileserver;
	private final String logname;
	private final int maxNrofConsecutivePollProblems;

	private int delay;
	private boolean paused = false;

	private int runs = 0;
	private int successes = 0;
	private int errors = 0;
	private int nrofDetectedFiles=0;
	private int nrofIgnoredFiles=0;
	private int nrofInProgressFiles=0;
	private int nrofConsecutivePollProblems=0;
	
	public String currentFile;
	
	private PollerJobListener listener = new DummyListener();

	public PollerJob(Poller parent,Props props) {
		super(parent.gft, props);
		this.flow=(Action) ActionList.createAction(gft, this, defaultAction);
		this.parent=parent;
		this.fileserver = null; // TODO: remove
		delay = props.getInt("delay", 10000);
		dir = TemplateUtil.processTemplate(props.getString("pollerDirectory"),gft.getContext());
		String ext = props.getString("extension",null);
		if (ext!=null && ext.startsWith("*"))
			ext=ext.substring(1);
		this.extension=ext;
		moveToDir = TemplateUtil.processTemplate(props.getString("moveToDirectory"),gft.getContext());
		deleteInProgressFile = props.getBoolean("deleteInProgressFile",	flow.deleteInProgressFile());
		pollForEntireDirectories = props.getBoolean("pollForEntireDirectories",	false);
		checkIfFileIsLocked = props.getBoolean("checkIfFileIsLocked",	false);
		minimumSize= props.getLong("minimumSize", 0);
		paused = props.getBoolean("paused", false);
		this.maxNrofConsecutivePollProblems=props.getInt("maxNrofMoveTries", 10);
		this.maxNrofMoveTries=props.getInt("maxNrofMoveTries", 3);
		if (pollForEntireDirectories)
			logname="directory";
		else
			logname="file";
			
	}

	@Override public Action getFlow() { return this.flow;}

	public FileServer getFileServer() { 
		if (fileserver==null)
			return parent.getFileServer();
		else
			return this.fileserver;
	}

	public void runOnce(FileServerConnection parentfsconn) {
		if (isPaused())
			return;

		listener.updateGuiStatus(name, true);
		listener.updateGuiRuns(name, runs++);

		FileServerConnection fsconn = parentfsconn;
		boolean success=false;
		if (fileserver != null)
			fsconn = fileserver.openConnection();
		try {
			pollOnce(fsconn);
			listener.updateGuiStatus(name, false);
			success=true;
		} finally {
			if (success)
				nrofConsecutivePollProblems=0;
			else {
				nrofConsecutivePollProblems++;
				if (nrofConsecutivePollProblems>maxNrofConsecutivePollProblems)
					LogService.log("error", "PausingPoller", getFullName(), dir, "Pausing pollerJob after "+nrofConsecutivePollProblems+" problems");

			}
			if (fileserver != null && fsconn != null)
				fsconn.close();
		}

	}

	public void setPaused(boolean paused) {
		this.paused = paused;
		logger.info("Folder {} pause = {}", dir, paused);
	}
	public String getFullName() { return parent.getName()+"."+name; }
	public String getShortName() { return name; }
	public String getName() { return parent.getName()+"."+name; }
	public String getDir() { return dir; }
	public String getMoveToDir() { return moveToDir; }
	public int getRuns() { return runs; }
	public int getSuccesses() { return successes; }
	public int getErrors() { return errors; }
	public boolean isPaused() { return paused || nrofConsecutivePollProblems>maxNrofConsecutivePollProblems; }
	public int getNumberOfDetectedFiles() { return nrofDetectedFiles; }
	public int getNumberOfProblematicFiles() { return nrofIgnoredFiles; }
	public int getNumberOfInProgressFiles() { return nrofInProgressFiles; }
	public int getNumberOfConsecutiveProblems() { return nrofConsecutivePollProblems; }
	public synchronized void reset() { known.clear(); retries.clear(); nrofConsecutivePollProblems=0; }
	
	public void setListener(PollerJobListener listener) { this.listener = listener; }

	private void pollOnce(FileServerConnection fsconn) {
		// Verwerking voor een bestand in een directory
		logger.debug("getting {}-list in directory {}", logname, dir);
		int tmpnrofIgnoredFiles=0;
		int tmpnrofDetectedFiles=0;
		for (String filename : fsconn.getDirectoryEntries(dir).keySet()) {
			if (".".equals(filename) || "..".equals(filename))
				continue;
			
			if (extension!=null && ! filename.endsWith(extension))
				continue;
			
			tmpnrofDetectedFiles++;

			if (shouldFileBeIgnored(fsconn,filename)) {
				tmpnrofIgnoredFiles++;
				continue;
			}
			
			logger.info(getFullName()+ " - {} {} gevonden, controleren tot er geen wijzigingen meer zijn.", logname, filename);
			if (isFileNotChangingAnymore(fsconn, filename))
				processFile(fsconn, filename);
		}
		this.nrofDetectedFiles=tmpnrofDetectedFiles;
		this.nrofIgnoredFiles=tmpnrofIgnoredFiles;
		try {
			this.nrofInProgressFiles=fsconn.getDirectoryEntries(moveToDir).size()-2;
		}
		catch (Exception e) { this.nrofInProgressFiles=9999; } // signal problem (e.g. missing directory)
	}
	

	// override to prevent logging (unless there is an error). processFile will log which task is ececuted
	@Override protected void logStart(Task task) {}
	@Override protected void logCompleted(Task task) {}

	private void processFile(FileServerConnection fsconn, String filename) {
		logger.info(getFullName()+" - {} {} is klaar om verplaatst te worden.",logname, dir + "/" + filename);
		PollerTask task=null;
		if (pollForEntireDirectories)
			task=new PollerTask(this, fsconn, dir + "/" + filename);
		else
			task=new PollerTask(this, fsconn, filename);
		
		task.startTransaction();
		boolean completed=false;
		try {
			tryToMove(task);
			logger.info(getFullName() + " - "+logname+" " + filename + " is verplaatst naar " + moveToDir);
			run(task);
			completed = true;
			synchronized (this) {
				known.remove(filename);
				retries.remove(filename);
			}
			listener.updateGuiSuccess(name, successes++);
			String msg=deleteInProgressFile? " and then removed" :"";
			LogService.log("info", getFlow().getClass().getSimpleName(), getFullName(), filename, "Polled file is succesfully moved from "+dir+" to "+moveToDir+" and handled "+msg);
		}
		finally {
			if (completed) {
				task.commit();
				if (deleteInProgressFile)
					task.deleteInProgressFile();
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
			logger.debug("directory {} gevonden, deze wordt overgeslagen bij alleen file verwerking.", dir + "/" + filename);
			return true;
		}
		return false;
	}

	private synchronized boolean isFileNotChangingAnymore(FileServerConnection fsconn, String filename) {
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
			if (otherSnapshot.getTimestamp() + delay < timestamp) {
				if (checkIfFileIsLocked && fsconn.isLocked(dir+"/"+filename))
					return false;
				if (minimumSize>0 && fsconn.fileSize(dir+"/"+filename)<minimumSize)
					return false;
				return true;
			}
		} else {
			known.put(filename, snapshot);
			retries.put(filename, 1);
		}
		return false;
	}
	
	
	private void tryToMove(PollerTask task) throws FileCouldNotBeMovedException {
		try {
			task.moveToInProgress();
		}
		catch (RuntimeException e) { 
			logger.warn(getFullName()+" Could not move "+logname+" "+task.filename+" to " +moveToDir, e);
			rememberFailedMove(task.filename, e);
			if (e instanceof FileCouldNotBeMovedException)
				throw e;
			throw new FileCouldNotBeMovedException(task.filename, e);
		}
	}

	private int getTryCount(String filename) {
		Integer tmp = retries.get(filename); 
		if (tmp==null)// test for null, because of unboxing
			return 0;
		return tmp;
	}
	
	private void rememberFailedMove(String filename, RuntimeException e) {
		int trycount=getTryCount(filename)+1;
		listener.updateGuiErrors(name, errors++);
		logger.debug("retrynummer {} van {}", trycount, filename);
		if (trycount < maxNrofMoveTries) {
			//LogService.log("WARN", "failed_move", getFullName(), "poller", "failed to move file "+filename+" to "+moveToDir);
			logger.warn(getFullName() + " - verplaatsen van file " + filename + " naar " + moveToDir + " is niet gelukt. Dit wordt later nog een keer geprobeerd."+e.getMessage());
		} else {
			LogService.log("error", "MoveToInProgress", getFullName(), filename, "failed "+(trycount-1)+" times to move file from "+dir+" to "+moveToDir+": "+e.getMessage());
			logger.error(getFullName() + " - verplaatsen van file " + filename + " naar " + moveToDir + " is niet gelukt niet na " + (trycount-1) + " keer proberen.");
			//known.remove(filename); // Zodat het weer vanaf begin opnieuw gaat, maar er is wel en Error gegeven.
		}
		retries.put(filename, trycount);
	}

	
	@Override public void writeHtml(PrintWriter out) {
		out.println("<H1>PollerJob</H1>");
		out.println("<table>");
		out.println("\t<tr><td><b>file</b></td><td><b>retries</b></td></tr>");
		for (String file: retries.keySet()) 
			out.println("\t<tr><td>"+file+"</td><td>"+retries.get(file)+"</td></tr>");
		out.println("</table>");
	}
}