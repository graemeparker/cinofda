package com.adfonic.webservices.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.device.service.DeviceManager;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-jpa-context.xml"})
public class TestCampaignController extends AbstractAdfonicTest {
    private CampaignManager campaignManager;
    private DeviceManager deviceManager;

    @Before
    public void runBeforeEachTest() {
        campaignManager = mock(CampaignManager.class);
        deviceManager = mock(DeviceManager.class);
    }

    @Test
    public void testTempSetupDeviceIdentifierTypesAsNeeded() {
        final Creative creative = mock(Creative.class);
        final Campaign campaign = mock(Campaign.class);
        final Destination destination = mock(Destination.class);
        final DeviceIdentifierType udid = mock(DeviceIdentifierType.class, "udid");
        final DeviceIdentifierType dpid = mock(DeviceIdentifierType.class, "dpid");
        final DeviceIdentifierType openudid = mock(DeviceIdentifierType.class, "openudid");
        final DeviceIdentifierType android = mock(DeviceIdentifierType.class, "android");
        final Set<DeviceIdentifierType> dits = new HashSet<DeviceIdentifierType>();
        
        expect(new Expectations() {{
            allowing (campaign).getId(); will(returnValue(randomLong()));
            allowing (creative).getId(); will(returnValue(randomLong()));
            allowing (creative).getCampaign(); will(returnValue(campaign));
            allowing (creative).getDestination(); will(returnValue(destination));
            allowing (campaign).getDeviceIdentifierTypes(); will(returnValue(dits));
            
            // Test 1: campaign is not install trackable
            oneOf (campaign).isInstallTrackingEnabled(); will(returnValue(false));

            // Test 2: IPHONE_APP_STORE
            oneOf (campaign).isInstallTrackingEnabled(); will(returnValue(true));
            oneOf (destination).getDestinationType(); will(returnValue(DestinationType.IPHONE_APP_STORE));
            oneOf (deviceManager).getDeviceIdentifierTypeBySystemName("udid"); will(returnValue(udid));
            oneOf (deviceManager).getDeviceIdentifierTypeBySystemName("dpid"); will(returnValue(dpid));
            oneOf (deviceManager).getDeviceIdentifierTypeBySystemName("openudid"); will(returnValue(openudid));
            oneOf (campaignManager).update(campaign); will(returnValue(campaign));

            // Test 3: ANDROID
            oneOf (campaign).isInstallTrackingEnabled(); will(returnValue(true));
            oneOf (destination).getDestinationType(); will(returnValue(DestinationType.ANDROID));
            oneOf (deviceManager).getDeviceIdentifierTypeBySystemName("android"); will(returnValue(android));
            oneOf (deviceManager).getDeviceIdentifierTypeBySystemName("dpid"); will(returnValue(dpid));
            oneOf (campaignManager).update(campaign); will(returnValue(campaign));

            // Test 4: some other DestinationType
            oneOf (campaign).isInstallTrackingEnabled(); will(returnValue(true));
            exactly(2).of (destination).getDestinationType(); will(returnValue(DestinationType.URL));
        }});

        // Test 1
        dits.clear();
        CampaignController.tempSetupDeviceIdentifierTypesAsNeeded(campaign, creative, campaignManager, deviceManager);
        assertTrue("dits should be empty, but contains: " + dits, dits.isEmpty());

        // Test 2
        dits.clear();
        CampaignController.tempSetupDeviceIdentifierTypesAsNeeded(campaign, creative, campaignManager, deviceManager);
        assertEquals("dits should contain 3 elements, but contains: " + dits, 3, dits.size());
        assertTrue(dits.contains(udid));
        assertTrue(dits.contains(dpid));
        assertTrue(dits.contains(openudid));

        // Test 3
        dits.clear();
        CampaignController.tempSetupDeviceIdentifierTypesAsNeeded(campaign, creative, campaignManager, deviceManager);
        assertEquals("dits should contain 2 elements, but contains: " + dits, 2, dits.size());
        assertTrue(dits.contains(android));
        assertTrue(dits.contains(dpid));

        // Test 4
        dits.clear();
        CampaignController.tempSetupDeviceIdentifierTypesAsNeeded(campaign, creative, campaignManager, deviceManager);
        assertTrue("dits should be empty, but contains: " + dits, dits.isEmpty());
    }
}