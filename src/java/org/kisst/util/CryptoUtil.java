package org.kisst.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;

public class CryptoUtil {
	private static final String xform = "DES/ECB/PKCS5Padding";
	private static final SecretKey key=createKey();
	private static byte[] iv =
	{ 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };

	public static byte[] encrypt(byte[] inpBytes) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(xform);
			IvParameterSpec ips = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, ips);
			return cipher.doFinal(inpBytes);
		}
		catch (NoSuchAlgorithmException e) {throw new RuntimeException(e);}
		catch (NoSuchPaddingException e) {throw new RuntimeException(e);}
		catch (InvalidKeyException e) {throw new RuntimeException(e);}
		catch (InvalidAlgorithmParameterException e) {throw new RuntimeException(e);}
		catch (IllegalBlockSizeException e) {throw new RuntimeException(e);}
		catch (BadPaddingException e) {throw new RuntimeException(e);}
	}

	public static byte[] decrypt(byte[] inpBytes) {
		try {
			Cipher cipher = Cipher.getInstance(xform);
			IvParameterSpec ips = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ips);
			return cipher.doFinal(inpBytes);
		}
		catch (NoSuchAlgorithmException e) {throw new RuntimeException(e);}
		catch (NoSuchPaddingException e) {throw new RuntimeException(e);}
		catch (InvalidKeyException e) {throw new RuntimeException(e);}
		catch (InvalidAlgorithmParameterException e) {throw new RuntimeException(e);}
		catch (IllegalBlockSizeException e) {throw new RuntimeException(e);}
		catch (BadPaddingException e) {throw new RuntimeException(e);}
	}

	public static SecretKey  createKey() {
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("DES");
			kg.init(56); // 56 is the keysize. Fixed for DES
			return kg.generateKey();
		} 
		catch (NoSuchAlgorithmException e) { throw new RuntimeException(e);}

	}
}


