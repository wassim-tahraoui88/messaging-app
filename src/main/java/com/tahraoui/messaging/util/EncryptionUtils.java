package com.tahraoui.messaging.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class EncryptionUtils {

	private static final String KEY_ALGORITHM = "AES";
	private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final String KEY_SHARING_ALGORITHM = "RSA";

	private static final int KEY_BIT_LENGTH = 256;
	private static final int KEY_PAIR_BIT_LENGTH = 2024;

	private static final int IV_BYTES = 16;

	public static SecretKey generateKey() throws GeneralSecurityException {
		var keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
		keyGenerator.init(KEY_BIT_LENGTH);
		return keyGenerator.generateKey();
	}
	public static byte[] encrypt(String message, SecretKey key, IvParameterSpec iv) throws GeneralSecurityException {
		var cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		return cipher.doFinal(message.getBytes());
	}
	public static String decrypt(byte[] encrypted, SecretKey key, IvParameterSpec iv) throws GeneralSecurityException {
		var cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		return new String(cipher.doFinal(encrypted));
	}

	public static KeyPair generateKeyPair() throws GeneralSecurityException {
		var keyPairGenerator = KeyPairGenerator.getInstance(KEY_SHARING_ALGORITHM);
		keyPairGenerator.initialize(KEY_PAIR_BIT_LENGTH);
		return keyPairGenerator.generateKeyPair();
	}
	public static byte[] encryptRSA(byte[] message, PublicKey key) throws GeneralSecurityException {
		var cipher = Cipher.getInstance(KEY_SHARING_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(message);
	}
	public static SecretKey decryptRSA(byte[] encrypted, PrivateKey key) throws GeneralSecurityException {
		var cipher = Cipher.getInstance(KEY_SHARING_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		var decryptedAESKey = cipher.doFinal(encrypted);
		return new SecretKeySpec(decryptedAESKey, 0, decryptedAESKey.length, KEY_ALGORITHM);
	}

	public static IvParameterSpec generateIV() {
		var iv = new byte[IV_BYTES];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}
}
