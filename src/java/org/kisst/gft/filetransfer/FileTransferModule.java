package org.kisst.gft.filetransfer;

import org.kisst.gft.GftContainer;
import org.kisst.gft.Module;
import org.kisst.gft.filetransfer.action.CheckCopiedFile;
import org.kisst.gft.filetransfer.action.CheckDestFileDoesNotExist;
import org.kisst.gft.filetransfer.action.CheckSourceFile;
import org.kisst.gft.filetransfer.action.CopyFile;
import org.kisst.gft.filetransfer.action.DeleteSourceDirectoryIfEmpty;
import org.kisst.gft.filetransfer.action.DeleteSourceFile;
import org.kisst.gft.filetransfer.action.FixPermissions;
import org.kisst.gft.filetransfer.action.MoveDestFileToFinalDestination;
import org.kisst.gft.filetransfer.action.SftpGetAction;
import org.kisst.gft.filetransfer.action.SftpPutAction;
import org.kisst.gft.task.TaskDefinition;
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
		gft.addAction("copy",CopyFile.class);
		gft.addAction("check_copy",CheckCopiedFile.class);
		gft.addAction("remove",DeleteSourceFile.class);
		gft.addAction("remove_src_dir_if_empty",DeleteSourceDirectoryIfEmpty.class);
		gft.addAction("fix_permissions",FixPermissions.class);
		gft.addAction("sftp_get", SftpGetAction.class);
		gft.addAction("sftp_put", SftpPutAction.class);
		gft.addAction("move_to_final_dest", MoveDestFileToFinalDestination.class);
	}

	@Override public void reset(Props props) {}

	@Override
	public TaskDefinition createDefinition(String type, Props props) {
		throw new RuntimeException("Cannot creat channel of type "+type); // this should never happen
	}

}
