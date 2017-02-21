package org.kisst.gft;

import org.apache.log4j.PropertyConfigurator;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.ssh.GenerateKey;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GftCli {
    final Logger logger= LoggerFactory.getLogger(Channel.class);

    protected final Cli cli=new Cli();
    private final  Cli.StringOption config;
    private final Cli.Flag help =cli.flag("h", "help", "show this help");
    private final Cli.Flag keygen =cli.flag("k", "keygen", "generate a public/private keypair");
    private final Cli.StringOption encrypt = cli.stringOption("e","encrypt","key", null);
    private final Cli.StringOption decrypt = cli.stringOption("d","decrypt","key", null);
    private final Cli.StringOption decode = cli.stringOption(null,"decode","decode a base64 string", null);
    protected final String topname;

    public GftCli(String topname, String args[]) {
		this.topname = topname;
		CryptoUtil.setKey("AB451204BD47vgtznh4r8-9yr45blfrui6093782");
		config = cli.stringOption("c", "config", "configuration file", "gft.properties");
		cli.parse(args);
	}

    public File getConfigFile() { return new File(config.get()); }
    public void main() {
        if (help.isSet()) {
            showHelp();
            return;

        }
        try {
            PropertyConfigurator.configure(getConfigFile().getParent()+"/log4j.properties");
        }
        catch (UnsatisfiedLinkError e) { // TODO: a bit of a hack to prevent log4j Link error
            System.out.println("Linking Error initializing log4j, probably you should execute \"set PATH=%PATH%;lib\"");
            if (localCommand())
                System.out.println("WARNING: could not initialize log4j properly:"+e.getMessage());
            else
                throw e;
        }

        if (keygen.isSet())
            GenerateKey.generateKey(getConfigFile().getParentFile().getAbsolutePath()+"/ssh/id_dsa_gft"); // TODO: should be from config file
        else if (encrypt.get()!=null)
            System.out.println(CryptoUtil.encrypt(encrypt.get()));
        else if (decrypt.get()!=null) {
            System.out.println("OPTION DISABLED");
            //System.out.println(CryptoUtil.decrypt(decrypt.get()));
        }
        else if (decode.get()!=null) {
            try {
                System.out.println(new String(org.kisst.util.Base64.decode(decode.get())));
            }
            catch (IOException e) { throw new RuntimeException(e); }
        }
    }

	public boolean localCommand() {
    	return encrypt.isSet() | decrypt.isSet() | decode.isSet() || help.isSet();
	}

    private void showHelp() {
        System.out.println("usage: java -jar gft.jar [options]");
        System.out.println(cli.getSyntax(""));
    }

}
