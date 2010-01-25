package org.kisst.gft.tasks;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.kisst.gft.task.TaskHandler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class FileTransfer implements TaskHandler {
	public Object execute(Object data) {
		System.out.println(data);
		return null;
	}


  public static void main(String[] arg){
	    if(arg.length!=2){
	      System.err.println("usage: java FileTransfer user@remotehost cmd");
	      System.exit(-1);
	    }      

	    FileOutputStream fos=null;
	    try{

	      String user=arg[0].substring(0, arg[0].indexOf('@'));
	      arg[0]=arg[0].substring(arg[0].indexOf('@')+1);
	      String host=arg[0];

	      JSch jsch=new JSch();
	      jsch.addIdentity("/home/mark/.ssh/id_rsa");
	      Session session=jsch.getSession(user, host, 22);

	      // username and password will be given via UserInfo interface.
	      session.setUserInfo(new MyUserInfo());
	      session.connect();

	      // exec 'scp -f rfile' remotely
	      String command=arg[1];
	      Channel channel=session.openChannel("exec");
	      ((ChannelExec)channel).setCommand(command);

	      //channel.setInputStream(System.in);
	      channel.setInputStream(null);
	      //channel.setOutputStream(System.out, true);	      
	      //((ChannelExec)channel).setErrStream(System.err, true);
	      InputStream in=channel.getInputStream();

	      channel.connect();

	      byte[] tmp=new byte[1024];
	      while (true) {
	    	  while(true){
	    		  int i=in.read(tmp, 0, 1024);
	    		  if(i<0)break;
	    		  System.out.print(new String(tmp, 0, i));
	    	  }
	    	  if(channel.isClosed()){
	    		  System.out.println("exit-status: "+channel.getExitStatus());
	    		  break;
	    	  }
	    	  try{Thread.sleep(1000);}catch(Exception ee){}
	      }
	      //channel.disconnect();
	      session.disconnect();
	    }
	    catch(Exception e){
	      System.out.println(e);
	      try{if(fos!=null)fos.close();}catch(Exception ee){}
	    }
	  }


	  public static class MyUserInfo implements UserInfo , UIKeyboardInteractive{
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
