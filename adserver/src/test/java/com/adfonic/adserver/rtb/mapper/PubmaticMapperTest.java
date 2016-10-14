package com.adfonic.adserver.rtb.mapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.ext.pubmatic.PubmaticBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.pubmatic.PubmaticDevice;
import com.adfonic.domain.DeviceIdentifierType;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class PubmaticMapperTest {

    @Mock
    private RtbBidEventListener listener;

    private final PubmaticRTBV2Mapper rtbMapper = PubmaticRTBV2Mapper.instance();
    private ObjectMapper rtbJsonMapper = WebConfig.getRtbJsonMapper();
    private String publisherExternalId = "28CCEE34-8178-4F32-8DD5-549D5A70B941";

    /**
     * Pubmatic sends IFA in request.device.ext.idfa or request.device.ext.otherdeviceid
     */
    @Test
    public void device_ios_ifa() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/pubmatic/app-b320x50-a0-ios.json"));
        PubmaticBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, PubmaticBidRequest.class);
        PubmaticDevice.DeviceExt deviceExt = rtbRequest.getDevice().getExt();
        Assertions.assertThat(deviceExt).isNotNull();
        final String deviceId = deviceExt.getIdfa();
        Assertions.assertThat(deviceId).isNotNull();

        //RTB -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).containsExactly(MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_IFA, deviceId));

    }

    /**
     * Pubmatic sends ADID in request.device.ext.otherdeviceid
     */
    @Test
    public void device_android_adid() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/pubmatic/app-b302x50-a3-andr.json"));
        PubmaticBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, PubmaticBidRequest.class);
        PubmaticDevice.DeviceExt deviceExt = rtbRequest.getDevice().getExt();
        Assertions.assertThat(deviceExt).isNotNull();
        final String deviceId = deviceExt.getOtherdeviceid();
        Assertions.assertThat(deviceId).isNotNull();

        //RTB -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).containsExactly(MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_ADID, deviceId));
    }

    /**
     * Pubmatic uses ext.bidguidefloor instead of standard bidfloor
     */
    @Test
    public void testBidFloorGetsPopulated() throws NoBidException, IOException {

        PubmaticBidRequest bidRequest = makeRequest();

        // When
        ByydRequest result = rtbMapper.mapRtbRequest(publisherExternalId, bidRequest, listener);

        // Then
        Assert.assertEquals(publisherExternalId, result.getId());
        ByydImp imp = result.getImp();
        Assert.assertEquals(new BigDecimal("1.82"), imp.getBidfloor());
    }

    private PubmaticBidRequest makeRequest() throws IOException {

        String json = "{\"id\":\"28CCEE34-8178-4F32-8DD5-549D5A70B941\",\"imp\":[{\"id\":\"1\",\"tagid\":\"36839\",\"bidfloor\":1.82,\"banner\":{\"w\":320,\"h\":50,\"topframe\":1,\"battr\":[8,5],\"api\":[]}}],\"app\":{\"id\":\"570756103\",\"name\":\"PingerEX:TextFree+FreeCalls\",\"ver\":\"1.4.3\",\"bundle\":\"com.pinger.pingerphone2plus\",\"domain\":\"http://www.pinger.com\",\"paid\":0,\"storeurl\":\"https://itunes.apple.com/us/app/pinger-ex-text-free-+-1-hour/id570756103?mt=8\",\"publisher\":{\"id\":\"31911\"}},\"device\":{\"ip\":\"208.122.88.254\",\"ua\":\"Mozilla/5.0(iPodtouch;CPUiPhoneOS7_0_6likeMacOSX)AppleWebKit/537.51.1(KHTML,likeGecko)Mobile/11B651\",\"carrier\":\"SouthCentralOhioComputerAssociation\",\"language\":\"en-us\",\"make\":\"Apple\",\"model\":\"iPODTouch\",\"os\":\"iOS\",\"Osv\":\"2\",\"js\":1,\"connectiontype\":2,\"devicetype\":1,\"geo\":{\"country\":\"US\",\"region\":\"OH\",\"city\":\"Waverly\",\"lat\":39.1527,\"lon\":-83.0391,\"zip\":\"45690\",\"type\":2},\"ext\":{\"otherdeviceid\":\"CCDBA1B7AF26E4C37E69E16BC86AE2962FBE7EF7\"}},\"user\":{\"id\":\"B80A1FCA-2DF4-4ABF-8E3B-4B71A0278E9B\",\"yob\":1996,\"gender\":\"M\",\"geo\":{\"lat\":39.1207,\"lon\":-83.0088,\"region\":\"OH\",\"city\":\"WAVERLY\",\"type\":3}},\"badv\":[\"trojan.ca\",\"schedulicity.com\",\"webfetti.com\"]}";

        PubmaticBidRequest requestObj = rtbJsonMapper.readValue(json, PubmaticBidRequest.class);
        return requestObj;

    }

}
