package org.kisst.gft.filetransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class Ssh {
	public static void main(String[] arg){
		if(arg.length!=2){
			System.err.println("usage: java FileTransfer user@remotehost cmd");
			System.exit(-1);
		}      
		String user=arg[0].substring(0, arg[0].indexOf('@'));
		arg[0]=arg[0].substring(arg[0].indexOf('@')+1);
		String host=arg[0];
		String command=arg[1];
		Credentials cred=new Credentials(user, "/home/mark/.ssh/id_rsa");
		ssh(cred, host, command);
	}

	public static String ssh(Credentials cred, String host, String command) {
		FileOutputStream fos=null;
		try{
			JSch jsch=new JSch();
			jsch.addIdentity(cred.keyfile);
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
					throw new RuntimeException("Exit value of command ["+command+"]  is "+exitvalue);
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


	public static class Credentials implements UserInfo , UIKeyboardInteractive{
		public final String user;
		public final String keyfile;

		public Credentials(String user, String keyfile) {
			this.user=user;
			this.keyfile=keyfile;
			File f=new File(keyfile);
			if (! f.exists())
				throw new RuntimeException("keyfile "+f+" does not exist");
			if (! f.isFile())
				throw new RuntimeException("keyfile "+f+" is not a file");
		}

		public void showMessage(String message){ System.out.println("Message: "+message); }
		public boolean promptYesNo(String str){ System.out.println("YesOrNo: "+str); return true;  }

		public boolean promptPassphrase(String message){ System.out.println("prompt Passphrase: "+message); return true; }
		public String getPassphrase(){ return ""; }

		public boolean promptPassword(String message) { System.out.println("prompt Password: "+message); return true; }
		public String getPassword(){ return ""; }

		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo) {
			System.out.println("destination: "+destination); 
			System.out.println("name : "+name);
			System.out.println("instruction : "+instruction);
			for (String s: prompt)
				System.out.println("prompt[i]: "+s);
			for (boolean b: echo)
				System.out.println("echo[i]: "+b); 
			return null;
		}
	}
}
