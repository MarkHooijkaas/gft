package org.kisst.gft.action;

import java.io.FileOutputStream;
import java.io.IOException;

import org.kisst.gft.GftContainer;
import org.kisst.gft.task.BasicTask;
import org.kisst.gft.task.SoapTask;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;
import org.kisst.util.Base64;

public class DecodeBase64ToFileAction implements Action{
	//private static final Logger logger = LoggerFactory.getLogger(DecodeBase64ToFileAction.class);

	public DecodeBase64ToFileAction(GftContainer gft, Props props) {}

	@Override
	public Object execute(Task task) {
		SoapTask soaptask =  (SoapTask) task;
		BasicTask basictask = (BasicTask) task;
		try {
			FileOutputStream fos = null;
			try {
				String encoded = soaptask.getContent().getChildText("bestandsinhoud"); 
				byte[] bytes = Base64.decode(encoded);
				fos = new FileOutputStream(basictask.getTempFile());
				fos.write(bytes);
				return null;
			}
			finally {
				if (fos!=null)
					fos.close();
			}
		} 
		catch (IOException e) { throw new RuntimeException(e);} 
	}

	@Override
	public boolean safeToRetry() {
		return false;
	}


}
