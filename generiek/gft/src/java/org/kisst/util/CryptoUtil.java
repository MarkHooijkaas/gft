package org.kisst.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {
	private static final String ALGORITHM = "AES";
	private static Key key= calcKey("jF0OQtZ4PYlEzEyZCchJdIq22GUuV6U9LoLZYqRt".getBytes());
	
	public static void setKey(String keystring) { key=calcKey(keystring.getBytes()); }
	private static Key calcKey(byte[] keyValue) { return new SecretKeySpec(keyValue, ALGORITHM); }
	
	public static String encrypt(String valueToEnc) {
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encValue = c.doFinal(valueToEnc.getBytes());
			String encryptedValue = toHex(encValue);
			return encryptedValue;
		}
		catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
		catch (NoSuchPaddingException e) { throw new RuntimeException(e); } 
		catch (InvalidKeyException e) { throw new RuntimeException(e); }
		catch (IllegalBlockSizeException e) { throw new RuntimeException(e); }
		catch (BadPaddingException e) { throw new RuntimeException(e); }
	}

	public static String decrypt(String encryptedValue) {
		try {
			Cipher c;
			c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decordedValue = fromHex(encryptedValue);
			byte[] decValue = c.doFinal(decordedValue);
			String decryptedValue = new String(decValue);
			return decryptedValue;
		}
		catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
		catch (NoSuchPaddingException e) { throw new RuntimeException(e); } 
		catch (InvalidKeyException e) { throw new RuntimeException(e); }
		catch (IllegalBlockSizeException e) { throw new RuntimeException(e); }
		catch (BadPaddingException e) { throw new RuntimeException(e); }
	}


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


