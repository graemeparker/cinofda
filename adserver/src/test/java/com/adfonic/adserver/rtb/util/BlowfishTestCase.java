package com.adfonic.adserver.rtb.util;

/*
 * AI-152 - *decryption reference*
 * 
 * This is the example java implementation taken as such from https://tickets.adfonic.com/browse/AI-152
 * 
 * do not modify this - not even make it a junit test
 * 
 * Basically it looks like Blowfish/ECB/NoPadding, but with no additional context information 
 * given, fully reuse the implementation
 * 
 */

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption/decryption of RTB bid price.
 *
 * Given a price of 06.93308 and a password of "rubicon", we produce A834BDD6C3478B8C
 *
 * Inbound price is guaranteed to be on an 8 byte boundary, so no padding is necessary.
 *
 */
public class BlowfishTestCase {
    private static final String HEXES = "0123456789ABCDEF";

    public static void main(String[] args) throws Exception {
        char[] password = new char[] { 'r', 'u', 'b', 'i', 'c', 'o', 'n' };
        String[] samplePrices = new String[] { "06.93308", "01.34821", "12.31345", "00.23913", "102.9000", "149.1341" };
        for (String price : samplePrices) {
            String encryptedHexValue = getHex(encrypt(password, price));
            decrypt(password, dehex(encryptedHexValue));
            //System.out.println(decrypted + " => 0x" + encryptedHexValue);
        }

    }

    private static byte[] encrypt(char[] password, String plaintext) throws Exception {
        byte[] bytes = new byte[password.length];
        for (int i = 0; i < password.length; ++i) {
            bytes[i] = (byte) password[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(bytes, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return encrypted;
    }

    private static String decrypt(char[] password, byte[] ciphertext) throws Exception {
        byte[] bytes = new byte[password.length];
        for (int i = 0; i < password.length; ++i) {
            bytes[i] = (byte) password[i];
        }
        SecretKeySpec skeySpec = new SecretKeySpec(bytes, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(ciphertext);
        return new String(encrypted);
    }

    private static byte[] dehex(String hex) {
        byte[] bits = new byte[hex.length() / 2];
        for (int i = 0; i < bits.length; i++) {
            bits[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bits;
    }

    private static String getHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}