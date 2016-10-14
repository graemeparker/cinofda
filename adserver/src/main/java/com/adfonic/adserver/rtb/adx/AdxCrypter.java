package com.adfonic.adserver.rtb.adx;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import com.adfonic.util.Base64;
import com.google.doubleclick.crypto.DoubleClickCrypto;

public class AdxCrypter {

    // We encrypt do not use time based init vector - just zeroes 
    private static final byte[] zeroinitv = new byte[DoubleClickCrypto.INITV_SIZE];

    private final DoubleClickCrypto.Price price;

    private final DoubleClickCrypto.AdId adid;

    private final DoubleClickCrypto.Idfa idfa;

    private final DoubleClickCrypto.Hyperlocal hyperlocal;

    public AdxCrypter(byte[] encKeyBytes, byte[] integKeyBytes) {
        if (encKeyBytes.length != 32 || integKeyBytes.length != 32) {
            throw new RuntimeException("encryption-key and integrity-key should both be 32 bytes long!");
        }
        SecretKeySpec encryptionKey = new SecretKeySpec(encKeyBytes, DoubleClickCrypto.KEY_ALGORITHM);
        SecretKeySpec integrityKey = new SecretKeySpec(integKeyBytes, DoubleClickCrypto.KEY_ALGORITHM);

        DoubleClickCrypto.Keys keys;
        try {
            keys = new DoubleClickCrypto.Keys(encryptionKey, integrityKey);
        } catch (InvalidKeyException ikx) {
            throw new IllegalStateException("Invalid AdX crypto keys", ikx);
        }
        this.price = new DoubleClickCrypto.Price(keys);
        this.adid = new DoubleClickCrypto.AdId(keys);
        this.idfa = new DoubleClickCrypto.Idfa(keys);
        this.hyperlocal = new DoubleClickCrypto.Hyperlocal(keys);
    }

    public AdxCrypter(String b64encryptionKey, String b64integrityKey) {
        this(Base64.decode(b64encryptionKey), Base64.decode(b64integrityKey));
    }

    public byte[] encryptPrice(long priceValue) {
        return price.encryptPriceMicros(priceValue, zeroinitv);
    }

    public long decryptPrice(byte[] priceCipher) throws SignatureException {
        return price.decryptPriceMicros(priceCipher);
    }

    public byte[] encryptAdvertisingId(String adidPlain) {
        UUID uuid = UUID.fromString(adidPlain);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return adid.encryptAdId(bb.array(), zeroinitv);
    }

    public byte[] encryptAdvertisingId(byte[] adidPlain) {
        return adid.encryptAdId(adidPlain, zeroinitv);
    }

    public String decryptAdvertisingId(byte[] adidCipher) throws SignatureException {
        ByteBuffer bb = ByteBuffer.wrap(idfa.decryptIdfa(adidCipher));
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }

    public byte[] encryptIdfa(String idfaPlain) {
        UUID uuid = UUID.fromString(idfaPlain);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return idfa.encryptIdfa(bb.array(), zeroinitv);
    }

    public byte[] encryptIdfa(byte[] idfaPlain) {
        return idfa.encryptIdfa(idfaPlain, zeroinitv);
    }

    public String decryptIdfa(byte[] idfaCipher) throws SignatureException {
        ByteBuffer bb = ByteBuffer.wrap(idfa.decryptIdfa(idfaCipher));
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString().toUpperCase(); // IDFA is upper case
    }

}
