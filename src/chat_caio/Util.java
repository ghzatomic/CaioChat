package chat_caio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class Util {

	private static String key = Constantes.CHAVE_CRIPTOGRAFIA;
	
	public static byte[] encode(String enc) {
		enc = xorMessage(enc, key);
		return enc.getBytes();
	}

	public static String decode(byte[] enc) {
		return xorMessage(new String(enc), key);
	}

	public static byte[] getData(InputStream in) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		try {
			int n = in.read(data);
			if( n < 0 ) return null;
			baos.write(data,0,n);
			//in.read(data);
		} catch (IOException e) {
			return null;
		}
		return baos.toByteArray();
	}

	public static String xorMessage(String message, String key) {
		try {
			if (message == null || key == null)
				return null;

			char[] keys = key.toCharArray();
			char[] mesg = message.toCharArray();

			int ml = mesg.length;
			int kl = keys.length;
			char[] newmsg = new char[ml];

			for (int i = 0; i < ml; i++) {
				newmsg[i] = (char) (mesg[i] ^ keys[i % kl]);
			}// for i
			mesg = null;
			keys = null;
			return new String(newmsg);
		} catch (Exception e) {
			return null;
		}
	}// xorMessage
	
}
