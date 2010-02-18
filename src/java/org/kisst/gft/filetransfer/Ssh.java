package org.kisst.gft.filetransfer;

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
	private static final Logger logger=LoggerFactory.getLogger(Ssh.class);
	static {
		JSch.setLogger(new MyLogger());
	}

	public static String ssh(Credentials cred, String host, String command) {
		FileOutputStream fos=null;
		try{
			JSch jsch=new JSch();

			if (cred.keyfile!=null) {
				logger.info("Using keyfile {}",cred.keyfile);
				jsch.addIdentity(cred.keyfile);
			}
			Session session=jsch.getSession(cred.user, host, 22);

			// username and password will be given via UserInfo interface.
			session.setUserInfo(cred);
			session.connect();

			// exec 'scp -f rfile' remotely
			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

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
					throw new RuntimeException("Exit value of command ["+command+"]  is "+exitvalue+"\noutput was:"+result.toString());
			}
			//channel.disconnect();
			session.disconnect();
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
			if (level==DEBUG) return logger.isDebugEnabled();
			if (level==INFO)  return logger.isInfoEnabled();
			if (level==WARN)  return logger.isWarnEnabled();
			if (level==ERROR) return logger.isErrorEnabled();
			if (level==FATAL) return true;
			return false;
		}
		public void log(int level, String message){
			if (level==DEBUG) logger.debug(message);
			if (level==INFO)  logger.info(message);
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

		public void showMessage(String message){ logger.info("Message: {}",message); }
		public boolean promptYesNo(String str){ logger.info("YesOrNo: {}",str); return true;  }

		public boolean promptPassphrase(String message){ logger.info("prompt Passphrase: {}",message); return true; }
		public String getPassphrase(){ return ""; }

		public boolean promptPassword(String message) { logger.info("prompt Password: {}",message); return true; }
		public String getPassword(){ logger.info("using Password: {}",password); return password; }

		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo) {
			logger.info("destination: {}",destination); 
			logger.info("name: {}",name);
			logger.info("instruction: {}",instruction);
			for (String s: prompt)
				logger.info("prompt[i]: {}",s);
			for (boolean b: echo)
				logger.info("echo[i]: {}",b); 
			return null;
		}
	}
}
