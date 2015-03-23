package org.kisst.gft.odwek;

import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.gft.task.BasicGftFlow;

public abstract class ArchiveSftpFileChannel extends OnDemandChannel implements SourceFile {
	private final FileLocation src;

	public ArchiveSftpFileChannel(BasicGftFlow flow, OnDemandDefinition def) {
		super(flow, def);
		this.src=new FileLocation(gft.sshhosts.get(props.getString("src.host")),props.getString("src.dir",  ""));
	}
	@Override public String getSrcDescription() { return src.getShortString(); }
	@Override public FileLocation getSourceFile() { return src;}
}
