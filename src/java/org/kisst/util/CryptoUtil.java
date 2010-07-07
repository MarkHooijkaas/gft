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
	
	// The following method is public, so that any program using this library can set it's
	// own key (preferably one that isn't publicly available worldwide through github :-) 
	public static void setKey(String keystring) { key=calcKey(keystring.getBytes()); }
	private static Key calcKey(byte[] keyValue) {
		// Make sure the key is 128-bits, if it is too long an Exception will be thrown
		byte[] bytes= new byte[16];
		for (int i=0; i<keyValue.length; i++)
			bytes[i%16] += keyValue[i];
		return new SecretKeySpec(bytes, ALGORITHM);
	}
	
	public static String encrypt(String valueToEnc) {
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] encValue = c.doFinal(valueToEnc.getBytes());
			return toHex(encValue);
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
			byte[] decodedValue = fromHex(encryptedValue);
			byte[] decValue = c.doFinal(decodedValue);
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


