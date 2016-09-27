package chat_caio;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

public class Constantes {

	public Constantes() {
		// TODO Auto-generated constructor stub
	}
	
	public static SecretKey getKey() throws Exception{
		DESKeySpec keySpec = new DESKeySpec(CHAVE_CRIPTOGRAFIA.getBytes("UTF8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		return key;
	}
	
	public static String CHAVE_CRIPTOGRAFIA = "l@e%c%o6mM0D&*E@@l)3+r=";
}
