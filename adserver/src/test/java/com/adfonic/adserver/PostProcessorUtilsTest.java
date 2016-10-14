package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

@SuppressWarnings("serial")
public class PostProcessorUtilsTest extends AbstractAdfonicTest {

    private static final String allMacrosUrl = "http://whatever.com/foo" //
            + "?publication=%publication%" //
            + "&publisher_id=%publisher_id%" //
            + "&pid=%pid%" //
            + "&publication_id=%publication_id%" //
            + "&creative=%creative%" //
            + "&campaign=%campaign%" //
            + "&advertiser=%advertiser%" //
            + "&platform=%platform%" // 
            + "&device_type=%device_type%" //
            + "&dtd=%dtd%" //
            + "&click=%click%" //
            + "&latitude=%latitude%" //
            + "&longitude=%longitude%" //
            + "&locationtype=%locationtype%" //
            + "&dpid=%dpid%" //
            + "&ifa=%ifa%" //
            + "&idfa=%idfa%" //
            + "&hifa=%hifa%" //
            + "&idfa_md5=%idfa_md5%" //
            + "&adid=%adid%" //
            + "&adid_md5=%adid_md5%" //
            + "&device_id=%device_id%"; //

    private final long dpidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    //private final long odin1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    //private final long openudidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long idfaDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long idfaSha1DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long idfaMd5DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long adidDeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");
    private final long adidMd5DeviceIdentifierTypeId = uniqueLong("DeviceIdentifierType.id");

    private final Map<String, String> deviceProperties = Collections.emptyMap();

    /**
     * Keys are copy from DeviceIdentifierType class
     */
    private final Map<String, Long> deviceIdentifierTypeIdsBySystemName = new HashMap<String, Long>() {
        {
            put("dpid", dpidDeviceIdentifierTypeId);
            put("ifa", idfaDeviceIdentifierTypeId);
            put("hifa", idfaSha1DeviceIdentifierTypeId);
            put("idfa_md5", idfaMd5DeviceIdentifierTypeId);
            put("adid", adidDeviceIdentifierTypeId);
            put("adid_md5", adidMd5DeviceIdentifierTypeId);
        }
    };

    @Test
    public void testPostProcessVariablesNullUrl() {
        String result = MacroTractor.resolveMacros(null, null, null, null, null, null, null, null, deviceIdentifierTypeIdsBySystemName, deviceProperties, false, null, true, null);
        assertNull(result);
    }

    @Test
    public void testPostProcessVariablesNullValues() {
        String result = MacroTractor.resolveMacros("http://whatever.com", null, null, null, null, null, null, null, deviceIdentifierTypeIdsBySystemName, deviceProperties, false,
                null, true, null);
        String expected = "http://whatever.com";
        assertEquals(expected, result);

    }

    @Test
    public void testAllMacrosResolvedNoValues() {
        Map<String, String> deviceProperties = Collections.emptyMap();
        String originalUrl = allMacrosUrl + "&wrong=%wrong%"; //

        String expected = "http://whatever.com/foo" //
                + "?publication=" //
                + "&publisher_id="//
                + "&pid=" //
                + "&publication_id=" //
                + "&creative=" //
                + "&campaign=" //
                + "&advertiser=" //
                + "&platform=" //
                + "&device_type=" //
                + "&dtd=" + "1" // true as parameter
                + "&click=" //
                + "&latitude=" //
                + "&longitude=" //
                + "&locationtype=" //
                + "&dpid=" //
                + "&ifa=" //
                + "&idfa="//
                + "&hifa=" //
                + "&idfa_md5=" //
                + "&adid=" //
                + "&adid_md5=" //
                + "&device_id=" //
                + "&wrong=%wrong%" // unresolved
        ; //
        String result = MacroTractor.resolveMacros(originalUrl, null, null, null, null, null, null, null, deviceIdentifierTypeIdsBySystemName, deviceProperties, true, null, true,
                null);
        assertEquals(expected, result);
    }

    @Test
    public void testAllMacrosResolvedWithValues() {
        final String dpid = randomHexString(40);
        final String idfa = randomHexString(32);
        final String idfa_sha1 = randomHexString(40);
        final String idfa_md5 = randomHexString(40);
        final String adid = randomHexString(32);
        final String adid_md5 = randomHexString(40);
        Map<Long, String> deviceIdentifiers = new HashMap<Long, String>() {
            {
                put(dpidDeviceIdentifierTypeId, dpid);
                put(idfaDeviceIdentifierTypeId, idfa);
                put(idfaSha1DeviceIdentifierTypeId, idfa_sha1);
                put(idfaMd5DeviceIdentifierTypeId, idfa_md5);
                put(adidDeviceIdentifierTypeId, adid);
                put(adidMd5DeviceIdentifierTypeId, adid_md5);
            }
        };

        Map<String, String> deviceProperties = new HashMap<String, String>() {
            {
                put("osName", "iPotato");
                put("isTablet", "1");
            }
        };

        final String publisherID = randomAlphaNumericString(10);
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String publicationExternalID = randomAlphaNumericString(10);

        final String campaignExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);

        Impression impression = new Impression();
        impression.setDeviceIdentifiers(deviceIdentifiers);
        impression.setLatitude(1.1);
        impression.setLongitude(2.2);
        impression.setLocationSource(LocationSource.EXPLICIT.name());

        String originalUrl = allMacrosUrl + "&wrong=%wrong%"; //

        String expected = "http://whatever.com/foo" //
                + "?publication=" + adSpaceExternalID //
                + "&publisher_id=" + publisherID //
                + "&pid=" + publicationExternalID //
                + "&publication_id=" + publicationExternalID //
                + "&creative=" + creativeExternalID //
                + "&campaign=" + campaignExternalID //
                + "&advertiser=" + advertiserExternalID //
                + "&platform=" + "IPOTATO" //
                + "&device_type=" + "TAB" //
                + "&dtd=" + "0" // false as parameter
                + "&click=" + impression.getExternalID() //
                + "&latitude=" + impression.getLatitude() //
                + "&longitude=" + impression.getLongitude() //
                + "&locationtype=" + "1" // 1 ~ EXPLICIT
                + "&dpid=" + dpid //
                + "&ifa=" + idfa //
                + "&idfa=" + idfa //
                + "&hifa=" + idfa_sha1 //
                + "&idfa_md5=" + idfa_md5 //
                + "&adid=" + adid //
                + "&adid_md5=" + adid_md5 //
                + "&device_id=" + idfa //
                + "&wrong=%wrong%" // unresolved
        ; //

        String actual = MacroTractor.resolveMacros(originalUrl, publisherID, adSpaceExternalID, creativeExternalID, campaignExternalID, advertiserExternalID,
                publicationExternalID, impression, deviceIdentifierTypeIdsBySystemName, deviceProperties, false, null, true, null);

        assertEquals(expected, actual);
    }

    @Test
    public void testPostProcessVariables04() {
        final String idfa = randomHexString(40);

        Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(idfaDeviceIdentifierTypeId, idfa);
            }
        };

        final String publisherID = randomAlphaNumericString(10);
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String campaignExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String publicationExternalID = randomAlphaNumericString(10);

        Impression impression = new Impression();
        impression.setDeviceIdentifiers(secureDeviceIdentifiers);

        String url = "http://whatever.com/foo" //
                + "?p=%publication%" //
                + "&dpid=%dpid%" //
                + "&publisher_id=%publisher_id%" //
                + "&idfa=%idfa%" //
                + "&adid=%adid%" //
                + "&c=%creative%" //
                + "&a=%campaign%" //
                + "&ad=%advertiser%" //
                + "&pu=%pid%" //
                + "&click=%click%"; //
        String expected = "http://whatever.com/foo" //
                + "?p=" + adSpaceExternalID //
                + "&dpid=" /* not defined, should be blanked out */
                + "&publisher_id=" + publisherID //
                + "&idfa=" + idfa //
                + "&adid=" /* not defined, should be blanked out */
                + "&c=" + creativeExternalID //
                + "&a=" + campaignExternalID //
                + "&ad=" + advertiserExternalID //
                + "&pu=" + publicationExternalID //
                + "&click=" + impression.getExternalID(); //

        String actual = MacroTractor.resolveMacros(url, publisherID, adSpaceExternalID, creativeExternalID, campaignExternalID, advertiserExternalID, publicationExternalID,
                impression, deviceIdentifierTypeIdsBySystemName, deviceProperties, false, null, true, null);

        //System.out.println(expected);
        //System.out.println(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testPostProcessIFA_ADID() {
        final String ifa = randomHexString(6);
        final String adid = randomHexString(6);

        Map<Long, String> secureDeviceIdentifiers = new HashMap<Long, String>() {
            {
                put(idfaDeviceIdentifierTypeId, ifa);
                put(adidDeviceIdentifierTypeId, adid);
            }
        };

        final String publisherID = randomAlphaNumericString(10);
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String campaignExternalID = randomAlphaNumericString(10);
        final String creativeExternalID = randomAlphaNumericString(10);
        final String advertiserExternalID = randomAlphaNumericString(10);
        final String publicationExternalID = randomAlphaNumericString(10);

        Impression impression = new Impression();
        impression.setDeviceIdentifiers(secureDeviceIdentifiers);

        String url = "http://whatever.com/foo" + "?p=%publication%" //
                + "&ifa=%ifa%" //
                + "&idfa=%idfa%" //
                + "&adid=%adid%" //
                + "&publisher_id=%publisher_id%" //
                + "&c=%creative%" //
                + "&a=%campaign%" //
                + "&ad=%advertiser%" //
                + "&pu=%pid%" //
                + "&click=%click%"; //
        String expected = "http://whatever.com/foo" //
                + "?p=" + adSpaceExternalID //
                + "&ifa=" + ifa //
                + "&idfa=" + ifa //
                + "&adid=" + adid //
                + "&publisher_id=" + publisherID //
                + "&c=" + creativeExternalID //
                + "&a=" + campaignExternalID //
                + "&ad=" + advertiserExternalID //
                + "&pu=" + publicationExternalID //
                + "&click=" + impression.getExternalID(); //

        // act
        String result = MacroTractor.resolveMacros(url, publisherID, adSpaceExternalID, creativeExternalID, campaignExternalID, advertiserExternalID, publicationExternalID,
                impression, deviceIdentifierTypeIdsBySystemName, deviceProperties, false, null, true, null);
        assertEquals(expected, result);
    }
}
