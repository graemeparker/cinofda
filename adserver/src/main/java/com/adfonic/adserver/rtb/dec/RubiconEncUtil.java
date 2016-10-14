package com.adfonic.adserver.rtb.dec;

import java.math.BigDecimal;
import java.text.NumberFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.adfonic.adserver.rtb.PriceDecrypter;

public class RubiconEncUtil implements PriceDecrypter {

    private static final long serialVersionUID = 1L;

    private static final String HEXES = "0123456789ABCDEF";

    private final char[] password;

    private static final String HEX_PFX = "0x";

    // given password is dealt as a character array, we are not doing a bin enc
    RubiconEncUtil(String encryptionKey) {
        password = encryptionKey.toCharArray();
    }

    @Override
    public BigDecimal decodePrice(String priceHexStr) {
        return new BigDecimal(decrypt(priceHexStr));
    }

    @Override
    public String encodePrice(BigDecimal price) {
        // Used Cipher below is 'NoPadding', which means that input length must be multiple of 8 
        // That's why format 00.00000 is used for price before encryption
        // Rubicon is sending prices as 16 hex character long strings 4.5 <-> 04.50000 <-> CE08DDACE95D82B8
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(5);
        format.setMaximumFractionDigits(5);
        format.setMinimumIntegerDigits(2);
        format.setMaximumIntegerDigits(2);
        return encrypt(format.format(price));
    }

    public String encrypt(String plainText) {
        try {
            return getHex(encrypt(password, plainText));
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: [" + plainText + "]", e);
        }
    }

    public String decrypt(String cipherHex) {
        try {
            String cipherHexNoPfx = cipherHex.startsWith(HEX_PFX) ? cipherHex.substring(HEX_PFX.length()) : cipherHex;
            return decrypt(password, dehex(cipherHexNoPfx));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: [" + cipherHex + "]", e);
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
