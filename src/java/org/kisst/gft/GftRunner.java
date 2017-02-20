package org.kisst.gft;

import org.kisst.gft.filetransfer.FileTransferModule;

public class GftRunner extends BaseRunner {
	public GftRunner(String configfilename) {
		super("gft", configfilename,
				FileTransferModule.class
		);
	}

	public static void main(String[] args) {
		GftCli cli=new GftCli(args);
		cli.main(new GftRunner(cli.getConfigFile()));
	}
}
