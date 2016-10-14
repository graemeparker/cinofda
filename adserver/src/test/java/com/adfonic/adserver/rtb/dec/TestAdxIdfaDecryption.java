package com.adfonic.adserver.rtb.dec;

import java.security.InvalidKeyException;
import java.security.SignatureException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.rtb.adx.AdxCrypter;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.util.Base64;

/*
 *     Tests based on examples given at https://support.google.com/adxbuyer/answer/3221407
 *     
 *     enc key - 00 01 02 03  04 05 06 07  08 09 0A 0B  0C 0D 0E 0F  10 11 12 13  14 15 16 17  18 19 1A 1B  1C 1D 1E 1F
 *     int key - 1F 1E 1D 1C  1B 1A 19 18  17 16 15 14  13 12 11 10  0F 0E 0D 0C  0B 0A 09 08  07 06 05 04  03 02 01 00
 *     unhashed ifa - 5AAB8704-4D8C-4879-8993-C6DD8361F88F
 *     encrypted ifa - 51 92 8A 66  00 00 00 00  AA AA AA CE  AA AA AA CE  D2 C3 2D F1  6B 16 58 B8  44 75 91 BE  4B A8 53 92  2C 55 AB AD
 *     md5 hashed ifa - 3B 66 71 54 FF A2 BC 3A DF 2F 41 13 63 44 AA EE
 *     encrypted hashd ifa - 51 92 8A 66  00 00 00 00  AA AA AA CE  AA AA AA CE  B3 0E DB A1  D9 38 AC FB  12 C9 16 70  AB 8D 01 F3  1E 43 45 57
 */
public class TestAdxIdfaDecryption {

    // These tests are using the PriceDecrypter methods.
    private AdXEncUtil adxEncUtil;
    AdxCrypter tool;
    private String encHx = "000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F";
    private String intHx = "1F1E1D1C1B1A191817161514131211100F0E0D0C0B0A09080706050403020100";

    @Before
    public void setUp() throws DecoderException, InvalidKeyException {
        adxEncUtil = new AdXEncUtil(Base64.encode(Hex.decodeHex(encHx.toCharArray())), Base64.encode(Hex.decodeHex(intHx.toCharArray())));
        tool = new AdxCrypter(Hex.decodeHex(encHx.toCharArray()), Hex.decodeHex(intHx.toCharArray()));
    }

    @Test
    public void decryptIfa() throws DecoderException, SignatureException {
        // IFA or IDFA e.g. encrypted_hashed_idfa
        String ifa = "5AAB8704-4D8C-4879-8993-C6DD8361F88F";
        byte[] encryptIdfa = tool.encryptIdfa(ifa);
        //String decryptIdfa = x.decryptAdvertisingId(encryptIdfa);
        String decrypted = adxEncUtil.decodeDeviceId(encryptIdfa);
        Assert.assertEquals(ifa, decrypted.toUpperCase());
    }

    @Test
    public void decryptIdfaMd5() throws DecoderException {
        // MD5-hashed IDFA
        String mifa = "3B667154FFA2BC3ADF2F41136344AAEE";
        String encrypted_mifa = "51928A6600000000AAAAAACEAAAAAACEB30EDBA1D938ACFB12C91670AB8D01F31E434557";
        String decrypted = adxEncUtil.decodeAs(encrypted_mifa, DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5);
        Assert.assertEquals(mifa, decrypted.toUpperCase().replace("-", ""));
    }

    @Test
    public void decryptAdid() throws DecoderException {
        // IFA or IDFA, e.g. encrypted_hashed_idfa (passing android if platform is android).
        String idfa = "5AAB8704-4D8C-4879-8993-C6DD8361F88F";
        byte[] encryptIdfa = tool.encryptAdvertisingId(idfa);
        //String encrypted_idfa = "51928A6600000000AAAAAACEAAAAAACED2C32DF16B1658B8447591BE4BA853922C55ABAD";
        String decrypted = adxEncUtil.decodeDeviceId(encryptIdfa);
        Assert.assertEquals(idfa, decrypted.toUpperCase());
    }
}
