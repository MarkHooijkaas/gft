package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.Module;
import org.kisst.gft.filetransfer.action.*;
import org.kisst.props4j.Props;

public class FileTransferModule implements Module {
	private final GftContainer gft;

	public FileTransferModule(GftContainer gft, Props props) {
		this.gft=gft;
	}
	
	@Override public void destroy() { }

	@Override public String getName() { return "FileTransferModule"; }

	@Override public void init(Props props) {
		gft.addAction("check_src",CheckSourceFile.class);
		gft.addAction("check_dest",CheckDestFileDoesNotExist.class);
		gft.addAction("copy",SftpGetPutAction.class);
		gft.addAction("check_copy",CheckCopiedFile.class);
		gft.addAction("remove",DeleteSourceFile.class);
		gft.addAction("archive_src",ArchiveSourceFile.class);
		gft.addAction("remove_src_dir_if_empty",DeleteSourceDirectoryIfEmpty.class);
		gft.addAction("fix_permissions",FixPermissions.class);
		gft.addAction("sftp_get", SftpGetAction.class);
		gft.addAction("sftp_put", SftpPutAction.class);
		gft.addAction("move_to_final_dest", MoveDestFileToFinalDestination.class);
	}

	@Override public void reset(Props props) {}
}
