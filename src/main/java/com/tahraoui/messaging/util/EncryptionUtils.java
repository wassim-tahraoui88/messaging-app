package com.tahraoui.messaging.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Random;

public class EncryptionUtils {

	private static final String ALGORITHM = "AES";

	public static String generateCode() {
		return new Random().ints(65,90).limit(10)
				.collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();
	}

	public static SecretKey generateSecretKey() throws Exception {
		var keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		return keyGenerator.generateKey();
	}
	public static SecretKey getSecretKeyFromBytes(byte[] bytes) { return new SecretKeySpec(bytes, ALGORITHM); }

	public static String encrypt(String text, SecretKey secretKey) throws Exception {
		var cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(text.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}
	public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
		return new String(cipher.doFinal(decodedBytes));
	}


}
