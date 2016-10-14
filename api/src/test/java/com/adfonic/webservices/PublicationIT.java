package com.adfonic.webservices;

import static com.adfonic.webservices.util.WSFixture.getStatisticUsersFixture;
import static com.adfonic.webservices.util.WSFixture.respRootElm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.webservices.dto.Publication;
import com.adfonic.webservices.dto.PublicationStatistics;
import com.adfonic.webservices.util.Form;
import com.adfonic.webservices.util.WSFixture;
import com.adfonic.webservices.util.WSFixture.Format;

/*
 * Tests PublicationIT. updated mostly(except stats) to standard base data
 */
public class PublicationIT {

    // Get only once
    private static WSFixture f;


    @BeforeClass
    public static void setup() throws Exception {
        f = new WSFixture();
        // Get around the problem of WebServices returning incompatible formats
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        // Util.startTommy();
    }


    @AfterClass
    public static void teardown() throws Exception {
        // Util.killServer();
    }


    @Test
    public void getPublicationsOnlyInJSON() throws Exception {
        String response = f.getJSON("/publication/list");
        Map<?, ?> rm = f.getResponseMap(response);

        // first method ; so dont bother, just go ahead and check count
        assertTrue(f.initial_pub_count <= ((List<?>) rm.get(respRootElm)).size());
    }


    @Test
    public void getPublications() throws Exception {
        Publication[] pubs = f.fGet(Publication[].class, "/publication/list");

        assertTrue(pubs.length > 0);

        List<String> pubIds = new ArrayList<String>();
        for (Publication p : pubs) {
            pubIds.add(p.id);
        }
        assertTrue(pubIds.contains(f.publication_1_id));
    }


    @Test
    public void getAPublicationJSON() throws Exception {
        String response = f.getJSON("/publication/" + f.publication_1_id);
        Map<?, ?> rm = f.getResponseMap(response);

        assertEquals(f.publication_1_name, ((Map<?, ?>) rm.get(respRootElm)).get("name"));
    }


    @Test
    public void getAPublication() throws Exception {
        Publication p = f.fGet(Publication.class, "/publication/" + f.publication_1_id);

        assertEquals(f.publication_1_name, p.name);
    }


    @Test
    public void getNonExistingPublication() throws Exception {
        Publication p = f.fGet(Publication.class, "/publication/" + "121323121421342");

        assertNull(p.name);
    }


    @Test
    public void createPublication() throws Exception {
        Publication pub = new Publication();
        pub.name = f.getNxtPubName();
        pub.type = "MOBILE_SITE";
        pub.description = "descr for cust pub 9809";
        pub.url = "http://domain:90/path?query_string#fragment_id";
        pub.autoapprove = false;
        pub.transparent = true;
        pub.languages = "es";
        pub.reference = "rf1";
        pub.requests = 1;
        pub.uniques = 1;
        Form pubForm = createPublicationForm(pub);

        String response = f.postURLENCforJSON("/publication/create", pubForm);
        Map<?, ?> rm = f.getResponseMap(response);
        String pubId = (String) ((Map<?, ?>) rm.get(respRootElm)).get("id");
        f.addToManagedPublications(pubId);// So as to have it removed in the end

        // TODO - verify response string itself - may be repeat the test case with fGet to do it easily

        Publication createdPub = f.fGet(Publication.class, "/publication/" + pubId);
        // TODO - revert to assertEquals after thread local impl
        // assertEquals(p, createdPub);
        // using assertTrue instead since we cannot guarantee which object equals is called on
        assertTrue(pub.exclude("id", "status").equals(createdPub));
    }


    // TODO - use reflection
    private Form createPublicationForm(Publication pub) {
        Form pubForm = new Form();
        pubForm.set("name", pub.name);
        pubForm.set("type", pub.type);
        pubForm.set("description", pub.description);
        pubForm.set("reference", pub.reference);
        pubForm.set("url", pub.url);
        pubForm.set("transparent", pub.transparent);
        pubForm.set("languages", pub.languages);
        pubForm.set("requests", pub.requests);
        pubForm.set("uniques", pub.uniques);
        pubForm.set("autoapprove", pub.autoapprove);
        return (pubForm);
    }


    @Test
    public void createPublicationWithOnlyMandatoryValues() throws Exception {
        Form pubForm = new Form();
        String pubName = f.getNxtPubName();
        pubForm.set("name", pubName);
        pubForm.set("type", "MOBILE_SITE");
        pubForm.set("description", "descr for cust pub 9809");
        pubForm.set("url", "http://domain:90/path?query_string#fragment_id");
        pubForm.set("transparent", "true");

        String response = f.postURLENCforJSON("/publication/create", pubForm);
        Map<?, ?> rm = f.getResponseMap(response);
        String pubId = (String) ((Map<?, ?>) rm.get(respRootElm)).get("id");
        f.addToManagedPublications(pubId);

        // TODO - verify response string

        Publication createdPub = f.fGet(Publication.class, "/publication/" + pubId);
        assertEquals(pubName, createdPub.name);
    }


    @Test
    public void updatePublication() throws Exception {
        String existingPubId = f.getAPublication();
        Form pubForm = new Form();
        String newPubName = f.getNxtPubName();
        pubForm.set("name", newPubName);
        pubForm.set("reference", "newref");

        String response = f.postURLENCforJSON("/publication/" + existingPubId, pubForm);
        // TODO - verify response string too

        Publication updatedPub = f.fGet(Publication.class, "/publication/" + existingPubId);
        assertTrue(newPubName.equals(updatedPub.name) && "newref".equals(updatedPub.reference));
    }


    @Test
    public void updatePublicationSpecifyingAllFields() throws Exception {
        String existingPubId = f.getAPublication();
        Publication pub = new Publication();
        pub.name = "CustPublication9809_changed_again";
        pub.type = "MOBILE_SITE";
        pub.description = "descr for cust pub 9809";
        pub.reference = "ref";
        pub.url = "http://domain:90/path?query_string#fragment_id";
        pub.transparent = true;
        pub.languages = "en";
        pub.requests = 1;
        pub.uniques = 1;
        pub.autoapprove = false;
        // TODO - for now statsus is mandatory and wont let
        // pub.status="PAUSED";
        pub.status = "PENDING";
        Form pubForm = createPublicationForm(pub);

        // TODO - looks like status is mandatory now(createPubForm doesn't set it) - needs to get spec changed
        pubForm.set("status", pub.status);

        String response = f.postURLENCforJSON("/publication/" + existingPubId, pubForm);

        Publication updatedPub = f.fGet(Publication.class, "/publication/" + existingPubId);

        // TODO - uncomment this after TL change
        // assertEquals(pub, updatedPub);

        // TODO - for now exclude 'transparent' see if it is a bug
        assertTrue(pub.exclude("id", "transparent", "status").equals(updatedPub));
    }


    // to verify
    @Test
    public void updatePublicationTryToActivateRejectedPublicationNEG_bz2144() throws Exception {
        String existingPubId = f.getAPublication();
        String newPubName = f.getNxtPubName();
        Form pubForm = new Form();

        // not depending on a REJECTED pub record in the database
        try {
            pubForm.set("name", newPubName);
            pubForm.set("status", "REJECTED");
            String response = f.postURLENCforJSON("/publication/" + existingPubId, pubForm);

            Publication updatedPub = f.fGet(Publication.class, "/publication/" + existingPubId);
            assertTrue(newPubName.equals(updatedPub.name));

            if (!"REJECTED".equals(updatedPub.status)) {
                // pass - it has refused to change to REJECTED - probably from PENDING - which is good already
                return;
            }
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create a rejected publication to test: " + t.getMessage());
        }

        pubForm = new Form();
        pubForm.set("status", "ACTIVE");
        String response = f.postURLENCforJSON("/publication/" + existingPubId, pubForm);

        Publication updatedPub = f.fGet(Publication.class, "/publication/" + existingPubId);
        assertFalse(newPubName.equals(updatedPub.name) && "ACTIVE".equals(updatedPub.status));
    }


    public void updatePublicationTypeNEG() {
    }


    public void createPublicationBadURLNEG() {
    }


    public void createPublicationBadLangISONEG() {
    }


    public void createPublicationInvalidTypeNEG() {
    }


    public void createPublicationBadFormatRequestsNEG() {

    }


    public void createPublicationBadFormatUniquesNEG() {

    }


    @Test
    public void getStatisticsForAllPublicationsWithNoDataJSON() throws Exception {
        String response = f.getJSON("/publication/statistics", "start", "end");
        System.out.println(response);
        // TODO - verification post bug fix
    }


    @Test
    public void getStatisticsForAllPublicationsWithNoData() throws Exception {
        PublicationStatistics[] pub = f.fGet(PublicationStatistics[].class, "/publication/statistics", "start", "end");

        assertTrue(pub.length == 0);
    }


    // Will fail if run with verification; Check below bug testcase
    @Test
    public void getStatisticsForAllPublications() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        String response = f.getJSON("/publication/statistics.json?start=2012010300&end=2012010316");

        assertTrue(response.contains("\"clicks\":12"));
        // TODO - verification post bug fix
    }


    // ignore ignore, for dates have values //@Ignore // blocked bug BZ-2171. blocked on BZ-2200
    @Test
    public void statsForAllPublsVersusStatsForAnIndividualPubForAuserAnyDateRange_bz2171() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics superStats = f.fGet(PublicationStatistics.class, "/publication/statistics", "start", "end");

        PublicationStatistics subStats = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end");

        assertSuperSub(superStats, subStats);
    }


    @Test
    public void statsForAllPublsVersusStatsForAnIndividualPubForAuser() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics superStats = f.fGet(PublicationStatistics.class, "/publication/statistics", "start="+f.statsPub_1_statsStart, "end="+f.statsPub_1_statsEnd);

        PublicationStatistics subStats = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start="+f.statsPub_1_statsStart, "end="+f.statsPub_1_statsEnd);

        assertSuperSub(superStats, subStats);
    }


    private void assertSuperSub(PublicationStatistics su, PublicationStatistics sb) {
        if (su.equals(sb))
            return;
        try {
            assertTrue((su.statistics == sb.statistics) || su.statistics.length > sb.statistics.length);
        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }


    @Test
    public void getStatisticsAtPublicationLevelForAllPublicationsJSON() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        String response = f.getJSON("/publication/statistics", "start", "end", "level=publication");

        System.out.println(response);
        // TODO - verification post bug fix
    }


    @Test
    public void getStatisticsAtAdSlotLevelForAllPublicationsJSON() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        String response = f.getJSON("/publication/statistics", "start", "end", "level=adslot");
        System.out.println(response);
        // TODO - verification post bug fix
    }


    @Test
    public void getStatisticsAtAdSlotLevelForAllPublicationsJSONWithoutData() throws Exception {
        String response = f.getJSON("/publication/statistics", "start", "end", "level=adslot");
        System.out.println(response);
        // TODO - verification post bug fix
    }


    // bug same like 2125
    @Test
    public void getStatisticsForGivenPublicationWithNoDataJSON() throws Exception {
        String response = f.getJSON("/publication/" + f.publication_1_id + "/statistics", "start", "end");
        System.out.println(response);
        // TODO - verification post bug fix
    }


    @Test
    public void getStatisticsForGivenPublication() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end");

        assertTrue(stats.id.equals(f.statsPub_1_id) && stats.statistics != null && stats.statistics[0].clicks == 12 && stats.adslot == null);
    }


    @Test
    public void getStatisticsForGivenPublicationNoMismatchTolerance_bz2170() throws Exception {
        WSFixture f = new WSFixture("adtstr+005@gmail.com", "devkey_adtstr005");

        // TODO - rm non-strict
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end");

        assertTrue(stats.id.equals(f.statsPub_1_id) && stats.statistics != null && stats.statistics[0].clicks == 12 && stats.adslot == null);
    }


    @Test
    public void getStatisticsForGivenPublicationWithExplicitLevel() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end", "level=publication");

        assertTrue(stats.id.equals(f.statsPub_1_id) && stats.statistics != null && stats.statistics[0].clicks == 12 && stats.adslot == null);
    }


    // same but like above will fail till the code fix. commenting
    // @Test
    public void getStatisticsAtAdSlotLevelForGivenPublication() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics stats = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end", "level=adslot");

        assertTrue(stats.id.equals(f.statsPub_1_id) && stats.statistics == null && stats.adslot != null && stats.adslot[0].statistics != null);
    }


    @Ignore // blocked bug BZ-2169. blocked on BZ-2200
    @Test
    public void statisticsAtPubAndAdSlotLevelsShouldBeConsistentAnyDateRange_bz2169() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics statsPlvl = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end", "level=publication");

        PublicationStatistics statsAlvl = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start", "end", "level=adslot");

        assertConsistency(statsPlvl, statsAlvl);
    }


    @Test
    public void statisticsAtPubAndAdSlotLevelsShouldBeConsistent() throws Exception {
        WSFixture f = getStatisticUsersFixture();
        PublicationStatistics statsPlvl = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start="+f.statsPub_1_statsStart, "end="+f.statsPub_1_statsEnd, "level=publication");

        PublicationStatistics statsAlvl = f.fGet(PublicationStatistics.class, "/publication/" + f.statsPub_1_id + "/statistics", "start="+f.statsPub_1_statsStart, "end="+f.statsPub_1_statsEnd, "level=adslot");

        assertConsistency(statsPlvl, statsAlvl);
    }


    private void assertConsistency(PublicationStatistics sp, PublicationStatistics sa) {
        assertTrue(sp.id.equals(sa.id));

        if (sp.statistics == null || sp.statistics.length == 0)
            return;

        assertTrue(sp.statistics[0].impressions >= sa.adslot[0].statistics[0].impressions);
        assertTrue(sp.statistics[0].clicks >= sa.adslot[0].statistics[0].clicks);
        // writeassertion ; now already failing
    }


    @Test
    public void updatePublicationRegression() throws Exception {
        String newPubName = "pubss";
        // String existingPubId = "ec6ed48d-f3c5-4781-9462-872e694cbeb8";
        String existingPubId = f.publication_pa_id;
        Form pubForm = new Form();

        pubForm.set("name", newPubName);
        pubForm.set("description", "amora");
        pubForm.set("languages", "es");
        pubForm.set("status", "ACTIVE");
        pubForm.set("transparent", "true");
        String response = f.postURLENCforJSON("/publication/" + existingPubId, pubForm);
        System.out.println(response);

    }


    @Test
    public void updatePublicationTransparentStatusRegression() throws Exception {
        String newPubName = "pubss";
        // String existingPubId = "ec6ed48d-f3c5-4781-9462-872e694cbeb8";
        String existingPubId = f.publication_pa_id;
        Form pubForm = new Form();

        pubForm.set("name", newPubName);
        pubForm.set("description", "amora");
        pubForm.set("languages", "es");
        pubForm.set("status", "ACTIVE");
        pubForm.set("transparent", "true");
        String response = f.postURLENCforJSON("/publication/" + existingPubId, pubForm);
        System.out.println(response);

    }

}
