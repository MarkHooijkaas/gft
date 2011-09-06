package org.kisst.gft.action;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalCommandAction implements Action{
	private static final Logger logger = LoggerFactory.getLogger(LocalCommandAction.class);
	private String name;
	private String command;



	public LocalCommandAction(GftContainer gft, Props props) {
		name = props.getLocalName();
		command = props.getString("command", null);
	}

//	public void StartPollerAction(String file, String moveToDir) {
//
//		if (command != null) {
//			String s = name + " - Starten met action " + command
//			+ " voor folder " + file;
//			logger.info(s);
//
//			try {
//				long d = new java.util.Date().getTime();
//				logger.debug("start run {}", d);
//
//				Process p = Runtime.getRuntime().exec(command);
//				p.waitFor();
//
//				long d2 = new java.util.Date().getTime();
//				logger.debug("klaar run {}", d2);
//
//				// TODO wat zijn de waardes die Eyes&Hands terug geeft???
//				if (p.exitValue() != 0)
//					logger.warn("return value van aan roep is {}", p
//							.exitValue());
//				else
//					logger.debug("return value van aan roep is {}", p
//							.exitValue());
//
//			} catch (InterruptedException e) {/* IGNORE */
//			} catch (Exception err) {
//				err.printStackTrace();
//			}
//		}else{
//			logger.error("geen actie of command gevonden voor poller {}", name);
//		}
//
//	}

   
	public Object execute(Task task) {
		if (command != null) {
			logger.info("{} - Starten met action {}", name, command);

			try {
				long d = new java.util.Date().getTime();
				logger.debug("start run {}", d);

				Process p = Runtime.getRuntime().exec(command);
				p.waitFor();

				long d2 = new java.util.Date().getTime();
				logger.debug("klaar run {}", d2);

				// TODO wat zijn de waardes die Eyes&Hands terug geeft???
				logger.debug("waarde antwoord commandexec : {}", p.exitValue());				
				if (p.exitValue() != 0)
					logger.warn("return value van aan roep is {}", p
							.exitValue());
				else
					logger.debug("return value van aan roep is {}", p
							.exitValue());

			} catch (InterruptedException e) {/* IGNORE */
			} catch (Exception err) {
				err.printStackTrace();
			}
		}else{
			logger.error("geen actie of command gevonden voor poller {}", name);
		}		
		
		return null;
	}

	@Override
	public boolean safeToRetry() {
		return false;
	}

}
