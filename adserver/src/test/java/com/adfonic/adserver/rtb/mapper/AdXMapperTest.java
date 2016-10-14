package com.adfonic.adserver.rtb.mapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.AdSlot.MatchingAdData.DirectDeal;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.Device;
import com.adfonic.adserver.rtb.adx.AdX.BidRequest.Mobile;
import com.adfonic.adserver.rtb.adx.AdxCrypter;
import com.adfonic.adserver.rtb.dec.AdXEncUtil;
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydMarketPlace;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheManager;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.protobuf.ByteString;

/**
 * 
 * @author mvanek
 *
 */
public class AdXMapperTest {

    static DomainCacheManager domainCacheManager;
    static {
        domainCacheManager = Mockito.mock(DomainCacheManager.class);
        Mockito.when(domainCacheManager.getCache()).thenReturn(Mockito.mock(DomainCache.class));
    }
    private static final String b64encryptionKey = "rmJgZlXgksQGN3HWYBHLFbvygXZDP4Fv68NyS2Cepfs=";
    private static final String b64integrityKey = "IoeAFe9fZLWyJiHL2PuTvSP2KWyW1naLZZKxuwHNlfs=";
    public static final AdxCrypter adxCryptoTool = new AdxCrypter(b64encryptionKey, b64integrityKey);
    public static final AdXEncUtil adXencoder = new AdXEncUtil(AdXMapperTest.b64encryptionKey, AdXMapperTest.b64integrityKey);
    public static final AdXMapper adxMapper = new AdXMapper(adXencoder, domainCacheManager);

    /**
     * AdServers are actually only decryption device ids and prices, but for test and debugging we encrypt with zero init vector
     */
    @Test
    public void testEncryptDecrypt() throws Exception {
        String idfaOriginal = "E41CA450-3DB7-4AEB-A0DA-334AFE76C3E9";
        byte[] encryptIdfaBytes = adxCryptoTool.encryptIdfa(idfaOriginal);
        String idfaDecrypted = adxCryptoTool.decryptIdfa(encryptIdfaBytes);
        Assertions.assertThat(idfaOriginal).isEqualTo(idfaDecrypted);

        String adidOriginal = "6f15aa81-9a7c-4090-8ff9-3f9e37fcec25";
        // This protoTextAdid1 was taken from live AdX bid request and test will break if encryption keys will be updated some day 
        String protoTextAdid1 = "VM\\371t\\000\\002\\275_\\n\\026\\233\\016\\001\\016\\366\\223\\222\\256\\327\\331\\325\\326\\220\\366^>\\234\\264\\371\\224\\361\\016F\\032\\221\\327";
        String adidDecrypted1 = adxCryptoTool.decryptAdvertisingId(AdXMapper.unescapeBytes(protoTextAdid1).toByteArray());
        Assertions.assertThat(adidOriginal).isEqualTo(adidDecrypted1);
        // Redo same using our encryption
        String protoTextAdid2 = AdXMapper.escapeBytes(adxCryptoTool.encryptAdvertisingId(adidDecrypted1));
        String adidDecrypted2 = adxCryptoTool.decryptAdvertisingId(AdXMapper.unescapeBytes(protoTextAdid2).toByteArray());
        Assertions.assertThat(adidOriginal).isEqualTo(adidDecrypted2);

    }

    @Test
    public void testMultiSizeDirectDeal() throws Exception {
        AdX.BidRequest.Builder bidBuilder = AdXMapper.protoText2Builder(FileUtils.readFileToString(new File("src/test/data/adx/!multi-width-direct-deal.proto.txt")));
        BidRequest rtbRequest = bidBuilder.build();

        DirectDeal directDeal = rtbRequest.getAdslot(0).getMatchingAdData(0).getDirectDeal(0);
        //When 
        ByydRequest byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), rtbRequest, null);

        //Then

        // 320x50 should be picked from widths in request
        ByydImp byydImp = byydRequest.getImp();
        Assertions.assertThat(byydImp.getW()).isEqualTo(320);
        Assertions.assertThat(byydImp.getH()).isEqualTo(50);

        ByydMarketPlace marketPlace = byydRequest.getMarketPlace();
        List<ByydDeal> dealsList = marketPlace.getDeals();
        ByydDeal byydDeal = dealsList.get(0);

        // Direct deal values are also taken
        Assertions.assertThat(byydDeal.getId()).isEqualTo(String.valueOf(directDeal.getDirectDealId()));
        Assertions.assertThat(byydDeal.getBidFloor()).isEqualTo(BigDecimal.valueOf(directDeal.getFixedCpmMicros()).movePointLeft(6).doubleValue());
    }

    @Test
    public void testDoubledDimensions() throws Exception {
        AdX.BidRequest rtbRequest = buildAdxRequest();
        AdX.BidRequest.Builder builder = AdX.BidRequest.newBuilder(rtbRequest);
        AdX.BidRequest.AdSlot.Builder adslotBuilder = builder.getAdslotBuilder(0);

        // GIVEN
        adslotBuilder.clearWidth();
        adslotBuilder.clearHeight();

        adslotBuilder.addWidth(300);
        adslotBuilder.addHeight(50);

        // WHEN
        ByydRequest byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), builder.build(), null);
        ByydImp byydImp = byydRequest.getImp();
        // THEN
        // Only present dimensions are used
        Assertions.assertThat(byydImp.getW()).isEqualTo(300);
        Assertions.assertThat(byydImp.getH()).isEqualTo(50);

        // BUT

        // GIVEN
        // Add second set of width and height is used as it had bigged width (height is not considered)
        adslotBuilder.addWidth(320);
        adslotBuilder.addHeight(30);

        // WHEN
        byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), builder.build(), null);
        byydImp = byydRequest.getImp();

        // THEN
        // Bigger width dimension is used
        Assertions.assertThat(byydImp.getW()).isEqualTo(320);
        Assertions.assertThat(byydImp.getH()).isEqualTo(30);

    }

    @Test
    public void testInterstitialDimensions() throws Exception {

        AdX.BidRequest rtbRequest = buildAdxRequest();
        AdX.BidRequest.Builder builder = AdX.BidRequest.newBuilder(rtbRequest);
        AdX.BidRequest.AdSlot.Builder adslotBuilder = builder.getAdslotBuilder(0);

        // GIVEN
        // NON interstitial request
        builder.getMobileBuilder().setIsInterstitialRequest(false);
        // Nonstandard dimensions 
        adslotBuilder.setWidth(0, 666);
        adslotBuilder.setHeight(0, 389);
        // WHEN
        ByydRequest byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), builder.build(), null);
        ByydImp byydImp = byydRequest.getImp();
        // THEN
        // Odd dimensions stay same...
        Assertions.assertThat(byydImp.getW()).isEqualTo(666);
        Assertions.assertThat(byydImp.getH()).isEqualTo(389);

        //AND 

        //GIVEN
        //Interstitial request
        builder.getMobileBuilder().setIsInterstitialRequest(true);

        // WHEN
        byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), builder.build(), null);
        byydImp = byydRequest.getImp();

        // THEN
        // Dimensions are replaced by standard format that can fit into original dimensions
        Assertions.assertThat(byydImp.getW()).isEqualTo(480);
        Assertions.assertThat(byydImp.getH()).isEqualTo(320);

    }

    @Test
    public void testAdvertisingId_ADID() throws Exception {

        AdX.BidRequest rtbRequest = buildAdxRequest();
        // When
        ByydRequest byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), rtbRequest, null);
        // Then
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        MapEntry adidEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_ADID, "0ad70515-f0e9-442c-8a6c-4e75658c48a4");
        MapEntry goolgeEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_GOUID, "google-1234-abcde-13245");
        Assertions.assertThat(deviceIdMap).containsOnly(adidEntry, goolgeEntry);
    }

    @Ignore
    @Test
    public void testAdvertisingId_Ifa() throws Exception {

        AdX.BidRequest rtbRequest = buildAdxRequest();

        AdX.BidRequest.Builder bidBuilder = AdX.BidRequest.newBuilder(rtbRequest);
        Mobile.Builder mobuilder = Mobile.newBuilder(rtbRequest.getMobile());
        //put idfa into mobile
        byte[] cryptoIdfa = adxCryptoTool.encryptIdfa("5AAB8704-4D8C-4879-8993-C6DD8361F88F");
        mobuilder.setEncryptedAdvertisingId(ByteString.copyFrom(cryptoIdfa));
        bidBuilder.setMobile(mobuilder);
        Device.Builder debuilder = Device.newBuilder(rtbRequest.getDevice());
        debuilder.setPlatform("iphone");
        bidBuilder.setDevice(debuilder);
        rtbRequest = bidBuilder.build();

        //When 
        ByydRequest byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), rtbRequest, null);

        //Then
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        MapEntry goolgeEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_GOUID, "google-1234-abcde-13245");
        MapEntry idfaEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_IFA, "5AAB8704-4D8C-4879-8993-C6DD8361F88F");
        Assertions.assertThat(deviceIdMap).containsOnly(goolgeEntry, idfaEntry);
    }

    @Test
    public void testEncryptedHashedIdfa() throws Exception {

        AdX.BidRequest rtbRequest = buildAdxRequest();

        AdX.BidRequest.Builder bidBuilder = AdX.BidRequest.newBuilder(rtbRequest);
        Mobile.Builder mobuilder = Mobile.newBuilder(rtbRequest.getMobile());
        //put md5 idfa into mobile
        byte[] digestIdfa = MessageDigest.getInstance("MD5").digest("5AAB8704-4D8C-4879-8993-C6DD8361F88F".getBytes());
        byte[] cryptoIdfa = adxCryptoTool.encryptIdfa(digestIdfa);
        mobuilder.setEncryptedHashedIdfa(ByteString.copyFrom(cryptoIdfa));
        mobuilder.clearEncryptedAdvertisingId();
        bidBuilder.setMobile(mobuilder);
        Device.Builder debuilder = Device.newBuilder(rtbRequest.getDevice());
        debuilder.setPlatform("iphone");
        bidBuilder.setDevice(debuilder);

        rtbRequest = bidBuilder.build();

        //When 
        ByydRequest byydRequest = adxMapper.mapRequest(RtbExchange.AdX.getPublisherExternalId(), rtbRequest, null);

        //Then
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        MapEntry goolgeEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_GOUID, "google-1234-abcde-13245");
        MapEntry idfaMd5Entry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_IDFA_MD5, new String(Hex.encodeHex(digestIdfa)));
        Assertions.assertThat(deviceIdMap).containsOnly(goolgeEntry, idfaMd5Entry);
    }

    private AdX.BidRequest buildAdxRequest() throws IOException {
        //https://developers.google.com/ad-exchange/rtb/request-guide#example-bid-request
        AdX.BidRequest.Builder bidBuilder = AdXMapper.protoText2Builder(FileUtils.readFileToString(new File("src/test/data/adx/adx-example.proto.txt")));

        Mobile.Builder mobuilder = Mobile.newBuilder();
        byte[] advertisingId = adxCryptoTool.encryptAdvertisingId("0ad70515-f0e9-442c-8a6c-4e75658c48a4");
        mobuilder.setEncryptedAdvertisingId(ByteString.copyFrom(advertisingId));

        mobuilder.setAppId("123456");
        mobuilder.setIsApp(true);
        bidBuilder.setMobile(mobuilder);

        Device.Builder debuilder = Device.newBuilder();
        debuilder.setPlatform("android");
        bidBuilder.setDevice(debuilder);

        bidBuilder.setGoogleUserId("google-1234-abcde-13245");
        bidBuilder.setIp(ByteString.copyFrom(new byte[] { 10, 20, 30, 40 }));
        bidBuilder.setUserAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.107 Safari/534.13,gzip");

        AdX.BidRequest adxRequest = bidBuilder.build();
        return adxRequest;
    }

    /**
     * This is just to verify https://support.google.com/adxbuyer/answer/3221407
     * And have a recepie how to generate 
     */
    @Test
    public void test_encrypted_hashed_idfa() throws Exception {
        //String unhashedIDFA = "5AAB87044D8C48798993C6DD8361F88F"; //32 - ifa without hyphens
        //byte[] decodeHex = Hex.decodeHex(unhashedIDFA.toCharArray());
        String unhashedIDFA = "5AAB8704-4D8C-4879-8993-C6DD8361F88F"; // normal ifa
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(unhashedIDFA.getBytes());
        //System.out.println(new String(Hex.encodeHex(digest))); //3B 66 71 54 FF A2 BC 3A DF 2F 41 13 63 44 AA EE

        byte[] ivector = Hex.decodeHex("51928A6600000000AAAAAACEAAAAAACE".toCharArray());

        byte[] ekey = Hex.decodeHex("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F".toCharArray());
        byte[] ikey = Hex.decodeHex("1F1E1D1C1B1A191817161514131211100F0E0D0C0B0A09080706050403020100".toCharArray());
        DoubleClickCrypto crypto = new DoubleClickCrypto(new DoubleClickCrypto.Keys(new SecretKeySpec(ekey, "HmacSHA1"), new SecretKeySpec(ikey, "HmacSHA1")));
        byte[] all = new byte[36];
        System.arraycopy(ivector, 0, all, 0, ivector.length);
        System.arraycopy(digest, 0, all, ivector.length, digest.length);
        crypto.encrypt(all);
        //System.out.println(new String(Hex.encodeHex(encrypted))); //51 92 8A 66  00 00 00 00  AA AA AA CE  AA AA AA CE  B3 0E DB A1  D9 38 AC FB  12 C9 16 70  AB 8D 01 F3  1E 43 45 57
    }

}
