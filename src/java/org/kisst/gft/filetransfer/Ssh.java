package org.kisst.gft.filetransfer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class Ssh {
	public static class ExitCodeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public ExitCodeException (SshHost host, String command, int exitvalue, String result) { 
			super("On host "+host.host+" when running command \""+command+"\" resulted in exitcode "+exitvalue+"\noutput was:"+result);
		}
	}

	private static final Logger logger=LoggerFactory.getLogger(Ssh.class);
	static {
		JSch.setLogger(new MyLogger());
	}
	
	//public static String ssh(Credentials cred, String host, String command) {

	public static String ssh(SshHost host, Ssh.Credentials cred, String command) {
		logger.info("Calling {} with command [{}]", host, command);
		FileOutputStream fos=null;
		try{
			JSch jsch=new JSch();
			if (host.known_hosts!=null)
				jsch.setKnownHosts(host.known_hosts);

			if (cred.keyfile!=null) {
				logger.debug("Using keyfile {}",cred.keyfile);
				jsch.addIdentity(cred.keyfile);
			}
			Session session=jsch.getSession(cred.user, host.host, 22);

			// username and password will be given via UserInfo interface.
			session.setUserInfo(cred);
			session.connect();

			// exec 'scp -f rfile' remotely
			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);
			((ChannelExec)channel).setAgentForwarding(true);

			ByteArrayOutputStream err = new ByteArrayOutputStream();
			((ChannelExec)channel).setErrStream(err);
			channel.setInputStream(null);
			InputStream in=channel.getInputStream();

			channel.connect();

			StringBuilder result= new StringBuilder();
			byte[] tmp=new byte[1024];
			int i;
			do {
				i=in.read(tmp, 0, 1024);
				if(i>0)
					result.append(new String(tmp, 0, i));
			} while (i>=0);

			if(channel.isClosed()){
				int exitvalue = channel.getExitStatus();
				if (exitvalue!=0)
					throw new ExitCodeException(host, command, exitvalue, result.toString());
			}
			//channel.disconnect();
			session.disconnect();
			result.append(err.toString());
			logger.info("Call to {} returned [{}]", host, result);
			return result.toString();
		}
		catch(JSchException e) { throw new RuntimeException(e); }
		catch(IOException e)   { throw new RuntimeException(e); }
		finally {
			try {
				if(fos!=null) fos.close();
			}
			catch(IOException e) { throw new RuntimeException(e); }
		}
	}

	public static class MyLogger implements com.jcraft.jsch.Logger {
		public boolean isEnabled(int level){
			if (level==DEBUG) return logger.isTraceEnabled();
			if (level==INFO)  return logger.isDebugEnabled();
			if (level==WARN)  return logger.isWarnEnabled();
			if (level==ERROR) return logger.isErrorEnabled();
			if (level==FATAL) return true;
			return false;
		}
		public void log(int level, String message){
			if (level==DEBUG) logger.trace(message);
			if (level==INFO)  logger.debug(message);
			if (level==WARN)  logger.warn(message);
			if (level==ERROR) logger.error(message);
			if (level==FATAL) logger.error(message);
		}
	}


	public static class Credentials implements UserInfo , UIKeyboardInteractive{
		private final String user;
		private final String password;
		private final String keyfile;

		public Credentials(String user, String password, String keyfile) {
			this.user=user;
			this.password=password;
			this.keyfile=keyfile;
			if (keyfile!=null) {
				File f=new File(keyfile);
				if (! f.exists())
					throw new RuntimeException("keyfile "+f+" does not exist");
				if (! f.isFile())
					throw new RuntimeException("keyfile "+f+" is not a file");
			}
		}

		public void showMessage(String message){ logger.debug("Message: {}",message); }
		public boolean promptYesNo(String str){ logger.debug("YesOrNo: {}",str); return true;  }

		public boolean promptPassphrase(String message){ logger.debug("prompt Passphrase: {}",message); return true; }
		public String getPassphrase(){ return ""; }

		public boolean promptPassword(String message) { logger.debug("prompt Password: {}",message); return true; }
		public String getPassword(){ logger.debug("using Password: {}",password); return password; }

		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo) {
			logger.debug("destination: {}",destination); 
			logger.debug("name: {}",name);
			logger.debug("instruction: {}",instruction);
			for (String s: prompt)
				logger.debug("prompt[i]: {}",s);
			for (boolean b: echo)
				logger.debug("echo[i]: {}",b); 
			return null;
		}
	}
}
