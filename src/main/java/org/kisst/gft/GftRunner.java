package org.kisst.gft;

import org.kisst.gft.filetransfer.FileTransferModule;

public class GftRunner extends BaseRunner {
	@SuppressWarnings("unchecked")
	public GftRunner(String configfilename) {
		super("gft", configfilename,
				FileTransferModule.class
		);
	}
}
