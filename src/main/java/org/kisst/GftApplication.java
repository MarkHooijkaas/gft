package org.kisst;

import org.kisst.gft.BaseRunner;
import org.kisst.gft.GftCli;
import org.kisst.gft.GftCli2;
import org.kisst.gft.filetransfer.FileTransferModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GftApplication {

	public static void main(String[] args) {
		GftCli cli=new GftCli2("gft", args);
		if (cli.handle())
			return;

		BaseRunner runner = new BaseRunner("gft", cli.getConfigFile(), FileTransferModule.class);
		runner.start();
		SpringApplication.run(GftApplication.class, args);
	}
}
