package com.adfonic.adserver;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import org.junit.Test;

import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Gender;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.Range;

public class TestSerializedImpressionSize extends AbstractAdfonicTest {
    @Test
    public void test() throws Exception {
        KryoManager kryoManager = new KryoManager();

        byte[] serialized;
        String base64;
        
        for (int k = 0; k < 2; ++k) {
            Impression impression = new Impression();
            //impression.setTrackingIdentifier(DigestUtils.shaHex("0123456789abcdef0123456789abcdef01234567"));
            impression.setAdSpaceId(12345L);
            impression.setCreativeId(12345L);
            impression.setModelId(9999L);
            impression.setCountryId(123L);
            impression.setOperatorId(123L);
            impression.setAgeRange(new Range<Integer>(0, 70));
            impression.setGender(Gender.MALE);
            impression.setGeotargetId(123L);
            impression.setIntegrationTypeId(123L);
            impression.setPostalCodeId(777777L);

            impression.setDeviceIdentifiers(new HashMap<Long,String>() {{
                        put(1L, randomHexString(40));
                    }});

            impression.setHost("ch1adserver14");
            impression.setStrategy("foo");
            impression.setDateOfBirth(null);
            impression.setLatitude(38.23444444444444);
            impression.setLongitude(-84.43469444444445);
            impression.setLocationSource("DERIVED");
            
            assertNull(impression.getUserTimeZone());
            
            impression.setUserTimeZoneId("Europe/London");
            assertNotNull(impression.getUserTimeZone());

            switch (k) {
            case 0:
                System.out.println("==================== adserver -> tracker =====================");
                //System.out.println("ExternalID size: " + impression.getExternalID().length());
                break;
            case 1:
                System.out.println("==================== cache or URL =====================");
                impression.setExternalID(null);
                break;
            }
            
            serialized = kryoManager.writeObject(impression);
            base64 = Base64.encodeBase64URLSafeString(serialized);
            //System.out.println(base64);
            System.out.println("Long tail Impression size: " + serialized.length + " (base64 size: " + base64.length() + ")");

            assertEquals(impression, kryoManager.readObject(serialized, Impression.class));
        
            impression.setPdDestinationUrl("http://rrmprod.amobee.com/upsteed/actionpage?as=95&t=1311416446242&h=2221907&pl=1&u=a7b81cb6-20cd-40a8-a5f8-1895af36cbc7&isu=false&i=166.132.164.157&monitor=1&a=1999715");
            serialized = kryoManager.writeObject(impression);
            base64 = Base64.encodeBase64URLSafeString(serialized);
            //System.out.println(base64);
            System.out.println("Plugin-served Impression size: " + serialized.length + " (base64 size: " + base64.length() + ")");
            
            assertEquals(impression, kryoManager.readObject(serialized, Impression.class));
            
            impression.setPdDestinationUrl(null);
            impression.setRtbBidPrice(BigDecimal.valueOf(0.12));
            impression.setRtbSettlementPrice(BigDecimal.valueOf(0.09));
            serialized = kryoManager.writeObject(impression);
            base64 = Base64.encodeBase64URLSafeString(serialized);
            //System.out.println(base64);
            System.out.println("RTB Impression size: " + serialized.length + " (base64 size: " + base64.length() + ")");
            
            assertEquals(impression, kryoManager.readObject(serialized, Impression.class));
        }

        Impression impression = new Impression();
        impression.setAdSpaceId(12345L);
        impression.setCreativeId(12345L);
        impression.setModelId(9999L);
        impression.setCountryId(123L);
        impression.setOperatorId(123L);
        impression.setIntegrationTypeId(123L);
        impression.setPostalCodeId(777777L);
        System.out.println("==================== minimal cache or URL =====================");
        impression.setExternalID(null);
        
        serialized = kryoManager.writeObject(impression);
        base64 = Base64.encodeBase64URLSafeString(serialized);
        //System.out.println(base64);
        System.out.println("Impression size: " + serialized.length + " (base64 size: " + base64.length() + ")");
    }

    private static void dumpBytes(byte[] bytes) {
        for (byte b : bytes) {
            int i = b;
            if (i < 0) {
                i += 256;
            }
            if (i < 100) {
                System.out.print(' ');
            }
            if (i < 10) {
                System.out.print(' ');
            }
            System.out.print(i + " (0x" + (i < 16 ? "0" : "") + Integer.toHexString(i).toUpperCase() + ") ");
            if (i < (1<<7)) {
                System.out.print('0');
            }
            if (i < (1<<6)) {
                System.out.print('0');
            }
            if (i < (1<<5)) {
                System.out.print('0');
            }
            if (i < (1<<4)) {
                System.out.print('0');
            }
            if (i < (1<<3)) {
                System.out.print('0');
            }
            if (i < (1<<2)) {
                System.out.print('0');
            }
            if (i < (1<<1)) {
                System.out.print('0');
            }
            System.out.println(Integer.toBinaryString(i));
        }
    }
}
