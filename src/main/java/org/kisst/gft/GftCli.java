package org.kisst.gft;

import org.kisst.gft.ssh.GenerateKey;
import org.kisst.util.CryptoUtil;

import java.io.File;
import java.io.IOException;

public class GftCli {
    protected final Cli cli=new Cli();
    protected final  Cli.StringOption config;
    private final Cli.Flag help =cli.flag("h", "help", "show this help");
    private final Cli.Flag keygen =cli.flag("k", "keygen", "generate a public/private keypair");
    private final Cli.StringOption encrypt = cli.stringOption("e","encrypt","key", null);
    private final Cli.StringOption decrypt = cli.stringOption("d","decrypt","key", null);
    private final Cli.StringOption decode = cli.stringOption(null,"decode","decode a base64 string", null);
    protected final String topname;

    public GftCli(String topname, String args[]) {
		this.topname = topname;
		CryptoUtil.setKey("AB451204BD47vgtznh4r8-9yr45blfrui6093782");
		config = cli.stringOption("c", "config", "configuration file", null);
		cli.parse(args);
	}

	public File getConfigFile() {
		String fname= config.get();
		if (fname!=null)
			return new File(fname);
		File result=new File(topname+".properties");
		if (result.exists())
			return result;
		result=new File("config/"+topname+".properties");
		if (result.exists())
			return result;
		return new File("config."+topname+"/"+topname+".properties");
	}
    public boolean handle() {
        if (help.isSet())
            showHelp();
        else if (keygen.isSet())
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
        else
			return false;
        return true;
    }

	public boolean localCommand() {
    	return encrypt.isSet() | decrypt.isSet() | decode.isSet() || help.isSet();
	}

    private void showHelp() {
        System.out.println("usage: java -jar gft.jar [options]");
        System.out.println(cli.getSyntax(""));
    }

}
