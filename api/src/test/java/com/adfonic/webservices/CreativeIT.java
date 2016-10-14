package com.adfonic.webservices;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.webservices.util.WSFixture;
import com.adfonic.webservices.util.WSFixture.Format;

public class CreativeIT {

    private static WSFixture f;


    @BeforeClass
    public static void setup() throws Exception {
        f = new WSFixture("adtstr+027@gmail.com", "devkey_adtstr027");
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        // Util.startTommy();
    }


    @AfterClass
    public static void teardown() throws Exception {
        // Util.killServer();
    }

    String campaignId = "872e663e-36de-4320-9ad7-c72c3a7ad267";


    @Test
    public void createCreativeInXMLWithExistingDestination() throws Exception {
        String req = null;
        String destinationURL = "http://ad.creativecollection.ex";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>Banner Test Creative1</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>banner</format>" +
              "</creative>";
        String response = f.postXMLBodyForXML("/creative/create", req, 201);
        f.verifyBasicContentInXMLResonse(response, "format", "banner");
    }


    @Test
    public void createCreativeInXMLForJSON() throws Exception {
        String req = null, name = "Banner Test Creative2";
        String destinationURL = "http://new.ad.creativecollection.ex";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>banner</format>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
    }


    @Test
    public void resishCreateTextLinkCreativeWithClickToCall() throws Exception {
        String req = null;
        String phNo="+4490889176";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>Text Creative 1</name>" +
                "<destination>" +
                    "<type>CALL</type>" +
                    "<data>" + phNo + "</data>" +
                "</destination>" +
                "<format>text</format>" +
              "</creative>";
        String response = f.postXMLBodyForXML("/creative", req, 201);
        f.verifyBasicContentInXMLResonse(response, "data", phNo);
    }


    @Test
    public void attemptToCreateCreativesWithOutDestinationNEG() throws Exception {
        String req = null, name = "Banner Test Creative3";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<format>banner</format>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, WSFixture.CODE_INPUT_MISSIN);
        f.verifyBasicContentInJSONResonse(response, "code", ErrorCode.VALIDATION_ERROR);
    }


    @Test
    public void attemptToCreateCreativesWithOutFormatNEG() throws Exception {
        String req = null, name = "Banner Test Creative4";
        String destinationURL = "http://ad.creativecollection.ex";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, WSFixture.CODE_INPUT_MISSIN);
        f.verifyBasicContentInJSONResonse(response, "code", ErrorCode.VALIDATION_ERROR);
    }


    @Test
    public void attemptToCreateTwoCreativesWithSameNameForAcampaignNEG() throws Exception {
        String req = null, name = "Banner Test Creative6";
        String destinationURL = "http://ad.creativecollection.ex";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>banner</format>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
        response = f.postXMLBodyForJSON("/creative/create", req, WSFixture.CODE_VALIDN);
        f.verifyBasicContentInJSONResonse(response, "code", ErrorCode.VALIDATION_ERROR);
    }


    @Test
    public void explicitCheckForlastUpdatedValue() throws Exception {
        String req = null, name = "Banner Test Creative8";
        String destinationURL = "http://ad.creativecollection.ex";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>banner</format>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, 201);
        Assert.assertTrue(response.contains("lastUpdated")); // TODO - put proper verification matching value to 5 sec approx; repeate verification in one of the update tests
    }


    @Ignore // tst data
    @Test
    public void tryAddingCreativeWithoutTranslationForNonEnglishCampaign() throws Exception {
        String nonEnglishCampaignID="af5e3060-ad17-419d-9157-99f0930f0771";
        String req = null, name = "Banner Test Creative9";
        String destinationURL = "http://new.ad.creativecollection.ex";
        req = "<creative>" +
                "<campaignID>" + nonEnglishCampaignID + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>banner</format>" +
              "</creative>";
        f.postXMLBodyForJSON("/creative/create", req, WSFixture.CODE_VALIDN);
    }


    @Test
    public void getCreative() throws Exception {
        String existingCreativeID = "c6a0d8df-f3f9-4bf2-b55f-9f0b20f8da23";
        String existingCreative_name = "bannerCreative_1";
        String response = f.getJSON("/creative/" + existingCreativeID);
        f.verifyBasicContentInJSONResonse(response, "name", existingCreative_name);
    }


    @Test
    public void updateCreativeChangeDestTypeItself() throws Exception {
        String existingCreativeID = "04820a0a-fcdc-401a-82f8-8dd021646e08";
        String req = null;
        String phNo = "+4490889177";
        req = "<creative>" +
                "<destination>" +
                    "<type>CALL</type>" +
                    "<data>" + phNo + "</data>" +
                "</destination>" +
                "<format>text</format>" +
              "</creative>" + "";
        String response=f.putXMLBodyForXML("/creative/" + existingCreativeID, req, 200);
        f.verifyBasicContentInXMLResonse(response, "destination.data", phNo); // TODO - implement hierarchial matching?
    }


    @Test
    public void attemptToUpdateAcreativsFormatNEG() throws Exception {
        String existingCreativeID = "04820a0a-fcdc-401a-82f8-8dd021646e08";
        String req = null;
        req = "<creative>" +
                "<format>banner</format>" +
              "</creative>" + "";
        String response=f.putXMLBodyForXML("/creative/" + existingCreativeID, req, 200);
        f.verifyBasicContentInXMLResonse(response, "format", "text");
    }


    @Test
    public void updateCreativMinusDestination() throws Exception {
        String existingCreativeID = "25a1c1cf-a820-461a-be98-85d26d7bd8cd";
        String existingCreative_name = "updateme_smallbanner_2";
        String req = null;
        req = "<creative>" +
                "<name>New Name</name>" +
                "<format>text</format>" +
                "<englishTranslation>should not matter</englishTranslation>" + // TODO - test for english Translation on non english campaign
              "</creative>";
        String response=f.putXMLBodyForXML("/creative/" + existingCreativeID, req, 200);
        f.verifyBasicContentInXMLResonse(response, "name", existingCreative_name);
    }


    @Test
    public void deleteCreative() throws Exception {
        String existingCreativeID = "4677ca42-1ded-48ed-b3a7-a9b4aa52e97b";
        f.delete("/creative/" + existingCreativeID, Format.JSON, 204);
    }


    @Ignore // add data
    @Test
    public void tryToDeleteSubmittedCreativeNEG() throws Exception {
        String existingCreativeID = "2a2abdb4-68b9-476c-9cc5-1313ab60af08";
        f.delete("/creative/" + existingCreativeID, Format.JSON, WSFixture.CODE_FORBDN);
    }


    @Test
    public void startPausedCreative() throws Exception {
        String creativeId = "b72e22a6-9db5-4a70-a1a5-5a768ce21c9a";
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=start", null, Format.ABSENT, Format.ABSENT, 204);
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=start", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_FORBDN);
    }


    @Test
    public void pauseActiveCreative() throws Exception {
        String creativeId = "9fe85f4e-e71e-4617-93d3-c62d7cce80a4";
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=pause", null, Format.ABSENT, Format.ABSENT, 204);
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=pause", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_FORBDN);
    }


    @Test
    public void submitNewCreative() throws Exception {
        String creativeId = "d23b9151-91bc-46c6-933b-30a1fbc8e724";
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, 204);
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_FORBDN);
    }


    @Test
    public void stopActiveCreative() throws Exception {
        String creativeId = "9af50f23-bd0a-4fe3-9c22-d306eee78b72";
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=stop", null, Format.ABSENT, Format.ABSENT, 204);
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=stop", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_FORBDN);
    }


    @Test
    public void tryStartingStoppedCreativeNEG() throws Exception {
        String creativeId = "641021bb-11c8-419c-b6b5-01fd64783e9f";
        f.postBodyFmtSpecd("/creative/" + creativeId + ".xml?command=start", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_FORBDN);
    }

}
