package com.adfonic.adserver.rtb.dec;

import java.math.BigDecimal;
import java.nio.charset.Charset;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.openx.market.ssrtb.crypter.SsRtbCrypter;
import org.openx.market.ssrtb.crypter.SsRtbDecryptingException;

import com.adfonic.adserver.rtb.PriceDecrypter;

/**
 * OpenX RTB price decryption
 */
public class OpenXUtil implements PriceDecrypter {

    private static final long serialVersionUID = 1L;

    private static final int KEY_LENGTH_BASE64 = 44;
    private static final int KEY_LENGTH_HEX = 64;

    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final String HMAC_SHA1 = "HmacSHA1";

    private final SecretKey encryptionKey;
    private final SecretKey integrityKey;

    OpenXUtil(String encryptionKeyString, String integrityKeyString) {
        encryptionKey = getSecretKey(encryptionKeyString);
        integrityKey = getSecretKey(integrityKeyString);
    }

    /*
     * Decrypt an encrypted price string
     * @return the decrypted price in micros
     */
    long decryptPrice(String encryptedPrice) {
        try {
            return new SsRtbCrypter().decodeDecrypt(encryptedPrice, encryptionKey, integrityKey);
        } catch (SsRtbDecryptingException e) {
            throw new IllegalStateException("Failed to decodeDecrypt encryptedPrice: " + encryptedPrice, e);
        }
    }

    String encryptPrice(long price) {
        return new SsRtbCrypter().encryptEncode(price, encryptionKey, integrityKey);
    }

    @Override
    public BigDecimal decodePrice(String encodedPrice) {
        return cpiMicrosToCpmUSD(decryptPrice(encodedPrice));
    }

    @Override
    public String encodePrice(BigDecimal price) {
        return encryptPrice(price.movePointRight(3).longValue());
    }

    /*
     * Convert CPI micros to CPM USD.  $1 USD CPM = 1,000 CPI micros
     */
    static BigDecimal cpiMicrosToCpmUSD(long micros) {
        return BigDecimal.valueOf(micros).movePointLeft(3);
    }

    static SecretKey getSecretKey(String keyString) {
        byte[] keyBytes;
        switch (keyString.length()) {
        case KEY_LENGTH_BASE64:
            keyBytes = Base64.decodeBase64(keyString.getBytes(US_ASCII));
            break;
        case KEY_LENGTH_HEX:
            try {
                keyBytes = Hex.decodeHex(keyString.toCharArray());
            } catch (DecoderException e) {
                throw new IllegalStateException("Hex.decodeHex failed", e);
            }
            break;
        default:
            throw new IllegalArgumentException("Unexpected key string length: " + keyString.length());
        }
        return new SecretKeySpec(keyBytes, HMAC_SHA1);
    }

}
