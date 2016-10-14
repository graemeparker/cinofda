package com.adfonic.webservices;

import static com.adfonic.webservices.util.WSFixture.getStatisticUsersFixture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.adfonic.webservices.dto.AdSlot;
import com.adfonic.webservices.dto.PublicationStatistics;
import com.adfonic.webservices.util.Form;
import com.adfonic.webservices.util.WSFixture;
import com.adfonic.webservices.util.WSFixture.Format;

/*
 * Quick Ad Slot tests. static data
 *
 * Create a base fixture and put all hostName, userEmail etc in there
 */
public class AdSlotIT {

    // Get only once
    private static WSFixture f;


    @BeforeClass
    public static void setup() throws Exception {
        f = new WSFixture(userEmail, userDeveloperKey);
        // Util.startTommy();
    }


    @AfterClass
    public static void teardown() throws Exception {
        // Util.killServer();
    }

    protected static String userEmail = "adtstr+003@gmail.com";
    protected static String userDeveloperKey = "devkey_adtstr003";

    String existingPublicationId_p = "70675f3d-ed04-49b9-8f31-87a91bb32090";
    String existingAdSlotId_p_a = "79bd83eb-6e44-47eb-81f2-2c783e434bb5";
    String existingAdSlotName_p_a = "PubCoActive_MobileWeb1_AdSlot1";

    String respRootElm = "masg-response";
    String errRootElm = "masg-error";


    private Map<?, ?> getResponseMap(String response) throws JsonParseException, JsonMappingException, IOException {
        return (new ObjectMapper().readValue(response, Map.class));
    }


    @Test
    public void getAdSlots() throws Exception {
        String response = f.get("/adslot/list", Format.JSON, userEmail, userDeveloperKey);
        // raw verification
        assertTrue(response.contains(existingAdSlotId_p_a));

        Map<?, ?> rm = getResponseMap(response);
        assertFalse("Expected atleast the starting count", 1 > ((List<?>) rm.get(respRootElm)).size());
    }


    @Test
    public void getAnAdSlot() throws Exception {
        String response = f.get("/adslot/" + existingAdSlotId_p_a, Format.JSON, userEmail, userDeveloperKey);
        Map<?, ?> rm = getResponseMap(response);
        assertEquals(existingAdSlotName_p_a, ((Map<?, ?>) rm.get(respRootElm)).get("name"));
    }


    @Test
    public void createAdSlot() throws Exception {
        Form adSlot = new Form();
        adSlot.set("publication", existingPublicationId_p);
        adSlot.set("name", "CustAdSlot9809_1");
        adSlot.set("formats", "banner,text");
        String url = f.buildUrl("/adslot/create", Format.JSON);
        String response = f.postForm(url, userEmail, userDeveloperKey, adSlot);
        // raw verification
        assertTrue(response.contains("CustAdSlot9809_1"));
        Map<?, ?> rm = getResponseMap(response);
        custAdSlotId = (String) ((Map<?, ?>) rm.get(respRootElm)).get("id");
        // TODO - remove assignment to static; use new f/w. more verif
    }

    // for now
    static String custAdSlotId = null;


    public void createSecondAdSlotForSamePublication() throws Exception {

    }


    @Test
    public void updateAdSlot() throws Exception {
        Form adSlot = new Form();
        String newName = existingAdSlotName_p_a + "Changed";
        adSlot.set("name", newName);
        adSlot.set("formats", "banner");
        String url = f.buildUrl("/adslot/" + existingAdSlotId_p_a, Format.JSON);
        String response = f.postForm(url, userEmail, userDeveloperKey, adSlot);
        // raw verification
        assertTrue(response.contains(newName));

        adSlot = new Form();
        adSlot.set("name", existingAdSlotName_p_a);
        url = f.buildUrl("/adslot/" + existingAdSlotId_p_a, Format.JSON);
        response = f.postForm(url, userEmail, userDeveloperKey, adSlot);
        // raw verification
        assertTrue(response.contains(existingAdSlotName_p_a));

        // TODO - more verif
    }


    public void updateAdSlotBySpecifyingNoParams() {
    }


    public void updateAdSlotSpecifyPubIdNEG() {
    }


    public void createAdSlotWithMadeUpFormatsNEG() throws Exception {
    }


    public void createAdSlotWithDuplicatedFormatsNEG() throws Exception {
    }


    public void createAdSlotWithBadlySeparatedFormatsNEG() throws Exception {
    }


    public void createAdSlotWithNoFormatsSpecifiedNEG() throws Exception {
    }


    public void createAdSlotWithTooLargeATagNEG() throws Exception {
    }


    public void createAdSlotWithNonExistingPublicationIdNEG() throws Exception {
    }


    @Test
    public void getStatisticsForGivenAdSlot() throws Exception {
        String adslotId = "e8d37632-5584-45a1-abec-f6f3eee1cc1d";
        WSFixture f = getStatisticUsersFixture();

        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/adslot/" + adslotId + "/statistics", "start", "end");

        assertTrue(stats.id.equals(adslotId) && stats.statistics != null && stats.statistics[0] != null && stats.statistics[0].requests == 23);
    }


    // TODO - raise bug ? - it should throw error . also now it is either 200/400 which is wrong.. error handling
    @Test
    public void getStatisticsForGivenAdSlotWithNonExistantDates() throws Exception {
        String s = "2011043112";
        String e = "2011060712";
        String adslotId = "79bd83eb-6e44-47eb-81f2-2c783e434bb5";
        WSFixture f = getStatisticUsersFixture();

        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/adslot/" + adslotId + "/statistics", "start=" + s, "end=" + e);

        assertTrue(stats.statistics == null);
    }


    // Bugzilla 2057
    // "20110430" is unparsable according to the patterns set in the controller. Commenting out @Test
    //@Test
    public void getStatisticsForGivenAdSlotWithBadDateFormat_bz2057() throws Exception {
        String s = "20110430";
        String e = "2011060712";
        String adslotId = "e8d37632-5584-45a1-abec-f6f3eee1cc1d";
        WSFixture f = getStatisticUsersFixture();

        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/adslot/" + adslotId + "/statistics", "start=" + s, "end=" + e);

        assertTrue(stats.code >= 0 && stats.description.contains(s));
    }


    @Test
    public void getAdSlotsInXMLFMT() throws Exception {// replicating existing test in XML format
        AdSlot[] slots = f.getTer(Format.XML).get(AdSlot[].class, "/adslot/list");

        assertFalse("Expected atleast the starting count", 1 > slots.length);
        for (AdSlot slot : slots) {
            if (existingAdSlotId_p_a.equals(slot.id)) {
                return;
            }
        }
        fail("Could not find adslot:" + existingAdSlotId_p_a + " in list");
    }


    @Test
    public void getAnAdSlotInXMLFMT() throws Exception {// replicating existing test in XML format
        AdSlot slot = f.getTer(Format.XML).get(AdSlot.class, "/adslot/" + existingAdSlotId_p_a);

        System.out.println(slot);
        assertEquals(existingAdSlotName_p_a, slot.name);
    }


    @Test
    public void getAdSlotsForAPublication() throws Exception {
        AdSlot[] slots = f.getTer(Format.XML).get(AdSlot[].class, "/adslot/list", "publication=" + existingPublicationId_p);

        for (AdSlot slot : slots) {
            if (existingAdSlotId_p_a.equals(slot.id)) {
                return;
            }
        }
        fail("Could not find adslot:" + existingAdSlotId_p_a + " in list");
    }


    // That publication ID does not exist - commenting out the @Test
    //@Test
    public void createAnAdSlotBUG() throws Exception {
        Form adSlot = new Form();
        adSlot.set("publication", "851ac861-8b95-4904-a7eb-bf5893c1de36");
        adSlot.set("name", "CustAdSlot9809_1");
        adSlot.set("formats", "banner,text");
        String response = f.postURLENCforJSON("/adslot/create", adSlot);
        System.out.println(response);
    }

}
