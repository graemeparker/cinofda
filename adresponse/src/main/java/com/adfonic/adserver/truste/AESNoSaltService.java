package com.adfonic.adserver.truste;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESNoSaltService {

    private final static String CIPHER_ID = "AES/ECB/PKCS5Padding";
    private final static String ENCODING = "UTF-8";
    private final SecretKey key;

    public AESNoSaltService(final String password) {
        this.key = makeKey(password);
    }

    public String encrypt(String inputText) {
        try {
            final byte[] ciphertext = cipher(Cipher.ENCRYPT_MODE).doFinal(inputText.getBytes(ENCODING));
            return Base64.encodeBase64URLSafeString(ciphertext);
        } catch (Exception ex) {
            throw new AESNoSaltServiceException(ex);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            final byte[] ciphertext = Base64.decodeBase64(encryptedText);
            return new String(cipher(Cipher.DECRYPT_MODE).doFinal(ciphertext), ENCODING);
        } catch (Exception ex) {
            throw new AESNoSaltServiceException(ex);
        }
    }

    private Cipher cipher(int mode) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ID);
            cipher.init(mode, key);
            return cipher;
        } catch (Exception ex) {
            throw new AESNoSaltServiceException(ex);
        }
    }

    private static SecretKey makeKey(String passwordString) {
        try {
            final MessageDigest format = MessageDigest.getInstance("MD5");
            format.update(passwordString.getBytes(Charset.forName(ENCODING)));
            final BigInteger hash = new BigInteger(1, format.digest());
            final String hashedPassword = hash.toString(16);
            final byte[] key = hashedPassword.getBytes(Charset.forName(ENCODING));
            final SecretKey secret = new SecretKeySpec(key, "AES");
            return secret;
        } catch (Exception ex) {
            throw new AESNoSaltServiceException(ex);
        }
    }
}
