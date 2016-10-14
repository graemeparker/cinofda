package com.adfonic.domain;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestDestination {
    @Test
    public void testSanitizeUrl_lowerCaseAlready() {
        String url = "http://whatever.com/foo?p=%publication%&dpid=%dpid%&c=%creative%&a=%campaign%&ad=%advertiser%&pu=%pid%&click=%click%&ts=%timestamp%";
        assertEquals(url, Destination.sanitizeUrl(url));
    }
    
    @Test
    public void testSanitizeUrl_upperCase() {
        String upperCase = "http://whatever.com/foo?p=%PUBLICATION%&dpid=%DPID%&c=%CREATIVE%&a=%CAMPAIGN%&ad=%ADVERTISER%&pu=%PID&click=%CLICK%&ts=%TIMESTAMP%";
        String valid = "http://whatever.com/foo?p=%publication%&dpid=%dpid%&c=%creative%&a=%campaign%&ad=%advertiser%&pu=%pid&click=%click%&ts=%timestamp%";
        assertEquals(valid, Destination.sanitizeUrl(upperCase));
    }
    
    @Test
    public void testSanitizeUrl_mixedCase() {
        String mixedCase = "http://whatever.com/foo?p=%Publication%&dpid=%DpId%&c=%CreAtiVe%&a=%Campaign%&ad=%Advertiser%&pu=%Pid&click=%CliCK%&ts=%TimeStamp%";
        String valid = "http://whatever.com/foo?p=%publication%&dpid=%dpid%&c=%creative%&a=%campaign%&ad=%advertiser%&pu=%pid&click=%click%&ts=%timestamp%";
        assertEquals(valid, Destination.sanitizeUrl(mixedCase));
    }
    
    @Test
    public void testUrlSanitization() {
        Company company = new Company("test");
        Advertiser advertiser = new Advertiser(company, "test");
        Destination destination;
        for (DestinationType destinationType : DestinationType.values()) {
            String valid = "http://whatever.com/foo?p=%publication%&dpid=%dpid%&c=%creative%&a=%campaign%&ad=%advertiser%&pu=%pid&click=%click%&ts=%timestamp%";
            String upperCase = "http://whatever.com/foo?p=%PUBLICATION%&dpid=%DPID%&c=%CREATIVE%&a=%CAMPAIGN%&ad=%ADVERTISER%&pu=%PID&click=%CLICK%&ts=%TIMESTAMP%";
            String mixedCase = "http://whatever.com/foo?p=%Publication%&dpid=%DpId%&c=%CreAtiVe%&a=%Campaign%&ad=%AdVertiser%&pu=%PId&click=%CliCK%&ts=%TimeStamp%";

            // Make sure the constructor sanitizes the destination URL
            
            destination = advertiser.newDestination(destinationType, valid, null);
            assertEquals("Valid constructor", valid, destination.getData());
            
            destination = advertiser.newDestination(destinationType, upperCase, null);
            assertEquals("Upper case constructor", valid, destination.getData());
            
            destination = advertiser.newDestination(destinationType, mixedCase, null);
            assertEquals("Mixed case constructor", valid, destination.getData());

            // Make sure setData sanitizes the URL passed in
            
            destination.setData(valid);
            assertEquals("Valid setData", valid, destination.getData());

            destination.setData(upperCase);
            assertEquals("Upper case setData", valid, destination.getData());
            
            destination.setData(mixedCase);
            assertEquals("Mixed case setData", valid, destination.getData());

            // Make sure beaconUrl gets sanitized as well when it gets set
            BeaconUrl beacon = new BeaconUrl(destination,valid);
            destination.addBeaconUrl(beacon);
            assertEquals("Valid setBeaconUrl", valid, destination.getBeaconUrls().get(0).getUrl());

            destination.getBeaconUrls().get(0).setUrl(upperCase);
            assertEquals("Upper case setBeaconUrl", valid, destination.getBeaconUrls().get(0).getUrl());

            destination.getBeaconUrls().get(0).setUrl(mixedCase);
            assertEquals("Mixed case setBeaconUrl", valid, destination.getBeaconUrls().get(0).getUrl());
            
        }
    }
        
    @Test
    public void testSanitizeUrl_beacons() {
        Company company = new Company("test");
        Advertiser advertiser = new Advertiser(company, "test");
        Destination destination;
        for (DestinationType destinationType : DestinationType.values()) {
            String valid = "http://whatever.com/foo?p=%publication%&dpid=%dpid%&c=%creative%&a=%campaign%&ad=%advertiser%&pu=%pid&click=%click%&ts=%timestamp%";
            String upperCase = "http://whatever.com/foo?p=%PUBLICATION%&dpid=%DPID%&c=%CREATIVE%&a=%CAMPAIGN%&ad=%ADVERTISER%&pu=%PID&click=%CLICK%&ts=%TIMESTAMP%";
            String mixedCase = "http://whatever.com/foo?p=%Publication%&dpid=%DpId%&c=%CreAtiVe%&a=%Campaign%&ad=%AdverTiser%&pu=%PId&click=%CliCK%&ts=%TimeStamp%";
            
            List<BeaconUrl> beacons = new ArrayList<BeaconUrl>();
            BeaconUrl beaconValid = new BeaconUrl(valid);
            beacons.add(beaconValid);

            // Make sure the constructor sanitizes the destination URL
            
            destination = advertiser.newDestination(destinationType, valid, null);
            assertEquals("Valid constructor", valid, destination.getData());
            
            destination = advertiser.newDestination(destinationType, upperCase, null);
            assertEquals("Upper case constructor", valid, destination.getData());
            
            destination = advertiser.newDestination(destinationType, mixedCase, beacons);
            assertEquals("Mixed case constructor", valid, destination.getData());

            // Make sure setData sanitizes the URL passed in
            
            destination.setData(valid);
            assertEquals("Valid setData", valid, destination.getData());

            destination.setData(upperCase);
            assertEquals("Upper case setData", valid, destination.getData());
            
            destination.setData(mixedCase);
            assertEquals("Mixed case setData", valid, destination.getData());

            // Make sure beaconUrl gets sanitized as well when it gets set            
            assertEquals("Valid setBeaconUrl", valid, destination.getBeaconUrls().get(0).getUrl());

            destination.getBeaconUrls().get(0).setUrl(upperCase);
            assertEquals("Upper case setBeaconUrl", valid, destination.getBeaconUrls().get(0).getUrl());

            destination.getBeaconUrls().get(0).setUrl(mixedCase);
            assertEquals("Mixed case setBeaconUrl", valid, destination.getBeaconUrls().get(0).getUrl());
        }
    }
}