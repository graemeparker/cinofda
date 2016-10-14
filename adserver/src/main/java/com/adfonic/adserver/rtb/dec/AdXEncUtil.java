package com.adfonic.adserver.rtb.dec;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import com.adfonic.adserver.rtb.PriceDecrypter;
import com.adfonic.adserver.rtb.adx.AdxCrypter;
import com.adfonic.domain.DeviceIdentifierType;

/**
 * AdX decryption code for settlement price
 * Based on sample code from google released under apache v2 license. 
 * We don't have custom changes and can more or less use the same thing. 
 * 
 * Basically what it does is this
 *  - first convert websafe unpadded to standard base64
 *  - decode to (iv[16], p[8]], sig[4])
 *  -- estimate price in micros
 *  price_pad = hmac(e_key, iv)
 *  price = p <xor> price_pad
 *  
 *  -- also verify
 *  conf_sig = hmac(i_key, price || iv)
 *  success = (conf_sig == sig)
 *  
 *  e_key and i_key provided by google
 */
public class AdXEncUtil implements PriceDecrypter {

    private static final long serialVersionUID = 1L;

    private static final transient Logger LOG = Logger.getLogger(AdXEncUtil.class.getName());

    private static final String HMAC_ALG = "HmacSHA1";

    /** The length of the initialization vector */
    private static final int INITIALIZATION_VECTOR_SIZE = 16;
    /** The length of the ciphertext */
    private static final int CIPHERTEXT_SIZE = 8;
    /** The length of the signature */
    private static final int SIGNATURE_SIZE = 4;

    private final SecretKey encryptionKey;
    private final SecretKey integrityKey;

    private final AdxCrypter crypter;

    public AdXEncUtil(String b64encryptionKey, String b64integrityKey) {
        byte[] encKeyBytes = Base64.decodeBase64(b64encryptionKey);
        byte[] integKeyBytes = Base64.decodeBase64(b64integrityKey);
        if (encKeyBytes.length != 32 || integKeyBytes.length != 32) {
            throw new RuntimeException("encryption-key and integrity-key should both be 32 bytes long!");
        }
        this.encryptionKey = new SecretKeySpec(encKeyBytes, HMAC_ALG);
        this.integrityKey = new SecretKeySpec(integKeyBytes, HMAC_ALG);
        this.crypter = new AdxCrypter(b64encryptionKey, b64integrityKey);
    }

    public AdxCrypter getCrypter() {
        return crypter;
    }

    /**
     * An Exception thrown by Decrypter if the ciphertext cannot successfully be decrypted.
     */
    private static class DecrypterException extends Exception {
        public DecrypterException(String message) {
            super(message);
        }
    }

    /**
     * Converts from unpadded web safe base 64 encoding (RFC 3548) to standard base 64 encoding (RFC 2045) and pads the result.
     */
    private static String unWebSafeAndPad(String webSafe) {
        String pad = "";
        if ((webSafe.length() % 4) == 2) {
            pad = "==";
        } else if ((webSafe.length() % 4) == 1) {
            pad = "=";
        }
        return webSafe.replace('-', '+').replace('_', '/') + pad;
    }

    /**
     * Performs the decryption algorithm.
     */
    private static byte[] decrypt(byte[] initializationVector, byte[] ciphertext, byte[] signature, SecretKey encryptionKey, SecretKey integrityKey) throws DecrypterException {
        byte[] plaintext = new byte[ciphertext.length];
        try {
            // Decrypt the value
            Mac encryptionHmac = Mac.getInstance(HMAC_ALG);
            encryptionHmac.init(encryptionKey);
            byte[] encryptionPad = encryptionHmac.doFinal(initializationVector);
            for (int i = 0; i < plaintext.length; i++) {
                plaintext[i] = (byte) (ciphertext[i] ^ encryptionPad[i]);
            }

            // Compute the signature
            Mac integrityHmac = Mac.getInstance(HMAC_ALG);
            integrityHmac.init(integrityKey);
            integrityHmac.update(plaintext);
            integrityHmac.update(initializationVector);
            byte[] expectedSignature = new byte[SIGNATURE_SIZE];
            System.arraycopy(integrityHmac.doFinal(), 0, expectedSignature, 0, expectedSignature.length);
            if (!Arrays.equals(signature, expectedSignature)) {
                throw new DecrypterException("Signature mismatch " + Hex.encodeHexString(signature) + " expected " + Hex.encodeHexString(expectedSignature) + " plaintext: "
                        + Hex.encodeHexString(plaintext));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("HmacSHA1 not supported.", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Key is invalid for this purpose.", e);
        }
        return plaintext;
    }

    /**
     * Parses the timestamp out of the initialization vector. Note: this method loses precision. java.util.Date only holds the date to millisecond precision while the initialization vector contains a timestamp with microsecond precision.
     */
    private static Date getTimeFromInitializationVector(byte[] initializationVector) {
        ByteBuffer buffer = ByteBuffer.wrap(initializationVector);
        long seconds = buffer.getInt();
        long micros = buffer.getInt();
        return new Date((seconds * 1000) + (micros / 1000));
    }

    @Override
    public String encodePrice(BigDecimal price) {
        return Base64.encodeBase64URLSafeString(crypter.encryptPrice(price.movePointRight(3).longValue()));
    }

    @Override
    public BigDecimal decodePrice(String websafeUnPaddedMessage) {
        String b64EncodedCiphertext = unWebSafeAndPad(websafeUnPaddedMessage);
        DataInputStream dis = decryptedStream(Base64.decodeBase64(b64EncodedCiphertext), CIPHERTEXT_SIZE);

        long value;
        try {
            value = dis.readLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO - to detect stale response attacks, reject the ones that is farther from the current time after
        //   considering timezone differences and buffer. For now comment out for performance issues
        //Date timestamp = getTimeFromInitializationVector(initializationVector);

        /* AdX spec - The encoded impression cost (that is, CPI rather than CPM) in micros of the account currency. 
         * For example, a winning CPM of $5 USD corresponds to 5,000,000 micros CPM, or 5,000 micros CPI. 
         * The decoded value of WINNING_PRICE in this case would be 5,000 */
        return BigDecimal.valueOf(value).movePointLeft(3);
    }

    private DataInputStream decryptedStream(byte[] codeString, int cipherTextSize) {
        return new DataInputStream(new ByteArrayInputStream(decrypt(codeString, cipherTextSize)));
    }

    public byte[] decrypt(byte[] codeString, int cipherTextSize) {
        byte[] initializationVector = new byte[INITIALIZATION_VECTOR_SIZE];
        System.arraycopy(codeString, 0, initializationVector, 0, initializationVector.length);
        byte[] ciphertext = new byte[cipherTextSize];
        System.arraycopy(codeString, initializationVector.length, ciphertext, 0, ciphertext.length);
        byte[] signature = new byte[SIGNATURE_SIZE];
        System.arraycopy(codeString, initializationVector.length + ciphertext.length, signature, 0, signature.length);

        try {
            return decrypt(initializationVector, ciphertext, signature, encryptionKey, integrityKey);
        } catch (DecrypterException e) {
            throw new RuntimeException("Failed to decode ciphertext. " + e.getMessage());
        }
    }

    private String getIdfa(byte[] codeString) {
        try {
            //return Appl3Util.getAsAdvertisingIdentifier(decryptedStream(codeString, 16));
            // DB allows ^[0-9A-Fa-f]{40}$. Not checked in code though, not rtb route. So for now get normal form
            return getAsAdvertisingIdentifier(decryptedStream(codeString, 16));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String decodeAs(String encodedDeviceId, String deviceIdentifierType) {
        if (DeviceIdentifierType.SYSTEM_NAME_IFA.equals(deviceIdentifierType) || DeviceIdentifierType.SYSTEM_NAME_ADID.equals(deviceIdentifierType)
                || DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5.equals(deviceIdentifierType)) {
            try {
                return getIdfa(Hex.decodeHex(encodedDeviceId.toCharArray()));
            } catch (DecoderException e) {
                throw new RuntimeException(e);
            }
        }

        throw new UnsupportedOperationException("No support decoding to type: " + deviceIdentifierType);
    }

    public String decodeDeviceId(byte[] bytes) {
        try {
            DataInputStream stream = decryptedStream(bytes, 16);
            UUID uuid = new UUID(stream.readLong(), stream.readLong());
            return uuid.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAsAdvertisingIdentifier(InputStream input) throws IOException {
        byte[] b = new byte[16];
        String advertiserId = "";
        if (input.read(b) != -1) {
            advertiserId = new String(Hex.encodeHex(b, false));
        }
        return advertiserId;
    }

}
