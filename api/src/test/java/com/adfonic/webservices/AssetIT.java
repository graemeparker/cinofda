package com.adfonic.webservices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.webservices.util.WSFixture;
import com.adfonic.webservices.util.WSFixture.Format;

public class AssetIT {

    private static WSFixture f;


    @BeforeClass
    public static void setup() throws Exception {
        f = new WSFixture("adtstr+028@gmail.com", "devkey_adtstr028");
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        // Util.startTommy();
    }


    @AfterClass
    public static void teardown() throws Exception {
        // Util.killServer();
    }

    private Map<String, String> csMap = new HashMap<String, String>();
    {
        csMap.put("120x20", "MMA Small Image Banner");
        csMap.put("168x28", "MMA Medium Image Banner");
        csMap.put("216x36", "MMA Large Image Banner");
        csMap.put("320x50", "MMA XX-Large Image Banner");
        csMap.put("320x480", "Full page / 320 x 480");
    }


    public String createImageBanner(String creativeId, String dimension, int expectedStatus, String fmtName, String fmtExtension) throws Exception {
        String req = null, base64edTxt = asBase64String(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(dimension + "." + fmtExtension)));
        req = "<asset>"+
                "<creativeId>" + creativeId + "</creativeId>" +
                "<contentType>" + fmtName + "</contentType>" +
                "<data>" + base64edTxt + "</data>" +
                "<contentSpec>" + csMap.get(dimension) + "</contentSpec>" +
              "</asset>";
        String response = f.postXMLBodyForXML("/asset/create", req, expectedStatus);
        if (expectedStatus == 201) {
            f.verifyBasicContentInXMLResonse(response, "creativeId", creativeId);
        }
        return (response);
    }
    
    private String asBase64String(byte[] bs) throws IOException{
        return Base64.encodeBase64String(bs);
    }


    public String createGIFBanner(String creativeId, String dimension, int expectedStatus) throws Exception {
        return createImageBanner(creativeId, dimension, expectedStatus, "GIF", "gif");
    }


    @Test
    public void createImageAsset() throws Exception {
        String creativeId = "5710cf5e-f030-4475-af2e-c4def4b88584";
        createGIFBanner(creativeId, "120x20", 201);
    }


    @Ignore // tst data
    @Test
    public void createFullPageImageAsset() throws Exception {
        /*String req = null, name = "Banner Test Creative2";
        String destinationURL = "http://new.ad.creativecollection.ex";
        String campaignId="da2e6001-137d-40c1-b2d9-04b30494faa3";
        req = "<creative>" +
                "<campaignID>" + campaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>image320x480</format>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);

        */String creativeId = "50921f1a-9050-4093-8816-0b72f53e8e7b";
        createImageBanner(creativeId, "320x480", 201, "JPEG", "jpg");
    }


    @Ignore // impl
    @Test
    public void addLargeandXXLargeAssetsForAndroidCampaign() throws Exception {
        /*
        String androidCampaignId="5f3bef25-5dfd-4664-937b-7b7b040a1541";
        String req = null, name = "androidbanner";
        String destinationURL = "http://new.ad.assetcollection.ex";
        req = "<creative>" +
                "<campaignID>" + androidCampaignId + "</campaignID>" +
                "<name>" + name + "</name>" +
                "<destination>" +
                    "<type>URL</type>" +
                    "<data>" + destinationURL + "</data>" +
                "</destination>" +
                "<format>banner</format>" +
              "</creative>";
        String response = f.postXMLBodyForJSON("/creative/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
        */
        String creativeId = "d8f79929-b5d6-47fe-abbb-049711593fd1";
        createImageBanner(creativeId, "216x36", 201, "GIF", "gif");
        //createImageBanner(creativeId, "320x50", 201, "JPEG", "jpg");
    }


    public String createTextBasedAsset(String creativeId, String contentSpec, String text, int expectedStatus) throws Exception {
        String req = null, base64edTxt;
        base64edTxt = asBase64String(text.getBytes());
        req = "<asset>" +
                "<creativeId>" + creativeId + "</creativeId>" +
                "<contentType>text/plain</contentType>" +
                "<data>" + base64edTxt + "</data>" +
                "<contentSpec>" + contentSpec + "</contentSpec>" +
              "</asset>";
        String response = f.postXMLBodyForXML("/asset/create", req, expectedStatus);
        if (expectedStatus == 201) {
            f.verifyBasicContentInXMLResonse(response, "creativeId", creativeId);
            f.verifyBasicContentInXMLResonse(response, "data", base64edTxt);
        }
        return (response);
    }


    @Ignore // permanently  - taglines are a thing of the past now
    @Test
    public void createTagLineAsset() throws Exception {
        String creativeId = "5710cf5e-f030-4475-af2e-c4def4b88584";
        createTextBasedAsset(creativeId, "MMA Small Text", "sml taglin", 201);
    }


    @Test
    public void attemptRepeatedCreationOfSimilarAssetNEG() throws Exception {
        String creativeId = "5710cf5e-f030-4475-af2e-c4def4b88584";
        createGIFBanner(creativeId, "168x28", 201);
        createGIFBanner(creativeId, "168x28", WSFixture.CODE_FORBDN);
    }


    @Test
    public void getAsset() throws Exception {
        String existinAssetID = "002a84c2-7295-47fb-97db-3236d6028038";
        String existinAssets_creativeID = "efa8137a-8aca-42d8-b88b-3b65125bf38e";
        String response = f.getJSON("/asset/" + existinAssetID);
        f.verifyBasicContentInXMLResonse(response, "creativeId", existinAssets_creativeID);
    }


    @Ignore // need tst data
    @Test
    public void getAnAsset() throws Exception {
        String existinAssetID = "238b0b25-9483-4ef0-8034-664036c950a4";
        String response = f.getJSON("/asset/" + existinAssetID);
        System.out.println(response);
        f.delete("/asset/" + existinAssetID, Format.JSON, 204);
    }


    @Test
    public void attemptCreationOfTagLineAssetInTextLinkCreativeNEG() throws Exception {
        String creativeId = "efa8137a-8aca-42d8-b88b-3b65125bf38e";
        createTextBasedAsset(creativeId, "MMA Small Text", "tagline", WSFixture.CODE_FORBDN);
    }


    @Test
    public void updateAsset() throws Exception {
        // Marker - update operation removed
    }


    @Test
    public void deleteAsset() throws Exception {
        String existinAssetID = "165bbe1c-bb47-440a-b24f-3916668886be";
        f.delete("/asset/" + existinAssetID, Format.JSON, 204);
    }


    @Test
    public void createAssetInActiveCreative() throws Exception {
        String creativeId = "4c25e13c-4037-4a95-b515-6328df88dc49";
        createGIFBanner(creativeId, "120x20", 201);
        String response = f.getJSON("/creative/" + creativeId);
        System.out.println(response); // TODO - instead verify with xpath the status is PENDING
    }


    @Ignore // need tes-data fixin no asset-bundle
    @Test
    public void deleteAssetFromActiveCreative() throws Exception {
        String existinAssetID = "594ed149-36bb-498a-8c77-f583bb8dd52c";
        String existinAssets_creativeId = "f05cf07a-18f9-4774-8352-0eca9c077a07";
        f.delete("/asset/" + existinAssetID, Format.JSON, 204);
        String response = f.getJSON("/creative/" + existinAssets_creativeId);
        System.out.println(response); // TODO - instead verify with xpath the status is PENDING
    }


    @Test
    public void tryDeletingFinalAssetFromActiveCreativeNEG() throws Exception {
        String existinAssetID = "9fea7f63-ade2-4a1e-9e8b-d04689682141";
        f.delete("/asset/" + existinAssetID, Format.JSON, WSFixture.CODE_FORBDN);
    }

}
