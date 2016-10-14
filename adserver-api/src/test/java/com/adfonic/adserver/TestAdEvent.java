package com.adfonic.adserver;

import static org.junit.Assert.*;
import static com.adfonic.adserver.KryoUtils.*;
import static com.adfonic.util.BitMaskUtils.*;

// This gives us the bitmask constants
import static com.adfonic.adserver.AdEvent.*;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.Ignore;

import au.com.bytecode.opencsv.CSVReader;

import com.adfonic.domain.AdAction;

public class TestAdEvent {
    @Ignore("Fails")
    @Test
    public void test() throws Exception {
        AdEventFactory adEventFactory = new AdEventFactory(new KryoManager());

        AdEvent adEvent;
        
        // Test the deserialization constructor, which is public but never gets used otherwise
        adEvent = new AdEvent();
        assertNull(adEvent.getHost());
        assertNull(adEvent.getEventTime());
        assertNull(adEvent.getUserTimeId());
        assertNull(adEvent.getAdAction());

        // Test the factory constructor
        adEvent = adEventFactory.newInstance(AdAction.IMPRESSION);
        assertNotNull(adEvent.getHost());
        assertNotNull(adEvent.getEventTime());
        assertNull(adEvent.getUserTimeId());
        assertEquals(adEvent.getAdAction(), AdAction.IMPRESSION);

        TimeZone userTimeZone = TimeZone.getTimeZone("Europe/London");
        
        Date eventTime = new Date();
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        // Test the factory constructor with a specific eventTime value
        adEvent = adEventFactory.newInstance(AdAction.IMPRESSION, eventTime, userTimeZone);
        assertEquals(eventTime, adEvent.getEventTime());
        assertNotNull(adEvent.getUserTimeId());

        adEvent.setCreativeId(12345L);
        adEvent.setCampaignId(23456L);
        adEvent.setAdSpaceId(456L);
        adEvent.setPublicationId(123L);
        adEvent.setModelId(120L);
        adEvent.setCountryId(129L);
        adEvent.setOperatorId(6L);
        adEvent.setIpAddress("1.2.3.4");
        adEvent.setUserAgentHeader("My User-Agent Value");
        adEvent.setTrackingIdentifier("abc");
        adEvent.setPostalCodeId(800000L);
        adEvent.setActionValue(123456);
        adEvent.setDeviceIdentifiers(new LinkedHashMap<Long,String>() {{
                    put(1L, "value of one");
                    put(3L, "value of three");
                }});

        byte[] serialized = adEventFactory.serialize(adEvent);
        System.out.println("Serialized size: " + serialized.length + " bytes");

        String csv = adEvent.toCsv();
        System.out.println(csv);
        
        CSVReader csvReader = new CSVReader(new StringReader(csv));
        String[] line = csvReader.readNext();
        
        adEvent = AdEvent.fromCsv(line);
        String csv2 = adEvent.toCsv();
        System.out.println(csv2);

        assertEquals(csv2, csv);
        
        adEvent = adEventFactory.deserialize(serialized);
        assertEquals(csv, adEvent.toCsv());

        assertArrayEquals(adEventFactory.serialize(adEvent), serialized);
    }

    /**
     * Test our ability to deserialize an "old" format AdEvent
     */
    @Test
    public void testBackwardCompatibility() {
        int bitmask = 0;
        bitmask = set(bitmask, BITMASK_HOST, false);
        bitmask = set(bitmask, BITMASK_CREATIVE_ID, false);
        bitmask = set(bitmask, BITMASK_CAMPAIGN_ID, false);
        bitmask = set(bitmask, BITMASK_MODEL_ID, true);
        bitmask = set(bitmask, BITMASK_COUNTRY_ID, true);
        bitmask = set(bitmask, BITMASK_OPERATOR_ID, false);
        bitmask = set(bitmask, BITMASK_AGE_RANGE, false);
        bitmask = set(bitmask, BITMASK_GENDER, false);
        bitmask = set(bitmask, BITMASK_GEOTARGET_ID, false);
        bitmask = set(bitmask, BITMASK_INTEGRATION_TYPE_ID, false);
        bitmask = set(bitmask, BITMASK_TEST_MODE, false);
        bitmask = set(bitmask, BITMASK_UNFILLED_REASON, false);
        bitmask = set(bitmask, BITMASK_USER_AGENT_HEADER, false);
        bitmask = set(bitmask, BITMASK_TRACKING_IDENTIFIER, true);
        bitmask = set(bitmask, BITMASK_RTB_SETTLEMENT_PRICE, false);
        // The old version didn't know about these higher bits, so leave 'em unset
        //bitmask = set(bitmask, BITMASK_POSTAL_CODE_ID, false);
        //bitmask = set(bitmask, BITMASK_ACTION_VALUE, false);

        Date eventTime = new Date();
        AdAction adAction = AdAction.AD_SERVED;
        long adSpaceId = 123;
        long publicationId = 234;
        long modelId = 345;
        long countryId = 456;
        String ipAddress = "123.45.67.89";
        String trackingIdentifier = "tracking identifier";
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        putInt(buffer, bitmask);
        putDate(buffer, eventTime);
        putEnum(buffer, adAction);
        putLong(buffer, adSpaceId);
        putLong(buffer, publicationId);
        putLong(buffer, modelId);
        putLong(buffer, countryId);
        putString(buffer, ipAddress);
        putString(buffer, trackingIdentifier);
        
        buffer.flip();

        AdEvent adEvent = new AdEvent();
        adEvent.readObjectData(null, buffer);
        assertEquals("eventTime", eventTime, adEvent.getEventTime());
        assertEquals("adAction", adAction, adEvent.getAdAction());
        assertEquals("adSpaceId", adSpaceId, adEvent.getAdSpaceId());
        assertEquals("publicationId", publicationId, adEvent.getPublicationId());
        assertNull(adEvent.getCreativeId());
        assertNull(adEvent.getCampaignId());
        assertEquals("modelId", (Object)modelId, adEvent.getModelId());
        assertEquals("countryId", (Object)countryId, adEvent.getCountryId());
        assertEquals("ipAddress", ipAddress, adEvent.getIpAddress());
        assertEquals("trackingIdentifier", trackingIdentifier, adEvent.getTrackingIdentifier());
    }
}
