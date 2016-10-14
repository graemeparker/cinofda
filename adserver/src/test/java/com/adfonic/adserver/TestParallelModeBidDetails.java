package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.HashMap;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestParallelModeBidDetails extends AbstractAdfonicTest {
    @Test
    public void test01_constructor_with_context() {
        final TargetingContext context = mock(TargetingContext.class, "context");
        final String ipAddress = randomAlphaNumericString(10);
        Impression impression = mock(Impression.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.IP); will(returnValue(ipAddress));
        }});
        ParallelModeBidDetails bidDetails = new ParallelModeBidDetails(context, impression);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(impression, bidDetails.getImpression());
        assertEquals(context, bidDetails.getBidTimeTargetingContext());
    }
    
    @Test
    public void test02_constructor_with_ip() {
        String ipAddress = randomAlphaNumericString(10);
        Impression impression = mock(Impression.class);
        ParallelModeBidDetails bidDetails = new ParallelModeBidDetails(ipAddress, impression);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(impression, bidDetails.getImpression());
        assertNull(bidDetails.getBidTimeTargetingContext());
    }

    @Test
    public void test03_toMap() {
        String ipAddress = randomAlphaNumericString(10);
        final Impression impression = mock(Impression.class);
        final byte[] serializedImpression = randomAlphaNumericString(100).getBytes();
        final KryoManager kryoManager = mock(KryoManager.class);
        expect(new Expectations() {{
            oneOf (kryoManager).writeObject(impression); will(returnValue(serializedImpression));
        }});
        ParallelModeBidDetails bidDetails = new ParallelModeBidDetails(ipAddress, impression);
        HashMap<String,Serializable> map = bidDetails.toMap(kryoManager);
        assertEquals(ipAddress, map.get("ip"));
        assertEquals(serializedImpression, map.get("imp"));
    }

    @Test
    public void test04_fromMap() {
        final String ipAddress = randomAlphaNumericString(10);
        final Impression impression = mock(Impression.class);
        final byte[] serializedImpression = randomAlphaNumericString(100).getBytes();
        final KryoManager kryoManager = mock(KryoManager.class);
        expect(new Expectations() {{
            oneOf (kryoManager).readObject(serializedImpression, Impression.class); will(returnValue(impression));
        }});
        @SuppressWarnings("serial")
		HashMap<String,Serializable> map = new HashMap<String,Serializable>() {{
                put("ip", ipAddress);
                put("imp", serializedImpression);
            }};
        ParallelModeBidDetails bidDetails = ParallelModeBidDetails.fromMap(map, kryoManager);
        assertEquals(ipAddress, bidDetails.getIpAddress());
        assertEquals(impression, bidDetails.getImpression());
        assertNull(bidDetails.getBidTimeTargetingContext());
    }
}
