package org.kisst;

import org.kisst.gft.GftCli;
import org.kisst.gft.GftCli2;
import org.kisst.gft.GftRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GftApplication {

	public static void main(String[] args) {
		GftCli cli=new GftCli2("gft", args);
		if (cli.handle())
			return;

		GftRunner runner = new GftRunner(cli.getConfigFile().getName());
		runner.start();
		SpringApplication.run(GftApplication.class, args);
	}
}
