package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestTrackerClient extends AbstractAdfonicTest {

    private TrackerClient setupTrackerClient(boolean blocked) {
        return new TrackerClient(blocked, null, null, null);
    }

    @Test
    public void test01_trackInstall_blocked() throws Exception {
        final String appId = randomAlphaNumericString(10);
        final String udid = randomHexString(40);
        assertEquals(TrackerClient.BLOCKED_RESPONSE_MAP, setupTrackerClient(true).trackInstall(appId, udid));
    }
    
    @Test
    public void test02_trackConversion_blocked() throws Exception {
        final String clickExternalID = randomAlphaNumericString(10);
        assertEquals(TrackerClient.BLOCKED_RESPONSE_MAP, setupTrackerClient(true).trackConversion(clickExternalID));
    }

    // Nothing else is tested here since it's currently impossible to mock the
    // HttpClient, at least the way AbstractThreadSafeHttpClient has been built.
    // I just wanted to make sure the new "blocked" stuff behaves.
}
