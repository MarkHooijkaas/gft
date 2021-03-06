package org.kisst.gft.action;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalCommandAction implements Action{
	private static final Logger logger = LoggerFactory.getLogger(LocalCommandAction.class);
	private final String name;
	private final String command;
	private final boolean safeToRetry;

	public LocalCommandAction(GftContainer gft, Props props) {
		name = props.getLocalName();
		command = props.getString("command", null);
		safeToRetry = props.getBoolean("safeToRetry", false);
	}

	@Override public void execute(Task task) {
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
				// Als er een windows popup (foutmelding) is het antwoord ook 0.
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
	}

	@Override public boolean safeToRetry() { return safeToRetry; }

}
