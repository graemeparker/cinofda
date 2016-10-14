package com.adfonic.webservices;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adfonic.webservices.util.WSFixture;
import com.adfonic.webservices.util.WSFixture.Format;


public class CampaignIT {

    // Get only once
    private static WSFixture f;


    @BeforeClass
    public static void setup() throws Exception {
        f = new WSFixture("adtstr+026@gmail.com", "devkey_adtstr026");
    	
        f.relaxComparisons().specifyCandidateFormat(Format.XML);
        //Util.startTommy();
    }


    @AfterClass
    public static void teardown() throws Exception {
        // Util.killServer();
    }


    @Test
    public void createCampaigninJSON() throws Exception {
        String req =  "{ \"name\":\"Test Camp2\", " +
                        "\"description\":\"usual description\", " +
                        "\"bid\":{ " +
                            "\"type\":\"CPC\", " +
                            "\"amount\":\"2\" " +
                        "}" +
                      "}";
        String response = f.postJSONBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", "Test Camp2");
    }


    @Test
    public void trycreateCampaigninJSONWithoutBidNEG() throws Exception {
        String req =  "{ \"name\":\"Test Camp3\", " +
                        "\"description\":\"usual but with bid\"" +
                      "}";
        String response = f.postJSONBodyForJSON("/campaign/create", req, WSFixture.CODE_INPUT_MISSIN);
        f.verifyBasicContentInJSONResonse(response, "code", ErrorCode.VALIDATION_ERROR);
    }


    @Test
    public void createCampaigninXML() throws Exception {
        createCampaigninXML("t3campaign");
    }


    public void createCampaigninXML(String name) throws Exception {
        String req = null;
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created via XML " + name + "</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
    }


    @Test
    public void createNonEnglishCampaign() throws Exception {
        String req = null, name="Test Campaign 129";
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>french campaign</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>3.00</amount>" +
                    "</bid>" +
                    "<defaultLanguage>fr</defaultLanguage>" +
                "</campaign>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
    }


    @Test
    public void createCampaigninWithEmptySegments() throws Exception {
        String req = null, name="TestCampaign4";
        // TODO - impl filter to validate and remove outer element
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created via XML TC4</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                    "<segments></segments>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
        f.verifyBasicContentInJSONResonse(response, "daysOfWeek", "127");
    }


    @Test
    public void createCampaigninWithNonEmptySegments() throws Exception {
        String req = null, description="Created via XML non empty seg";
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>Test CampaignABC</name>" +
                    "<description>"+description+"</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                    "<segment>" +
                        "<daysOfWeek>3</daysOfWeek> " +
                    "</segment>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "description", description);
    }


    @Test
    public void restishCreateCampaigninJSON() throws Exception {
        String name="Test Camp2zz";
        String req =  "{ \"name\":\""+name+"\", " +
                "\"description\":\"usual description ye\", " +
                "\"bid\":{ " +
                    "\"type\":\"CPC\", " +
                    "\"amount\":\"2.9\" " +
                "}" +
              "}";
        String response = f.postJSONBodyForXML("/campaign", req, 201);
        f.verifyBasicContentInXMLResonse(response, "name", name);
    }


    @Test
    public void restishCreateCampaigninXML() throws Exception {
        String req = null, name="Restishly created Campaign";
        // req= "<masg-request>"+
        req = "<campaign>" +
                "<name>"+name+"</name>" +
                "<description>Created via XML</description>" +
                "<bid><type>CPC</type><amount>2.00</amount></bid>" +
              "</campaign>";
        // "</masg-request>";
        String response=f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
    }


    @Test
    public void createCampaignWithFullSegmentInfo() throws Exception {
        String req = null, name="Test Campaign 142";
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Campaign with full segment info</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                    "<segment> " +
                        "<daysOfWeek>3</daysOfWeek> " +
                        "<hoursOfDay>1</hoursOfDay> " +
                        "<maxAge>45</maxAge> " +
                        "<hoursOfDayWeekend>2</hoursOfDayWeekend> " +
                        "<operators> " +
                            "<operator>Mobiland</operator> " +
                            "<operator>Eagle Mobile</operator> " +
                        "</operators> " +
                        "<operatorWhitelist>2</operatorWhitelist> " +
                    "</segment>" +
                "</campaign>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
    }


    String segmentCampId = "dff97606-3d9b-4e1d-bb61-c9e2156da40f";


    @Test
    public void updateSomeSegmentInfo() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<daysOfWeek>3219</daysOfWeek> " +
                        "<hoursOfDay>1</hoursOfDay> " +
                        "<maxAge>41</maxAge> " +
                        "<hoursOfDayWeekend>2</hoursOfDayWeekend> " +
                        "<countries> " +
                            "<country>US</country> " +
                            "<country>AR</country> " +
                        "</countries> " +
                        "<operators> " +
                            "<operator>Telsysint</operator> " +
                            "<operator>Movicel</operator> " +
                        "</operators> " +
                        "<models> " +
                            "<model>slide99</model> " +
                            "<model>Mandarina Duck</model> " +
                            "<model>M515</model> " +
                        "</models> " +
                        "<operatorWhitelist>1</operatorWhitelist> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, 200);
    }


    @Test
    public void updateSegmentInfoIncludingIPAddresses() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<daysOfWeek>3219</daysOfWeek> " +
                        "<hoursOfDay>1</hoursOfDay> " +
                        "<maxAge>41</maxAge> " +
                        "<hoursOfDayWeekend>2</hoursOfDayWeekend> " +
                        "<operatorWhitelist>1</operatorWhitelist> " +
                        "<platforms> " +
                            "<platform>symbian</platform> " +
                            "<platform>webos</platform> " +
                        "</platforms> " +
                        "<ipAddresses> " +
                            "<ipAddress>9.29.98.78</ipAddress> " +
                            "<ipAddress>9.9.12.123</ipAddress> " +
                        "</ipAddresses> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, 200);
    }


    @Test
    public void updateSegmentInfoIncludingIPAddressesAndGeotargets() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<daysOfWeek>4</daysOfWeek> " +
                        "<hoursOfDay>1</hoursOfDay> " +
                        "<maxAge>41</maxAge> " +
                        "<hoursOfDayWeekend>2</hoursOfDayWeekend> " +
                        "<operatorWhitelist>1</operatorWhitelist> " +
                        "<ipAddresses> " +
                            "<ipAddress>9.29.98.78</ipAddress> " +
                            "<ipAddress>9.9.12.123</ipAddress> " +
                        "</ipAddresses> " +
                        "<geotargets>" +
                            "<geotarget>" +
                                "<country>US</country>" +
                                "<type>STATE</type>" +
                                "<name>California</name>" +
                            "</geotarget>" +
                            "<geotarget>" +
                                "<country>US</country>" +
                                "<type>STATE</type>" +
                                "<name>Arizona</name>" +
                            "</geotarget>" +
                        "</geotargets>" +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, 200);
    }


    static final int MINUTE = 1000 * 60;
    static final int HOUR = MINUTE * 60;
    static final long DAY = HOUR * 24;

    @Test
    public void createCampaignWithTimePeriods() throws Exception {
        long currentTime=System.currentTimeMillis();

        String req = null, name="Test Camp Tim per 15";
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created via XML " + name + "</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                    "<timePeriods>" +
                        "<timePeriod>" +
                            "<startDate>" + (currentTime + 2 * HOUR)/1000 + "</startDate>" +
                            "<endDate>" + (currentTime + 1 * DAY)/1000 + "</endDate>" +
                        "</timePeriod>" +
                        "<timePeriod>" +
                            "<startDate>" + (currentTime + 1 * DAY)/1000 + "</startDate>" +
                            "<endDate>" + (currentTime + 4 * DAY)/1000 + "</endDate>" +
                        "</timePeriod>" +
                        "<timePeriod>" +
                            "<startDate>" + (currentTime + 6 * DAY)/1000 + "</startDate>" +
                        "</timePeriod>" +
                    "</timePeriods>" +
                "</campaign>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
    }


    @Test
    public void updateCampaignWithTimePeriods() throws Exception {
        long currentTime = System.currentTimeMillis();
        String campaignID = segmentCampId;// currently reusing segment campaign for this

        String req = null;
        req =   "<campaign>" +
                    "<timePeriods>" +
                        "<timePeriod>" +
                            "<startDate></startDate>" +
                            "<endDate>" + (currentTime + 3 * DAY)/1000 + "</endDate>" +
                        "</timePeriod>" +
                        "<timePeriod>" +
                            "<startDate>" + (currentTime + 6 * DAY)/1000 + "</startDate>" +
                        "</timePeriod>" +
                    "</timePeriods>" +
                "</campaign>";
        String response = f.putXMLBodyForXML("/campaign/" + campaignID, req, 200);
    }


    @Test
    public void tryUpdatingSegmentInfoWithIncompatibleGeotargetsNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<geotargets>" +
                            "<geotarget>" +
                                "<country>US</country>" +
                                "<type>STATE</type>" +
                                "<name>California</name>" +
                            "</geotarget>" +
                            "<geotarget>" +
                                "<country>CA</country>" +
                                "<type>STATE</type>" +
                                "<name>Ontario</name>" +
                            "</geotarget>" +
                        "</geotargets>" +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, WSFixture.CODE_INPUT_VALIDN);
    }


    @Test
    public void tryUpdatingSegmentInfoWithBothIPAddressAndOperatorSetsNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<daysOfWeek>3219</daysOfWeek> " +
                        "<hoursOfDay>1</hoursOfDay> " +
                        "<maxAge>41</maxAge> " +
                        "<hoursOfDayWeekend>2</hoursOfDayWeekend> " +
                        "<operators> " +
                            "<operator>Telsysint</operator> " +
                            "<operator>Movicel</operator> " +
                        "</operators> " +
                        "<operatorWhitelist>1</operatorWhitelist> " +
                        "<platforms> " +
                            "<platform>symbian</platform> " +
                            "<platform>webos</platform> " +
                        "</platforms> " +
                        "<ipAddresses> " +
                            "<ipAddress>9.29.98.78</ipAddress> " +
                            "<ipAddress>9.9.12.123</ipAddress> " +
                        "</ipAddresses> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, WSFixture.CODE_INPUT_VALIDN);
    }


    @Test
    public void tryUpdatingSegmentInfoWithBothPlatformAndModelSetsNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<daysOfWeek>3219</daysOfWeek> " +
                        "<hoursOfDay>1</hoursOfDay> " +
                        "<maxAge>41</maxAge> " +
                        "<hoursOfDayWeekend>2</hoursOfDayWeekend> " +
                        "<models> " +
                            "<model>slide99</model> " +
                            "<model>Mandarina Duck</model> " +
                        "</models> " +
                        "<operators> " +
                            "<operator>Telsysint</operator> " +
                            "<operator>Movicel</operator> " +
                        "</operators> " +
                        "<operatorWhitelist>1</operatorWhitelist> " +
                        "<platforms> " +
                            "<platform>symbian</platform> " +
                            "<platform>webos</platform> " +
                        "</platforms> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, WSFixture.CODE_INPUT_VALIDN);
    }


    @Test
    public void tryUpdatingSegmentInfoWithBothPlatformAndVendorSetsNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                    "<segment> " +
                        "<vendors> " +
                            "<vendor>Orange</vendor> " +
                        "</vendors> " +
                        "<platforms> " +
                            "<platform>symbian</platform> " +
                            "<platform>webos</platform> " +
                        "</platforms> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, WSFixture.CODE_INPUT_VALIDN);
    }


    @Test
    public void updateSegmentInfoWithBothVendorsAndModelsCheckNormalizationNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                    "<segment> " +
                        "<vendors> " +
                            "<vendor>Apple</vendor> " +
                            "<vendor>Orange</vendor> " +
                        "</vendors> " +
                        "<models> " +
                            "<model>slide99</model> " +
                            "<model>7270</model> " +
                            "<model>iPhone</model> " +
                            "<model>Mandarina Duck</model> " +
                        "</models> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, 200);
        //TODO - check for models in returned stuff. It should not have iPhone
    }


    @Test
    public void tryUpdatingSegmentInfoWithBothCountryAndGeotargetSetsNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<countries> " +
                            "<country>GB</country> " +
                            "<country>AR</country> " +
                        "</countries> " +
                        "<geotargets>" +
                            "<geotarget>" +
                                "<country>US</country>" +
                                "<type>STATE</type>" +
                                "<name>California</name>" +
                            "</geotarget>" +
                            "<geotarget>" +
                                "<country>US</country>" +
                                "<type>STATE</type>" +
                                "<name>Arizona</name>" +
                            "</geotarget>" +
                        "</geotargets>" +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, WSFixture.CODE_INPUT_VALIDN);
    }


    @Test
    public void tryUpdatingSegmentInfoWithBothModelAndPlatformSetsNEG() throws Exception {
        String req = null;
        req =   "<campaign>" +
                "<name>updated campaign10</name>" +
                    "<description>Campaign with full segment info upd</description>" +
                    "<segment> " +
                        "<models> " +
                            "<model>Vulcan</model> " +
                        "</models> " +
                        "<platforms> " +
                            "<platform>ios</platform> " +
                            "<platform>rim</platform> " +
                        "</platforms> " +
                    "</segment>" +
                "</campaign>";
        f.putXMLBodyForXML("/campaign/" + segmentCampId, req, WSFixture.CODE_INPUT_VALIDN);
    }


    @Test
    public void attemptToCreateCampaignWithoutDescriptionNEG() throws Exception {
        String name = "Test Campaign 23";
        String req =  "{ \"name\":\""+name+"\", " +
                "\"bid\":{ " +
                    "\"type\":\"CPC\", " +
                    "\"amount\":\"2.9\" " +
                "}" +
              "}";
        String response = f.postJSONBodyForJSON("/campaign/create", req, WSFixture.CODE_INPUT_MISSIN);
        f.verifyBasicContentInJSONResonse(response, "code", ErrorCode.VALIDATION_ERROR);
    }


    @Test
    public void implyUniqueCampaignNamesUnderSameAdvertiserNEG() throws Exception {
        String req = null, name = "Test Campaign 25";
        req = "<campaign>" +
                "<name>"+name+"</name>" +
                "<description>Created via XML Tst 25</description>" +
                "<bid>" +
                    "<type>CPC</type>" +
                    "<amount>2.01</amount>" +
                "</bid>" +
              "</campaign>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, 201);
        f.verifyBasicContentInJSONResonse(response, "name", name);
        response = f.postXMLBodyForJSON("/campaign/create", req, WSFixture.CODE_VALIDN);
        f.verifyBasicContentInJSONResonse(response, "code", ErrorCode.VALIDATION_ERROR);
    }


    @Test
    public void tryCreatingAndSubmittingCampaignWithTooLowBidNEG() throws Exception {
        String req = null, name="Test Campaign 28";
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created - tst campagin 28 </description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>0.01</amount>" +
                    "</bid>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, WSFixture.CODE_VALIDN);
        //String extCampId = null;// TODO - xpath code impl = f.getElementValueFromXMLResonse(response, "id");
        //response=f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, 400);
        //f.verifyBasicContentInXMLResonse(response, "code", ErrorCode.VALIDATION_ERROR);// TODO - this is dep on src/main
    }


    @Test
    public void tryCreatingAndSubmittingCampaignWithTooLowDailyBudgetNEG() throws Exception {
        String req = null, name="Test Campaign 32";
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created - tst campagin " + name + " </description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.01</amount>" +
                    "</bid>" +
                    "<dailyBudget>"+"8"+"</dailyBudget>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, f.CODE_VALIDN);
        //String extCampId = null;// TODO - xpath code impl = f.getElementValueFromXMLResonse(response, "id");
        //response=f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, 400);
        //f.verifyBasicContentInXMLResonse(response, "code", ErrorCode.VALIDATION_ERROR);// TODO - this is dep on src/main
    }


    @Test
    public void tryCreatingAndSubmittingCampaignWithTooLowDailyBudgetWeekdayNEG() throws Exception {
        String req = null, name="Test Campaign 33";
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created - tst campagin " + name + " </description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.01</amount>" +
                    "</bid>" +
                    "<dailyBudgetWeekend>"+"28"+"</dailyBudgetWeekend>" +
                    "<dailyBudgetWeekday>"+"9"+"</dailyBudgetWeekday>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForJSON("/campaign/create", req, f.CODE_VALIDN);
        //String extCampId = null;// TODO - xpath code impl = f.getElementValueFromXMLResonse(response, "id");
        //response=f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, 400);
        //f.verifyBasicContentInXMLResonse(response, "code", ErrorCode.VALIDATION_ERROR);// TODO - this is dep on src/main
    }


    @Test
    public void getCampaign() throws Exception {
        String existingCampaignID = "abddeff2-ed25-41e5-9518-1ce20daa045c";
        String existingCampaign_name = "Campaign_CPM_Campaign";
        String response = f.getJSON("/campaign/" + existingCampaignID);
        System.out.println(response);
        f.verifyBasicContentInJSONResonse(response, "name", existingCampaign_name);
    }


    @Test
    public void getAllCreativesForCampaignJSON() throws Exception {
        System.setProperty("adf.tst.accpthdr.urlbased", "");
        String existingCampaignID = "abddeff2-ed25-41e5-9518-1ce20daa045c";
        String response = f.getJSON("/campaign/" + existingCampaignID+"/creatives/list");
        JSONArray creativesJSON=JSONArray.fromObject(response);
        for(int i=0, len=creativesJSON.size(); i< len; i++){
            verifyCreativeArrayJSONElem(creativesJSON.getString(i), existingCampaignID);
        }
    }
    
    @Ignore
    @Test
    public void getAllCreativesForCampaignWithAssetBundleFS() throws Exception {
        WSFixture f = new WSFixture("jon.hoffman@adfonic.com", "testkey");
        System.setProperty("adf.tst.accpthdr.urlbased", "");
        String existingCampaignID = "d1ec9bc0-87f2-418d-8ab8-51a6279ad6f1";
        String response = f.getJSON("/campaign/" + existingCampaignID+"/creatives/list");
        JSONArray creativesJSON=JSONArray.fromObject(response);
        for(int i=0, len=creativesJSON.size(); i< len; i++){
            System.out.println(creativesJSON.getString(i));
        }
    }
    
    
    private void verifyCreativeArrayJSONElem(String creative, String campaignId)throws Exception{
        System.out.println("FROM_ARR>"+creative);
        JSONObject creativeJSON=JSONObject.fromObject(creative);
        String creativeId=creativeJSON.getString("id");
        Assert.assertEquals(campaignId, creativeJSON.getString("campaignID"));
        
        JSONObject creativeJSONIndiv=JSONObject.fromObject(f.getJSON("/creative/" + creativeId));
        System.out.println("FROM_IND>"+creativeJSONIndiv.toString());
        Assert.assertEquals(creativeJSON, creativeJSONIndiv);
    }

    @Test
    public void getAllCreativesForCampaignXML() throws Exception {
        String existingCampaignID = "abddeff2-ed25-41e5-9518-1ce20daa045c";
        String response = f.getXML("/campaign/" + existingCampaignID+"/creatives/list");
        DocumentBuilderFactory domFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=domFactory.newDocumentBuilder();
        Document document=builder.parse(IOUtils.toInputStream(response));
        XPath xpath=XPathFactory.newInstance().newXPath();
        NodeList creativesXML=(NodeList)xpath.compile("/creatives/creative").evaluate(document, XPathConstants.NODESET);
        for(int i=0, len=creativesXML.getLength(); i<len; i++){
            verifyCreativeArrayXMLElem(creativesXML.item(i), existingCampaignID);
        }
    }
    
    private void verifyCreativeArrayXMLElem(Node creativeNode, String campaignId)throws Exception{
        XPath xpath=XPathFactory.newInstance().newXPath();
        Assert.assertEquals(campaignId, (String)xpath.compile("//campaignID").evaluate(creativeNode, XPathConstants.STRING));
        String creativeId=(String)xpath.compile("id").evaluate(creativeNode, XPathConstants.STRING);
        DocumentBuilderFactory domFactory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=domFactory.newDocumentBuilder();
        Document document=builder.parse(IOUtils.toInputStream(f.getXML("/creative/"+creativeId)));
        Document crDoc=builder.newDocument();
        crDoc.appendChild(crDoc.adoptNode(creativeNode.cloneNode(true)));
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff=new Diff(crDoc, document);
        Assert.assertTrue("XMLs similar " + diff.toString(), diff.similar());
        Assert.assertTrue("XMLs identical " + diff.toString(), diff.identical());
    }
    

    @Test
    public void tryCreatingAndSubmittingCampaignAmbiguousDailyBudgetCombinationNEG() throws Exception {
        String req = null, name="Test Campaign 35";
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created - tst campagin " + name + " </description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.01</amount>" +
                    "</bid>" +
                    "<dailyBudgetWeekend>"+"108"+"</dailyBudgetWeekend>" +
                    "<dailyBudget>"+"29"+"</dailyBudget>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForXML("/campaign/create", req, f.CODE_VALIDN);
        //String extCampId = null;// TODO - xpath code impl = f.getElementValueFromXMLResonse(response, "id");
        //response=f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, 400);
        //f.verifyBasicContentInXMLResonse(response, "code", ErrorCode.VALIDATION_ERROR);// TODO - this is dep on src/main
    }


    @Ignore
    @Test
    public void testDailyBudgetOverrideAdjustmentsAndValidations() throws Exception {
        String req = null, name="Test Campaign 36";
        // req= "<masg-request>"+
        /*req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created - tst campagin " + name + " </description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.01</amount>" +
                    "</bid>" +
                    "<dailyBudgetWeekend>"+"108"+"</dailyBudgetWeekend>" +
                "</campaign>";
        // "</masg-request>";
        String response = f.postXMLBodyForXML("/campaign/create", "");
        */
        String extCampId = "24a47a5d-45d8-42c3-90e8-39fe6c455d48";// TODO - xpath code impl = f.getElementValueFromXMLResonse(response, "id");
        req =   "<campaign>" +
                    "<dailyBudget>"+"29"+"</dailyBudget>" +
                "</campaign>";
        String response = f.putXMLBodyForXML("/campaign/" + extCampId, req, 200);
        req =   "<campaign>" +
                    "<dailyBudgetWeekday>"+"76"+"</dailyBudgetWeekday>" +
                "</campaign>";
        //check if dailyBudgent is 29 and dailyBudgentWeekend is nulled out
        response = f.putXMLBodyForXML("/campaign/" + extCampId, req, 200);

    }


    @Test
    public void tryCreatingCampaignWithTooLowOverAllBudgetNEG() throws Exception {
        String req = null, name="Test Campaign 43";
        req =   "<campaign>" +
                    "<name>"+name+"</name>" +
                    "<description>Created - tst campagin " + name + " </description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.01</amount>" +
                    "</bid>" +
                    "<overallBudget>"+"9"+"</overallBudget>" +
                "</campaign>";
        f.postXMLBodyForJSON("/campaign/create", req, f.CODE_VALIDN);
    }


    @Test
    public void deleteCampaign() throws Exception {//Modified to test the new delete process - additional checks
        String extCampId = "e6357d84-964b-4696-80b7-7af6a292b4d9";
        f.delete("/campaign/" + extCampId, Format.JSON, 204);
        //Try re-deleting the campaign - should not find it
        f.delete("/campaign/" + extCampId, Format.JSON, 404);
        //Try to create a campaign with the same name of the deleted creative - should be successful
        createCampaigninXML("Campaign_deleteme1");
    }


    @Test
    public void updateCampaign() throws Exception {
        String extCampId = "4d83c2d3-9244-4c53-8e38-2773a0427f7a";
        String req = null;
        // req= "<masg-request>"+
        req = "<campaign>" +
                "<name>updated campaign0</name>" +
                "<description>noa via joa</description>" +
                "<reference>usual reference</reference>" +
                "<bid>" +
                  "<type>CPC</type>" +
                   "<amount>2.00</amount>" +
                "</bid>" +
               "</campaign>";
        // "</masg-request>";
        f.putXMLBodyForXML("/campaign/" + extCampId, req, 200);
    }


    @Test
    public void updateCampaignWithSegment() throws Exception {
        String extCampId = "2c5c6b03-f8fb-45e4-8659-4e00b40064d2";
        String req = null;
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>updated campaign1</name>" +
                    "<description>noa via joa</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                    "<segment>" +
                        "<models>" +
                            "<model>M5</model>" +
                            "<model>Pro80</model>" +
                        "</models>" +
                    "</segment>" +
                "</campaign>";
        // "</masg-request>";
        f.putXMLBodyForXML("/campaign/" + extCampId, req, 200);
    }


    @Test
    public void updateCampaignWithSegmentJSON() throws Exception {
        String extCampId = "c121b693-24a8-4468-8904-cbd91d89a79b";
        String req = null;
        // req= "<masg-request>"+
        req =   "<campaign>" +
                    "<name>updated campaign2</name>" +
                    "<description>noa via joa</description>" +
                    "<bid>" +
                        "<type>CPC</type>" +
                        "<amount>2.00</amount>" +
                    "</bid>" +
                    "<segment>" +
                        "<models>" +
                            "<model>One Touch 512</model>" +
                            "<model>F8</model>" +
                        "</models>" +
                    "</segment>" +
                "</campaign>";
        // "</masg-request>";
        f.putBodyFmtSpecd("/campaign/" + extCampId, req, Format.XML, Format.JSON, 200);
    }


    // Make sure to rebuild dataset rigth before running this one, or it will not pass.
    @Test
    public void submitCampaign() throws Exception {
        String extCampId = "9fd3ee4f-8b56-408d-b936-627c1b83a0f5";
        f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, 204);
    }


    @Test
    public void trySubmittingAlreadyActiveCampaignNEG() throws Exception {
        String extCampId = "41776450-95db-472f-b282-2a9eb64b4c28";
        String response=f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_FORBDN);
        f.verifyBasicContentInXMLResonse(response, "code", ErrorCode.INVALID_STATE);// TODO - should be more specific error. other places too
    }


    @Test
    public void trySubmittingCampaignWithNoCreativesNEG() throws Exception {
        String extCampId = "93535eaa-0086-4004-818a-bd4bb62fa92e";
        String response=f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=submit", null, Format.ABSENT, Format.ABSENT, WSFixture.CODE_VALIDN);
        f.verifyBasicContentInXMLResonse(response, "code", ErrorCode.INVALID_STATE);
    }


    @Test
    public void changeCampaignStatusActiveToStop() throws Exception {
        String extCampId = "dbcd8fbb-de26-4c6f-8067-8c692aac93d8";
        f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=stop", null, Format.ABSENT, Format.ABSENT, 204);
    }

    @Test
    public void changeCampaignStatusActiveToPaused() throws Exception {
        String extCampId = "1c4d3122-5140-46c2-b4ff-cf84f55713e7";
        f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=pause", null, Format.ABSENT, Format.ABSENT, 204);
    }

    @Test
    public void changeCampaignStatusPausedToActive() throws Exception {
        String extCampId = "2ac14f22-7c7b-48cb-8cc4-95ee1dea7a98";
        f.postBodyFmtSpecd("/campaign/" + extCampId + ".xml?command=start", null, Format.ABSENT, Format.ABSENT, 204);
    }

}
