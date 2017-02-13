package org.kisst.gft;

import nl.duo.gft.algemeen.GftDuoAlgemeenModule;
import nl.duo.gft.chesicc.ChesiccModule;
import nl.duo.gft.filetransfer.DuoFileTransferModule;
import nl.duo.gft.klaarzettenBestand.KlaarzettenBestandModule;
import nl.duo.gft.vzub.VzubModule;
import org.kisst.gft.filetransfer.FileTransferModule;

public class GftRunner extends BaseRunner {
	public GftRunner(String configfilename) {
		super("gft", configfilename,
				FileTransferModule.class,
				DuoFileTransferModule.class,
				GftDuoAlgemeenModule.class,
				KlaarzettenBestandModule.class,
				VzubModule.class,
				ChesiccModule.class
		);
	}

	public static void main(String[] args) {
		GftCli cli=new GftCli(args);
		cli.main(new GftRunner(cli.getConfigFile()));
	}
}
