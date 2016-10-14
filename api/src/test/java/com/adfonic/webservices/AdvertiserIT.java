package com.adfonic.webservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.webservices.dto.Campaign;
import com.adfonic.webservices.dto.CampaignStatistics;
import com.adfonic.webservices.util.WSFixture;
import com.adfonic.webservices.util.WSFixture.Format;

/**
 * Basic Integration tests for advertiser ws With the typical JSON parsing, the casting is unsafe and it is up to the runtime exceptions to err the test result
 * 
 * Will either need to define domain classes or be changed for a more dynamic style
 * 
 */
public class AdvertiserIT {

    // Get only once
    private static WSFixture f;


    @BeforeClass
    public static void setup() throws Exception {
        f = new WSFixture();

        // Util.startTommy();
    }


    @AfterClass
    public static void teardown() throws Exception {
        // Util.killServer();
    }

    //protected String userEmail = "tatyanaisoft@gmail.com";
    //protected String userDeveloperKey = "thisismykey";
    protected String userEmail = "adtstr+026@gmail.com";
    protected String userDeveloperKey = "devkey_adtstr026";
    
    protected String wrongUserDeveloperKey = "__thisismykey__";
    protected String authorizedAdvertiserId = "66607e82-f91f-47eb-a151-b8bea5b7cff1";
    protected String unauthorizedAdvertiserId = "f7100d28-10b8-444e-ac11-4828e67caeae";
    protected String authorizedCampaignId = "dbcd8fbb-de26-4c6f-8067-8c692aac93d8";
    protected String unauthorizedCampaignId = "872e663e-36de-4320-9ad7-c72c3a7ad267";

    String respRootElm = "masg-response";
    String errRootElm = "masg-error";


    private Map<?, ?> getResponseMap(String response) throws JsonParseException, JsonMappingException, IOException {
        return (new ObjectMapper().readValue(response, Map.class));
    }


    private Map<?, ?> getResponseMapFromXml(String response) throws JsonParseException, JsonMappingException, IOException {
        // new org.json.XML - missing - do i want to add yet another java json parser
        return (new ObjectMapper().readValue(response, Map.class));
    }


    @Test
    public void getPeerAdvertisers() throws Exception {
        String response = f.get("/advertisers/list", Format.JSON, userEmail, userDeveloperKey);
        assertTrue("Expected advertiser missing", response.contains(authorizedAdvertiserId));
        // TODO - better verification with new f/w - maybe
    }


    // @Ignore //BUG-2114 - depends on 2126
    @Test
    public void getPeerAdvertisersXMLFMT_bz2114() throws Exception {
        String response = f.get("/advertisers/list", Format.XML, userEmail, userDeveloperKey);
        assertTrue("Expected advertiser missing", response.contains(authorizedAdvertiserId));
        // TODO - better verification with new f/w - maybe
    }


    @Test
    public void testAuthorisedUserGetAdvertiser() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId, Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(authorizedAdvertiserId, ((Map<?, ?>) rm.get(respRootElm)).get("id"));
    }


    @Test
    public void testAuthorisedUserGetAdvertiserXMLFMT() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId, Format.XML, userEmail, userDeveloperKey);
        System.out.println(response);
        // write XML to JSON and do verification - or bifurcate and do both for everything
    }


    @Test
    public void useUnAuthorisedUserToFetchAdvertiserNEG() throws Exception {
        String response = f.get("/advertiser/" + unauthorizedAdvertiserId, Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(1600, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    // Fixed @Ignore // Bugzilla 2112
    @Test
    public void useUnAuthorisedUserToFetchAdvertiserTestDescriptionNEG_bz2112() throws Exception {
        String response = f.get("/advertiser/" + unauthorizedAdvertiserId, Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        String errorDescription = (String) ((Map<?, ?>) rm.get(errRootElm)).get("description");
        assertFalse("Should not talk about campaign here :[" + errorDescription + "]", errorDescription.contains("ampaign"));
    }


    @Test
    public void getCampainsForGivenAdvertiser() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId + "/campaigns/list", Format.JSON, userEmail, userDeveloperKey);

        // minimal raw verification
        assertTrue(response.contains(authorizedCampaignId));

        Map<?, ?> rm = getResponseMap(response);
        /* will change the json deser anyway - ref class comments */
        @SuppressWarnings("unchecked")
        List<Map<?, ?>> campaigns = (List<Map<?, ?>>) rm.get(respRootElm);
        boolean pass = false;
        for (Map<?, ?> campaign : campaigns) {
            if (((String) campaign.get("id")).equals(authorizedCampaignId)) {
                pass = true;
            }
        }
        if (!pass) {
            fail();
        }
    }


    // @Ignore //BUG-2113 - depends on 2126
    @Test
    public void getCampaignsForGivenAdvertiserXMLFMT_bz2113() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId + "/campaigns/list", Format.XML, userEmail, userDeveloperKey);

        // minimal raw verification
        assertTrue(response.contains(authorizedCampaignId));

        // TODO - write more after fixing - may be
    }


    @Ignore //till Accept - application/json workaround is no more needed
    @Test
    public void testAuthorisedUserGetCampaign() throws Exception {
        String response = f.get("/campaign/" + authorizedCampaignId, Format.JSON, userEmail, userDeveloperKey);
        System.out.println("*******************\n" + response);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(authorizedCampaignId, ((Map<?, ?>) rm.get(respRootElm)).get("id"));
    }


    @Test
    public void useUnAuthorisedUserToFetchCampaignNEG() throws Exception {
        String response = f.get("/campaign/" + unauthorizedCampaignId, Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(2000, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    // @Ignore - may be have one more version w/o data
    @Test
    public void getStatisticsForGivenAdvertiser() throws Exception {
        String s = "2011043112";
        String e = "2011060712";
        String url = f.buildUrl("/advertiser/" + authorizedAdvertiserId + "/statistics", Format.JSON);
        url = url + "?start=" + s + "&end=" + e;
        String response = f.get(url, userEmail, userDeveloperKey);
        System.out.println(response);
        // set data and complete verification
    }


    @Test
    public void testAdvertiserWithOutAuthHeaderNEG() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId, Format.JSON, null, null);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(ErrorCode.AUTH_NO_AUTHORIZATION, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    @Test
    public void tryToTestAdvertiserWithBadAuthNEG() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId, Format.JSON, userEmail, null);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(ErrorCode.AUTH_INVALID_AUTHORIZATION, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    @Test
    public void testAdvertiserWithInvalidEmailNEG() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId, Format.JSON, "someemailnon3xtnt@adfonic.com", "doesntmatter");
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(ErrorCode.AUTH_INVALID_EMAIL, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    @Test
    public void testAdvertiserWithInvalidDevKeyNEG() throws Exception {
        String response = f.get("/advertiser/" + authorizedAdvertiserId, Format.JSON, userEmail, "invalofc0urs3");
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(ErrorCode.AUTH_INVALID_DEVELOPER_KEY, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    /*
     * @Test public void testGetAdvertiser2Requests()throws Exception{ String url = buildUrl("/advertiser/" + authorizedAdvertiserId, Format.JSON); String response = Util.getWebServiceResponseTwice(url, userEmail, userDeveloperKey); Map<?,?> rm=getResponseMap(response); assertEquals(authorizedAdvertiserId, ((Map<?,?>)rm.get(respRootElm)).get("id")); }
     */

    @Test
    public void testGetNonExistingCampaign() throws Exception {
        String response = f.get("/campaign/" + "cannotexistcampaignid908", Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(ErrorCode.ENTITY_NOT_FOUND, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    @Test
    public void testGetNonExistingAdvertiser() throws Exception {
        String response = f.get("/advertiser/" + "cannotExistAdvertiser908", Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(ErrorCode.ENTITY_NOT_FOUND, ((Map<?, ?>) rm.get(errRootElm)).get("code"));
    }


    // @Ignore //BUG-2125 - depends on 2126
    @Test
    public void getStatisticsForGivenCampaignWithNoDataJSON_bz2125() throws Exception {
        String s = "2011043112";
        String e = "2011060712";
        String url = f.buildUrl("/campaign/" + authorizedCampaignId + "/statistics", Format.JSON);
        url = url + "?start=" + s + "&end=" + e;
        String response = f.get(url, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertNull(rm.get(errRootElm));
        assertNotNull(rm.get(respRootElm));
    }


    // @Ignore //BUG-2126 - blocks 2113, 2114, 2125
    @Test
    public void emptyCampaignStatsWouldReturnInExpectedFormat_bz2126() throws Exception {
        String s = "2011043112";
        String e = "2011060712";
        String url = f.buildUrl("/campaign/" + authorizedCampaignId + "/statistics", Format.JSON);
        url = url + "?start=" + s + "&end=" + e;
        String response = f.get(url, userEmail, userDeveloperKey);
        try {
            getResponseMap(response);
        } catch (Exception ex) {
            fail("Unable to parse response response: " + response);
        }
    }


    @Test
    public void getStatisticsForGivenCampaignWithNoDataTolerantMode() throws Exception {
        WSFixture f = new WSFixture(userEmail, userDeveloperKey);
        f.relaxComparisons().specifyCandidateFormat(Format.XML);

        CampaignStatistics stats = getCampaignStats(f);

        // make sure it returned no data
        assertNull(stats.id);
        System.out.println(stats);
    }


    // will pass after fixing 2125, 2126
    @Test
    public void getStatisticsForGivenCampaignWithNoDataNoTolerance() throws Exception {
        WSFixture f = new WSFixture(userEmail, userDeveloperKey);

        CampaignStatistics stats = getCampaignStats(f);

        // make sure it returned no data
        assertNull(stats.id);
        System.out.println(stats);
    }


    private CampaignStatistics getCampaignStats(WSFixture f) throws Exception {
        String s = "2011043112";
        String e = "2011060712";
        return (f.fGet(CampaignStatistics.class, "/campaign/" + authorizedCampaignId + "/statistics", "start=" + s, "end=" + e));

    }


    // @Ignore
    // same as above but make sure there is data
    @Test
    public void getStatisticsForGivenCampaignJSON() throws Exception {
        String s = "2011043112";
        String e = "2011102412";
        String url = f.buildUrl("/campaign/" + statsCampId + "/statistics", Format.JSON);
        url = url + "?start=" + s + "&end=" + e;
        // String url = buildUrl(rtiser/" + "e7276bdd-15fe-43cd-881b-4cd235b0dc5b" + "/campaigns/list", Format.JSON);
        String response = f.get(url, "olivier.voute@booking.com", "some");
        System.out.println(response);
        // set data and complete verification
    }

    private String statsCampId = "36d95115-02d0-4ec1-bfb4-00a8cbb58347";


    @Test
    public void getStatisticsForGivenCampaignInXMLcausesParserBreakDueToElementNameWithSpaces_bz2193() throws Exception {
        String s = "2012010300";
        String e = "2012010300";
        WSFixture f = new WSFixture("adtstr+025@gmail.com", "devkey_adtstr025");
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        CampaignStatistics stats = f.fGet(CampaignStatistics.class, "/campaign/" + statsCampId + "/statistics", "start=" + s, "end=" + e);

        assertNotNull(stats);
        assumeNotNull(stats.id);
        assertTrue(stats.id.equals(statsCampId));
        // set data and complete verification
    }


    @Ignore  //format mismatch is known issue - see jira BZ-2170 for details  
    // TODO - supplement to 2170
    @Test
    public void getStatisticsForGivenCampaignXMLvsJSONMisMatch_bz2170_supplement() throws Exception {
        String s = "2012010300";
        String e = "2012010300";
        WSFixture f = new WSFixture("adtstr+025@gmail.com", "devkey_adtstr025");
        // f.relaxComparisons().specifyCandidateFormat(XML_FORMAT);
        CampaignStatistics stats = f.fGet(CampaignStatistics.class, "/campaign/" + statsCampId + "/statistics", "start=" + s, "end=" + e);

        // System.out.println(stats);
    }


    @Test
    public void testAuthorisedUserGetCampaignInXMLFMT() throws Exception {// replicating existing test in XML format w newer api
        WSFixture f = new WSFixture(userEmail, userDeveloperKey);
        Campaign campaign = f.getTer(Format.XML).get(Campaign.class, "/campaign/" + authorizedCampaignId);

        assertEquals(authorizedCampaignId, campaign.id);
        System.out.println(campaign);
    }

}
