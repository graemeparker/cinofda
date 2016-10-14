package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.adfonic.adserver.rtb.mapper.OpenXMapper;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.adserver.rtb.openx.OpenX.AdId;
import com.google.protobuf.ByteString;

//@RunWith(Suite.class)
//@SuiteClasses({ MyClassTest.class, MySecondClassTest.class })
public class OpenXControllerTest extends AbstractBidTest<OpenXController> {

    private static File TEST_FILE = new File("src/test/data/openx/app-320x50-iphone.proto.txt");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/openx/bid/c5373546-5d54-41c0-9707-0fe49fdf5863";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.OpenX;
    }

    @Override
    protected OpenXController buildController() {
        return new OpenXController(rtbLogicMock, bidListenerMock, backupLoggerMock, offenceRegistry, fisherman, counterManager);
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return OpenXMapper.protoText2Bytes(FileUtils.readFileToString(TEST_FILE));
    }

    @Override
    ByydResponse getByydResponse() {
        //OpenX.BidRequest rtbRequest = buildBidRequest();
        return buildByydResponse("0"); //index into getMatchingAdIds
    }

    public OpenX.BidRequest buildRtbBidRequest() throws IOException {

        String auctionId = StringUtils.defaultIfEmpty(System.getProperty("auctionId"), UUID.randomUUID().toString());
        String pubWebsiteId = StringUtils.defaultIfEmpty(System.getProperty("pubWebsiteId"), "54edd463-720a-4551-8cb2-c8eaa5db044c");
        Integer adWidth = NumberUtils.createInteger(StringUtils.defaultIfEmpty(System.getProperty("adWidth"), "320"));
        Integer adHeight = NumberUtils.createInteger(StringUtils.defaultIfEmpty(System.getProperty("adHeight"), "50"));
        String userAgent = StringUtils.defaultIfEmpty(System.getProperty("userAgent"),
                "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16");
        String ip = StringUtils.defaultIfEmpty(System.getProperty("ip"), "67.32.183.150");
        String url = StringUtils.defaultIfEmpty(System.getProperty("url"), "http://www.disney.com/byyd-test");
        boolean testMode = BooleanUtils.toBoolean(StringUtils.defaultIfEmpty(System.getProperty("testMode"), "true"));
        String languages = StringUtils.defaultIfEmpty(System.getProperty("languages"), "en,es,fr,ko");
        String dpid = System.getProperty("dpid");
        String androidId = System.getProperty("androidId");
        String odin1 = System.getProperty("odin1");
        String openudid = System.getProperty("openudid");
        String ifa = System.getProperty("ifa");
        String connectionType = StringUtils.defaultIfEmpty(System.getProperty("connectionType"), "wifi");
        String mccDashMnc = System.getProperty("mccDashMnc");
        Float latitude = NumberUtils.createFloat(System.getProperty("latitude"));
        Float longitude = NumberUtils.createFloat(System.getProperty("longitude"));
        String country = System.getProperty("country");
        String state = System.getProperty("state");
        String zip = System.getProperty("zip");
        Integer dma = NumberUtils.createInteger(System.getProperty("dma"));
        int campaignId = 666;
        int placementId = 666;
        int creativeId = 666;
        String userCookieId = System.getProperty("userCookieId");

        OpenX.Geo.Builder geo = OpenX.Geo.newBuilder();
        if (latitude != null) {
            geo.setLat(latitude);
        }
        if (longitude != null) {
            geo.setLon(longitude);
        }
        if (StringUtils.isNotEmpty(country)) {
            geo.setCountry(country);
        }
        if (StringUtils.isNotEmpty(state)) {
            geo.setState(state);
        }
        if (StringUtils.isNotEmpty(zip)) {
            geo.setZip(zip);
        }
        if (dma != null) {
            geo.setDma(dma);
        }

        OpenX.Device.Builder device = OpenX.Device.newBuilder().setUa(userAgent).setIp(ip).setGeo(geo);
        if (StringUtils.isNotEmpty(languages)) {
            for (String language : StringUtils.split(languages, ",")) {
                device.addLanguage(language);
            }
        }
        if (StringUtils.isNotEmpty(mccDashMnc)) {
            device.setCarrier(mccDashMnc);
        }
        if (StringUtils.isNotEmpty(connectionType)) {
            device.setConnectiontype(connectionType);
        }
        if (StringUtils.isNotEmpty(dpid)) {
            device.setDidsha1(dpid);
        }
        if (StringUtils.isNotEmpty(androidId)) {
            //device.setAndroidid(androidId);
        }
        if (StringUtils.isNotEmpty(odin1)) {
            device.setOdin1(odin1);
        }
        if (StringUtils.isNotEmpty(openudid)) {
            device.setOpenudid(openudid);
        }
        if (StringUtils.isNotEmpty(ifa)) {
            device.setIdforad(ifa);
        }

        AdId.Builder adId = AdId.newBuilder().setCampaignId(campaignId).setPlacementId(placementId).setCreativeId(creativeId).setAdWidth(adWidth).setAdHeight(adHeight);

        OpenX.BidRequest.Builder bidRequestBuilder = OpenX.BidRequest.newBuilder().setApiVersion(7).setAuctionId(auctionId).setPubWebsiteId(pubWebsiteId).setAdWidth(adWidth)
                .setAdHeight(adHeight).setUserAgent(userAgent).setUserIpAddress(ByteString.copyFrom(InetAddress.getByName(ip).getAddress())).setUrl(url).setIsTest(testMode)
                .setDevice(device)
                // Uncomment these to test multiple imps per bid request
                //.addMatchingAdIds(adId)
                //.addMatchingAdIds(adId)
                .addMatchingAdIds(adId);
        if (StringUtils.isNotEmpty(userCookieId)) {
            bidRequestBuilder.setUserCookieId(userCookieId);
        }
        return bidRequestBuilder.build();

    }

}