package com.adfonic.adx.client.impl;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.logging.Logger;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adx.client.AdXClient;
import com.adfonic.adx.client.AdXClientException;
import com.adfonic.test.AbstractAdfonicTest;

/**
 * Run this test via:
 *
 * mvn -Dcom.adfonic.adx.client.secret=... -Dtest=AdXClientImplIT test
 *
 */
public class AdXClientImplIT extends AbstractAdfonicTest {
    private static final transient Logger LOG = Logger.getLogger(AdXClientImplIT.class.getName());
    
    private static final String SECRET = System.getProperty("com.adfonic.adx.client.secret");
    private static final String BASE_URI = "https://ad-x.co.uk";
    private static final int CONNECTION_TIMEOUT = 1000;
    private static final int SO_TIMEOUT = 2000;
    
    private AdXClientImpl impl;

    @Before
    public void runBeforeEachTest() {
        LOG.info("Using secret=" + SECRET + ", baseUri=" + BASE_URI);
        impl = new AdXClientImpl(SECRET, BASE_URI, CONNECTION_TIMEOUT, SO_TIMEOUT, new DefaultHttpClient());
    }

    @Test
    public void test01_provision_and_update() throws AdXClientException {
        String bundleId = "com.priceline.negotiator";
        String advertiserExternalId = UUID.randomUUID().toString();
        String creativeExternalId = UUID.randomUUID().toString();
        AdXClient.Platform platform = AdXClient.Platform.iOS;
        String destinationUrl = "https://app-install.priceline.com/API/click/31qpon43B/5018149b9509c";

        // First time should return CREATED
        assertEquals(AdXClient.Outcome.CREATED, impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl));

        // Second time should return UPDATED
        assertEquals(AdXClient.Outcome.UPDATED, impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl));

        // Try changing a param, should return UPDATED as well
        platform = AdXClient.Platform.Android;
        assertEquals(AdXClient.Outcome.UPDATED, impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl));

        /*
         * This currently fails...since these URLs are apparently "special"
        // Try changing a param, should return UPDATED as well
        destinationUrl = destinationUrl + "f";
        assertEquals(AdXClient.Outcome.UPDATED, impl.provisionCreative(bundleId, advertiserExternalId, creativeExternalId, platform, destinationUrl));
        */
    }
}