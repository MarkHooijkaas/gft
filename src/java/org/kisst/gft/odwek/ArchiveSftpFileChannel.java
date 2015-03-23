package org.kisst.gft.odwek;

import org.kisst.gft.GftContainer;
import org.kisst.gft.filetransfer.FileLocation;
import org.kisst.gft.filetransfer.action.SourceFile;
import org.kisst.props4j.Props;

public abstract class ArchiveSftpFileChannel extends OnDemandChannel implements SourceFile {
	private final FileLocation src;

	public ArchiveSftpFileChannel(GftContainer gft, Props props, OnDemandDefinition def) {
		super(gft, props, def);
		this.src=new FileLocation(gft.sshhosts.get(props.getString("src.host")),props.getString("src.dir",  ""));
	}
	@Override public String getSrcDescription() { return src.getShortString(); }
	@Override public FileLocation getSourceFile() { return src;}
}
