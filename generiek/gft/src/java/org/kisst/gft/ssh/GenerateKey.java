package org.kisst.gft.ssh;

import java.io.File;
import java.io.IOException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;


public class GenerateKey {
	public static void generateKey(String fname) {
		String pubfname=fname+".pub";
		if (new File(fname).exists())
			throw new RuntimeException("Private keyfile "+fname+" already exists, will not generate new keypair");
		if (new File(pubfname).exists())
			throw new RuntimeException("Public keyfile "+pubfname+" already exists, will not generate new keypair");
		
		JSch jsch=new JSch();
		try {
			KeyPair kp=KeyPair.genKeyPair(jsch, KeyPair.DSA, 1024);
			kp.writePrivateKey(fname);
			kp.writePublicKey(pubfname, "GFT");
		}
		catch (JSchException e) { throw new RuntimeException(e);} 
		catch (IOException e) {  throw new RuntimeException(e);}
	}
}
