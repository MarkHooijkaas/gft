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
	public static class ExecResult {
		public final int exitcode;
		public final String stdout;
		public final String stderr;
		public ExecResult(int exitcode, String stdout, String stderr) {
			this.exitcode=exitcode;
			this.stdout=stdout;
			this.stderr=stderr;
		}
	}
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
		ExecResult result=exec(host, cred, command);
		if (result.exitcode!=0)
			throw new ExitCodeException(host, command, result.exitcode, result.stdout+result.stderr);
		return result.stdout+result.stderr;
	}
	
	public static ExecResult exec(SshHost host, Ssh.Credentials cred, String command) {
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

			int count=0;
			while (! channel.isClosed()) {
				if (count>=50)
					throw new RuntimeException("SSH Channel was not closed after "+count+" waiting attempts");
				logger.info("Sleeping some time because channel is not yet closed, attempt "+count++);
				try{Thread.sleep(200);}catch(Exception ee){}
			}

			int exitvalue = channel.getExitStatus();

			//channel.disconnect();
			session.disconnect();
			if (logger.isWarnEnabled()){
				if (exitvalue!=0 )
					logger.warn("Call to {} returned exitvalue "+exitvalue, host);
				if (err.size()>0)
					logger.warn("Call to {} returned stderr {}", host, err.toString());
				if (logger.isInfoEnabled())
					logger.info("Call to {} returned stdout [{}]", host, result);
			}
			return new ExecResult(exitvalue, result.toString(), err.toString());
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

	
	public static Session openSession(SshHost host, Ssh.Credentials cred) {
		logger.info("creating sftp connection to {} ", host);
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
			return session;
		}
		catch(JSchException e) { throw new RuntimeException(e); }
	}
	
	public static void closeChannel(Channel channel) {
		int count=0;
		while (! channel.isClosed()) {
			if (count>=50)
				throw new RuntimeException("SSH Channel was not closed after "+count+" waiting attempts");
			logger.info("Sleeping some time because channel is not yet closed, attempt "+count++);
			try{Thread.sleep(200);}catch(Exception ee){}
		}
		//channel.disconnect();
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
			// Dirty hack to prevent all the Warnings in the log
			if (level==WARN && message.trim().startsWith("Permanently added") && message.trim().endsWith("to the list of known hosts."))
				level=DEBUG;
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
