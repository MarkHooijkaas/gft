package org.kisst.gft.action;

import org.kisst.cfg4j.Props;
import org.kisst.gft.GftContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalCommandAction {
	private static final Logger logger = LoggerFactory
			.getLogger(LocalCommandAction.class);
	private String name;
	private String command;
	private String action;

	public LocalCommandAction(GftContainer gft, Props props) {
		this.name = props.getLocalName();
		command = props.getString("command", null);
		action = props.getString("action", null);

	}

	public void StartPollerAction(String file, String moveToDir) {

		if (command != null) {
			String s = name + " - Starten met action " + command
					+ " voor folder " + file;
			logger.info(s);

			try {
				long d = new java.util.Date().getTime();
				logger.debug("start run {}", d);

				Process p = Runtime.getRuntime().exec(command);
				p.waitFor();

				long d2 = new java.util.Date().getTime();
				logger.debug("klaar run {}", d2);

				// TODO wat zijn de waardes die Eyes&Hands terug geeft???
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
		}
		else if (action != null){
			String s = name + " - Starten met action " + action + " voor " + file;
			logger.info(s);
			//TODO start channel richting GFT.
			
		}else{
			logger.error("geen actie of command gevonden voor poller {}", name);
		}

	}

}
