package org.kisst.gft;

import com.ibm.mq.MQException;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.pgm.Main;
import org.kisst.gft.filetransfer.Channel;
import org.kisst.gft.ssh.GenerateKey;
import org.kisst.jms.ActiveMqSystem;
import org.kisst.jms.JmsSystem;
import org.kisst.jms.JmsUtil;
import org.kisst.mq.MsgMover;
import org.kisst.mq.QueueManager;
import org.kisst.props4j.Props;
import org.kisst.props4j.SimpleProps;
import org.kisst.util.CryptoUtil;
import org.kisst.util.FileUtil;
import org.kisst.util.JarLoader;
import org.kisst.util.TemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

public class GftCli {
    final static Logger logger= LoggerFactory.getLogger(Channel.class);

    private static Cli cli=new Cli();
    private static Cli.StringOption config;
    private static Cli.StringOption putmsg = cli.stringOption("p","putmsg", "puts a message from the named file on the input queue",null);
    private static Cli.StringOption delmsg = cli.stringOption("d","delmsg","selector", null);
    private static Cli.StringOption mvmsg = cli.stringOption("m","mvmsg","move message with <str> as msgid (or @all) from errorqueue to main queue", null);
    private static Cli.Flag help =cli.flag("h", "help", "show this help");
    private static Cli.Flag keygen =cli.flag("k", "keygen", "generate a public/private keypair");
    private static Cli.StringOption encrypt = cli.stringOption("e","encrypt","key", null);
    private static Cli.StringOption decrypt = cli.stringOption("d","decrypt","key", null);
    private static Cli.StringOption decode = cli.stringOption(null,"decode","decode a base64 string", null);
    private static Cli.SubCommand jgit =cli.subCommand("jgit", "run jgit CLI");
    private static Cli.SubCommand git =cli.subCommand("git", "run jgit CLI");
    private static Cli.SubCommand backup =cli.subCommand("backup", "backup the config directory to git");


    public static void main(String topname, String[] args, Class<? extends Module> ... modules) {
        config = cli.stringOption("c","config","configuration file", null);
        String[] newargs = cli.parse(args);
        if (help.isSet()) {
            showHelp();
            return;
        }
        CryptoUtil.setKey("-P34{-[u-C5x<I-v'D_^{79'3g;_2I-P_L0�_j3__5`y�%M�_C");
        String configfilename = config.get();
        GftRunner runner =new GftRunner(topname, configfilename, modules);
        try {
            PropertyConfigurator.configure(runner.configfile.getParent()+"/"+topname+".log4j.properties");
        }
        catch (UnsatisfiedLinkError e) { // TODO: a bit of a hack to prevent log4j Link error
            System.out.println("Linking Error initializing log4j, probably you should execute \"set PATH=%PATH%;lib\"");
            if (backup.isSet() || jgit.isSet() || git.isSet() | mvmsg.isSet() | delmsg.isSet() | putmsg.isSet() | encrypt.isSet() | decrypt.isSet())
                System.out.println("WARNING: could not initialize log4j properly:"+e.getMessage());
            else
                throw e;
        }
        SimpleProps props=new SimpleProps();
        props.load(runner.configfile);
        props=(SimpleProps) props.getProps(topname);
        if (jgit.isSet() || git.isSet()) {
            if (System.getProperty("jgit.gitprefix")==null)
                System.setProperty("jgit.gitprefix",props.getString("jgit.gitprefix","D:\\git"));
            Main.main(newargs);
        }
        else if (backup.isSet()) {
            backup(newargs, props);
        }
        else if (keygen.isSet())
            GenerateKey.generateKey(runner.configfile.getParentFile().getAbsolutePath()+"/ssh/id_dsa_gft"); // TODO: should be from config file
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
        else if (putmsg.isSet())
            putmsg(props,putmsg.get());
        else if (delmsg.isSet())
            delmsg(props, delmsg.get());
        else if (mvmsg.isSet())
            moveMessage(props, mvmsg.get());
        else {
            runner.run();
            System.out.println("GFT stopped");
        }
    }

    private static void backup(String[] args, SimpleProps props) {
        if (System.getProperty("jgit.gitprefix")==null)
            System.setProperty("jgit.gitprefix",props.getString("jgit.gitprefix","D:\\git"));
        try {
            boolean newRepo=false;
            if (! new File(".git").isDirectory()) {
                System.out.println("No backup repository yet, will create a new git repository");
                if (! new File(".gitignore").isFile()) {
                    System.out.println("No .gitignore file, will create default, ignoring logs and ssh keys");
                    PrintWriter writer = new PrintWriter(".gitignore", "UTF-8");
                    writer.println("config/ssh");
                    writer.println("logs");
                    writer.close();
                }
                newRepo=true;
            }
            Git git = Git.init().call();
            Status status= git.status().call();
            boolean newConfigFile=false;
            for (String str: status.getUntracked()	) {
                if (str.startsWith("config/")) {
                    System.out.println("New      "+str);
                    newConfigFile=true;
                }
            }
            if (newConfigFile || newRepo || status.hasUncommittedChanges()) {
                git.add().addFilepattern("config").call();
                git.add().addFilepattern(".gitignore").call();
                for (String str: status.getAdded())    { System.out.println("Added    "+str); }
                for (String str: status.getChanged())  { System.out.println("Changed  "+str); }
                for (String str: status.getMissing())  { System.out.println("Missing  "+str); }
                for (String str: status.getModified()) { System.out.println("Modified "+str); }
                for (String str: status.getRemoved())  { System.out.println("Removed  "+str); }
                for (String str: status.getUncommittedChanges())  { System.out.println("Uncommitted  "+str); }
                String comment = getGitComment(args);
                if (comment.length()==0)
                    System.out.println("Nothng backed up, commit is mandatory");
                else {
                    for (String str: status.getMissing())  { git.rm().addFilepattern(str).call(); }
                    git.commit().setMessage(comment).call();
                }
            }
            else
                System.out.println("No changes to backup");
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String getGitComment(String[] args) {
        if (args.length==0) {
            System.out.println("commit commentaar is verplicht, type een regel die beschrijft waarom of wat er veranderd is, b.v. een RFC nummer");
            return System.console().readLine().trim();
        }
        String comment=args[0];
        for (int i=1; i<args.length; i++)
            comment+=" "+args[i];
        return comment;
    }

    private static String getQueue(SimpleProps props) {
        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put("global", props.get("global", null));
        String queuename = TemplateUtil.processTemplate(props.getString("listener.main.queue"), context);
        return queuename;
    }

    private static void showHelp() {
        System.out.println("usage: java -jar gft.jar [options]");
        System.out.println(cli.getSyntax(""));
    }

    private static JmsSystem getQueueSystem(Props props) {
        new JarLoader(props, "gft").getMainClasses();
        Props qmprops=props.getProps("mq.host.main");
        String type=qmprops.getString("type");
        if ("ActiveMq".equals(type))
            return new ActiveMqSystem(qmprops);
        else if ("Jms".equals(type))
            return new JmsSystem(qmprops);
        else
            throw new RuntimeException("Unknown type of queueing system "+type);
    }

    private static void putmsg(SimpleProps props, String filename) {
        logger.info("gft put");
        JmsSystem queueSystem=getQueueSystem(props);
        String queuename = getQueue(props);
        String data=null;
        if (filename==null || "-".equals(filename)) {
            logger.info("loading data from standard input");
            data= FileUtil.loadString(new InputStreamReader(System.in));
        }
        else {
            logger.info("loading data from file "+filename);
            data=FileUtil.loadString(filename);
        }
        logger.info("sending message");
        queueSystem.getQueue(queuename).send(data);
        logger.info("send the following message to the queue {}",queuename);
        logger.debug("data send was {}",data);
        System.out.println("Sent message to "+queuename);
        queueSystem.close();
    }

    private static void delmsg(SimpleProps props,String selector) {
        JmsSystem queueSystem=getQueueSystem(props);
        String queuename = getQueue(props);
        logger.info("removing the following message "+selector);
        try {
            Session session = queueSystem.getConnection().createSession(true, Session.SESSION_TRANSACTED);
            MessageConsumer consumer = session.createConsumer(session.createQueue(queuename), selector);
            Message msg = consumer.receive(5000);
            if (msg==null)
                logger.info("Could not find message "+selector);
            else {
                session.commit();
                logger.info("Removed message "+selector);
            }
            queueSystem.close();
        }
        catch (JMSException e) { throw JmsUtil.wrapJMSException(e); }
    }

    private static void moveMessage(Props props, String msgid) {
        String src=props.getString("listener.main.errorqueue");
        String dest=props.getString("listener.main.queue");
        try {
            JmsSystem jmsSystem = new JmsSystem(props.getProps("mq.host.main"));
            MsgMover.moveMessage(new QueueManager(jmsSystem.createMQQueueManager()), src, dest, msgid);
        }
        catch (MQException e) { throw new RuntimeException(e); }
    }


}
