package com.adfonic.adserver;

import static org.junit.Assert.*;
import static com.adfonic.adserver.KryoUtils.*;
import static com.adfonic.util.BitMaskUtils.*;

// This gives us the bitmask constants
import static com.adfonic.adserver.Impression.*;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

public class TestImpression {
    /**
     * Test our ability to deserialize an "old" format Impression
     */
    @Test
    public void testBackwardCompatibility() {
        int bitmask = 0;
        bitmask = set(bitmask, BITMASK_EXTERNAL_ID, true);
        bitmask = set(bitmask, BITMASK_MODEL_ID, true);
        bitmask = set(bitmask, BITMASK_COUNTRY_ID, true);
        bitmask = set(bitmask, BITMASK_TRACKING_IDENTIFIER, true);
        // We'll pass this one to make sure a bit is set higher than lat/lon
        bitmask = set(bitmask, BITMASK_INTEGRATION_TYPE_ID, true);

        String externalID = UUID.randomUUID().toString();
        Date creationTime = new Date();
        String trackingIdentifier = "tracking identifier";
        long adSpaceId = 123;
        long creativeId = 234;
        long modelId = 345;
        long countryId = 456;
        long integrationTypeId = 567;
        
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        putInt(buffer, bitmask);
        putString(buffer, externalID);
        putDate(buffer, creationTime);
        putString(buffer, trackingIdentifier);
        putLong(buffer, adSpaceId);
        putLong(buffer, creativeId);
        putLong(buffer, modelId);
        putLong(buffer, countryId);
        putLong(buffer, integrationTypeId);
        
        buffer.flip();

        Impression impression = new Impression();
        impression.readObjectData(null, buffer);
        assertEquals("externalID", externalID, impression.getExternalID());
        assertEquals("creationTime", creationTime, impression.getCreationTime());
        assertEquals("trackingIdentifier", trackingIdentifier, impression.getTrackingIdentifier());
        assertEquals("adSpaceId", adSpaceId, impression.getAdSpaceId());
        assertEquals("creativeId", creativeId, impression.getCreativeId());
        assertEquals("modelId", (Object)modelId, impression.getModelId());
        assertEquals("countryId", (Object)countryId, impression.getCountryId());
        assertEquals("integrationTypeId", (Object)integrationTypeId, impression.getIntegrationTypeId());
    }

    @Test
    public void testGetDeviceIdentifiers_AF_1192() {
        Impression impression = new Impression();
        assertNotNull(impression.getDeviceIdentifiers());
    }
}
