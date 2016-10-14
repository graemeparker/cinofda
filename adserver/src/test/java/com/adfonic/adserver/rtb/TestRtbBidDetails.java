package com.adfonic.adserver.rtb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.test.AbstractAdfonicTest;

public class TestRtbBidDetails extends AbstractAdfonicTest {
    @Test
    public void test01_constructor_with_context_with_platform() {
        final TargetingContext context = mock(TargetingContext.class, "context");
        final Impression impression = mock(Impression.class);
        final String ipAddress = randomAlphaNumericString(10);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final String displayTypeSystemName = randomAlphaNumericString(10);
        final PlatformDto platform = mock(PlatformDto.class, "platform");
        final long platformId = randomLong();
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.IP); will(returnValue(ipAddress));
            oneOf (displayType).getSystemName(); will(returnValue(displayTypeSystemName));
            oneOf (context).getAttribute(TargetingContext.PLATFORM); will(returnValue(platform));
            oneOf (platform).getId(); will(returnValue(platformId));
        }});
        RtbBidDetails bidDetails = new RtbBidDetails(context, impression, displayType, null);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(displayTypeSystemName, bidDetails.getDisplayTypeSystemName());
        assertEquals(platformId, (long)bidDetails.getPlatformId());
        assertEquals(impression, bidDetails.getImpression());
        assertEquals(context, bidDetails.getBidTimeTargetingContext());
    }
    
    @Test
    public void test02_constructor_with_context_no_platform() {
        final TargetingContext context = mock(TargetingContext.class, "context");
        final Impression impression = mock(Impression.class);
        final String ipAddress = randomAlphaNumericString(10);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final String displayTypeSystemName = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.IP); will(returnValue(ipAddress));
            oneOf (displayType).getSystemName(); will(returnValue(displayTypeSystemName));
            oneOf (context).getAttribute(TargetingContext.PLATFORM); will(returnValue(null));
        }});
        RtbBidDetails bidDetails = new RtbBidDetails(context, impression, displayType, null);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(displayTypeSystemName, bidDetails.getDisplayTypeSystemName());
        assertNull(bidDetails.getPlatformId());
        assertEquals(impression, bidDetails.getImpression());
        assertEquals(context, bidDetails.getBidTimeTargetingContext());
    }
    
    @Test
    public void test03_constructor_with_no_context() {
        final String ipAddress = randomAlphaNumericString(10);
        final String displayTypeSystemName = randomAlphaNumericString(10);
        final Long platformId = randomLong();
        final Impression impression = mock(Impression.class);
        RtbBidDetails bidDetails = new RtbBidDetails(ipAddress, displayTypeSystemName, platformId, impression, null);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(displayTypeSystemName, bidDetails.getDisplayTypeSystemName());
        assertEquals(platformId, bidDetails.getPlatformId());
        assertEquals(impression, bidDetails.getImpression());
        assertNull(bidDetails.getBidTimeTargetingContext());
    }

    @Test
    public void test04_toMap_with_platform_id() {
        final String ipAddress = randomAlphaNumericString(10);
        final String displayTypeSystemName = randomAlphaNumericString(10);
        final Long platformId = randomLong();
        final Impression impression = mock(Impression.class);
        final byte[] serializedImpression = randomAlphaNumericString(100).getBytes();
        final KryoManager kryoManager = mock(KryoManager.class);
        expect(new Expectations() {{
            oneOf (kryoManager).writeObject(impression); will(returnValue(serializedImpression));
        }});
        RtbBidDetails bidDetails = new RtbBidDetails(ipAddress, displayTypeSystemName, platformId, impression, null);
        HashMap<String,Serializable> map = bidDetails.toMap(kryoManager);
        assertEquals(ipAddress, map.get(RtbBidDetails.IP_ADDRESS));
        assertEquals(displayTypeSystemName, map.get(RtbBidDetails.DISPLAY_TYPE_SYSTEM_NAME));
        assertEquals(platformId, map.get(RtbBidDetails.PLATFORM_ID));
        assertEquals(serializedImpression, map.get(RtbBidDetails.IMPRESSION));
    }

    @Test
    public void test05_toMap_without_platform_id() {
        final String ipAddress = randomAlphaNumericString(10);
        final String displayTypeSystemName = randomAlphaNumericString(10);
        final Impression impression = mock(Impression.class);
        final byte[] serializedImpression = randomAlphaNumericString(100).getBytes();
        final KryoManager kryoManager = mock(KryoManager.class);
        expect(new Expectations() {{
            oneOf (kryoManager).writeObject(impression); will(returnValue(serializedImpression));
        }});
        RtbBidDetails bidDetails = new RtbBidDetails(ipAddress, displayTypeSystemName, null, impression, null);
        HashMap<String,Serializable> map = bidDetails.toMap(kryoManager);
        assertEquals(ipAddress, map.get(RtbBidDetails.IP_ADDRESS));
        assertEquals(displayTypeSystemName, map.get(RtbBidDetails.DISPLAY_TYPE_SYSTEM_NAME));
        assertNull(map.get(RtbBidDetails.PLATFORM_ID));
        assertEquals(serializedImpression, map.get(RtbBidDetails.IMPRESSION));
    }

    @Test
    public void test06_fromMap() {
        final String ipAddress = randomAlphaNumericString(10);
        final String displayTypeSystemName = randomAlphaNumericString(10);
        final Long platformId = randomLong();
        final Impression impression = mock(Impression.class);
        final byte[] serializedImpression = randomAlphaNumericString(100).getBytes();
        final KryoManager kryoManager = mock(KryoManager.class);
        expect(new Expectations() {{
            oneOf (kryoManager).readObject(serializedImpression, Impression.class); will(returnValue(impression));
        }});
        @SuppressWarnings("serial")
		HashMap<String,Serializable> map = new HashMap<String,Serializable>() {{
                put(RtbBidDetails.IP_ADDRESS, ipAddress);
                put(RtbBidDetails.DISPLAY_TYPE_SYSTEM_NAME, displayTypeSystemName);
                put(RtbBidDetails.PLATFORM_ID, platformId);
                put(RtbBidDetails.IMPRESSION, serializedImpression);
            }};
        RtbBidDetails bidDetails = RtbBidDetails.fromMap(map, kryoManager);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(displayTypeSystemName, bidDetails.getDisplayTypeSystemName());
        assertEquals(platformId, bidDetails.getPlatformId());
        assertEquals(impression, bidDetails.getImpression());
        assertNull(bidDetails.getBidTimeTargetingContext());
    }
}
