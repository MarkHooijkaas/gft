package org.kisst.gft.poller;

import java.io.PrintWriter;
import java.util.HashMap;


import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileCouldNotBeMovedException;
import org.kisst.gft.filetransfer.FileServer;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.filetransfer.FoundDirectoryTask;
import org.kisst.gft.filetransfer.FoundFileTask;
import org.kisst.gft.task.BasicTaskDefinition;
import org.kisst.props4j.Props;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollerJob extends BasicTaskDefinition {
	private static final Logger logger = LoggerFactory.getLogger(PollerJob.class);

	private final String dir;
	private final String moveToDir;
	private boolean pollForEntireDirectories;
	private final HashMap<String, DirectorySnapshot> known = new HashMap<String, DirectorySnapshot>();
	private final HashMap<String, Integer> retries = new HashMap<String, Integer>();
	private final FileServer fileserver;
	private int delay;
	private boolean paused = false;

	private int runs = 0;
	private int successes = 0;
	private int errors = 0;

	public String currentFile;
	
	private PollerJobListener listener = new DummyListener();

	private final HashMap<String, FileSnapshot> knownFile = new HashMap<String, FileSnapshot>();

	public PollerJob(GftContainer gft,Props props, FileServer fileserver) {
		super(gft, props, "send_gft_message");
		this.fileserver = fileserver;
		delay = props.getInt("delay", 10000);
		dir = TemplateUtil.processTemplate(props.getString("pollerDirectory"),gft.getContext());
		moveToDir = TemplateUtil.processTemplate(props.getString("moveToDirectory"),gft.getContext());
		pollForEntireDirectories = props.getBoolean("pollForEntireDirectories",
				false);
		paused = props.getBoolean("paused", false);
	}

	public PollerJob(GftContainer gft,Props props) {
		this(gft, props, null);
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
			if (pollForEntireDirectories) {
				checkDirectory(fsconn);
			} else {
				checkFiles(fsconn);
			}
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
	public String getName() {
		return name;
	}

	public String getDir() {
		return dir;
	}

	public String getMoveToDir() {
		return moveToDir;
	}

	
	public int getRuns() {
		return runs;
	}

	public int getSuccesses() {
		return successes;
	}

	public int getErrors() {
		return errors;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setListener(PollerJobListener listener) {
		this.listener = listener;
	}

	private void checkFiles(FileServerConnection fsconn) {
		// Verwerking voor een bestand in een directory
		logger.info("getting files in directory {}", dir);
		 
		for (String f : fsconn.getDirectoryEntries(dir).keySet()) {
			if (".".equals(f) || "..".equals(f))
				continue;
			if (fsconn.isDirectory(dir + "/" + f)){
				logger.info("directory {} gevonden, deze wordt overgeslagen bij alleen file verwerking.", dir + "/" + f);
				continue;
			}
				
			logger
					.info(
							"{} - File {} gevonden, controleren tot er geen wijzigingen meer zijn.",
							name, f);
			FileSnapshot snapFile = new FileSnapshot(fsconn, dir + "/"
					+ f);
			FileSnapshot otherFile = knownFile.get(f);
			if (otherFile == null) {
				knownFile.put(f, snapFile);
				retries.put(f, 1);
			} else {
				if (snapFile.equals(otherFile)) {
					long timestamp = new java.util.Date().getTime();
					if (otherFile.getTimestamp() + delay < timestamp) {
						logger
								.debug(
										"{} - File {} is klaar om verplaatst te worden.",
										name, dir + "/" + f);
						boolean moved = false;
						try {
							fsconn.move(dir + "/" + f,	moveToDir + "/" + f);
							moved=true;
						}
						catch (FileCouldNotBeMovedException e) { /* ignore */ }
						if (moved) {
							FoundFileTask task=new FoundFileTask(gft,this, f);
							run(task);
							String s = name + " - Folder " + f	+ " is verplaatst naar " + moveToDir;
							logger.info(s);
							knownFile.remove(f);
							listener.updateGuiSuccess(name, successes++);
						} else {
							listener.updateGuiErrors(name, errors++);
							int aantal = retries.get(f);
							logger.debug("retrynummer {} van {}",
									aantal, f);
							if (retries.get(f) < props.getInt(
									"moveRetries", 3)) {
								String s = name
										+ " - verplaatsen van file "
										+ f
										+ " naar "
										+ moveToDir
										+ " is niet gelukt. Dit wordt later nog een keer geprobeerd.";
								logger.warn(s);
								retries.put(f, aantal + 1);
							} else {
								String s = name
										+ " - verplaatsen van file "
										+ f + " naar " + moveToDir
										+ " is niet gelukt niet na "
										+ aantal + " keer proberen.";
								logger.error(s);
								knownFile.remove(f); // Zodat het weer
														// van
								// af begin
								// opnieuw gaat, maar er is wel en Error
								// gegeven.
							}
						}
					}
				} else {
					knownFile.put(f, snapFile);
					retries.put(f, 1);
				}
			}
		}
	}

	private void checkDirectory(FileServerConnection fsconn) {
		// Verwerking voor een volledige directory met alles erin
		logger.info("getting directory from {}", fsconn);
		for (String f : fsconn.getDirectoryEntries(dir).keySet()) {
			if (".".equals(f) || "..".equals(f))
				continue;
			if (fsconn.isDirectory(dir + "/" + f)) {
				logger
						.info(
								"{} - Folder {} gevonden, controleren tot er geen wijzigingen meer zijn.",
								name, f);
				DirectorySnapshot snap = new DirectorySnapshot(fsconn,
						dir + "/" + f);
				DirectorySnapshot other = known.get(f);
				if (other == null) {
					known.put(f, snap);
					retries.put(f, 1);
				} else {
					if (snap.equals(other)) {
						long timestamp = new java.util.Date().getTime();
						if (other.getTimestamp() + delay < timestamp) {
							logger
									.debug(
											"{} - Geen wijzigingen meer in folder {}, klaar om verplaatst te worden.",
											name, dir + "/" + f);
							boolean moved = false;
							try {
								fsconn.move(dir + "/" + f,	moveToDir + "/" + f);
								moved=true;
							}
							catch (FileCouldNotBeMovedException e) { /* ignore TODO: waarom niet? file is nog bezig of zo */ }
							if (moved) {
								String s = name + " - Folder " + f
										+ " is verplaatst naar "
										+ moveToDir;
								logger.info(s);

								FoundDirectoryTask task=new FoundDirectoryTask(gft, this, dir + "/" + f);
								run(task);
								known.remove(f);
								listener.updateGuiSuccess(name,
										successes++);
							} else {
								listener
										.updateGuiErrors(name, errors++);
								int aantal = retries.get(f);
								logger.debug("retrynummer {} van {}",
										aantal, f);
								if (retries.get(f) < props.getInt(
										"moveRetries", 3)) {
									String s = name
											+ " - verplaatsen van file "
											+ f
											+ " naar "
											+ moveToDir
											+ " is niet gelukt. Dit wordt later nog een keer geprobeerd.";
									logger.warn(s);
									retries.put(f, aantal + 1);
								} else {
									String s = name
											+ " - verplaatsen van file "
											+ f
											+ " naar "
											+ moveToDir
											+ " is niet gelukt niet na "
											+ aantal
											+ " keer proberen.";
									logger.error(s);
									known.remove(f); // Zodat het weer
									// van af begin
									// opnieuw gaat, maar er is wel en
									// Error gegeven.
								}
							}
						}
					} else {
						known.put(f, snap);
						retries.put(f, 1);
					}

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