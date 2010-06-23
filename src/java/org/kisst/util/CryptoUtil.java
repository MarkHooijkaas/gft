package org.kisst.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
	private static final String xform = "DES/ECB/PKCS5Padding";
	private static SecretKey key=createKey("F243E43987CA3293");
	private static byte[] iv =
	{ 0x0a, 0x01, 0x02, 0x03, 0x04, 0x0b, 0x0c, 0x0d };

	private static SecretKey createKey(String key) { return new SecretKeySpec(fromHex(key), "DES"); }
	public static void setKey(String keystring) { key=createKey(keystring); }

	public static String encrypt(String text) { return toHex(text.getBytes()); }
	public static String decrypt(String hex) { return new String(fromHex(hex)); }

	/*
	public static byte[] encrypt(byte[] inpBytes) {
		Cipher cipher;
		try {
			//cipher = Cipher.getInstance(xform);
			cipher = Cipher.getInstance("PBEWithMD5AndDES");
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
*/
	
	static final String HEXES = "0123456789ABCDEF";
	public static byte[] fromHex( String hex) {
		byte[] result=new byte[hex.length()/2];
		for ( int i=0; i<hex.length(); i+=2) {
			byte b=(byte) (HEXES.indexOf(""+hex.charAt(i))*16);
			b +=(byte) HEXES.indexOf(""+hex.charAt(i+1));
			result[i/2]=b;
		}
		return result;
	}
	public static String toHex( byte [] raw ) {
		if ( raw == null ) {
			return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * raw.length );
		for ( final byte b : raw ) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4))
			.append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

}


