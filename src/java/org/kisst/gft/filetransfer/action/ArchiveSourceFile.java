package org.kisst.gft.filetransfer.action;

import org.kisst.gft.action.BaseAction;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.FileServerConnection;
import org.kisst.gft.task.Task;
import org.kisst.props4j.Props;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ArchiveSourceFile extends BaseAction {
	private final String archiveDir;
	private final String datePrefixPattern;
	public ArchiveSourceFile(Props props) {
		super(props);
		this.archiveDir=props.getString("archiveDir");
		this.datePrefixPattern=props.getString("datePrefixPattern",null);
	}

	@Override public boolean safeToRetry() { return true; }

	@Override public void execute(Task task) {
		//SourceFile srctask= (SourceFile) task;
		FileLocation src = ((SourceFile) task).getSourceFile();
		FileServerConnection fsconn=src.getFileServer().openConnection();
		try {
			if (datePrefixPattern==null)
				fsconn.move(src.getPath(), archiveDir);
			else {
				DateFormat formatter = new SimpleDateFormat(datePrefixPattern);
				String path=src.getPath();
				int pos=path.lastIndexOf('/');
				if (pos>0)
					path=path.substring(pos+1);
				String filename=formatter.format(System.currentTimeMillis())+path;
				fsconn.move(src.getPath(), archiveDir+"/"+filename);
			}
		}
		finally {
			if (fsconn!=null)
				fsconn.close();
		}
	}

}
