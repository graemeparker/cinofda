package com.adfonic.adserver.rtb.mapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidRequest;
import com.adfonic.domain.DeviceIdentifierType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MopubMapperTest {

    private final ObjectMapper rtbJsonMapper = WebConfig.getRtbJsonMapper();
    private final MopubRTBv2Mapper rtbMapper = MopubRTBv2Mapper.instance();

    /**
     * Mopub sends IFA and ADID in request/device/ext/idfa
     */
    @Test
    public void device_ios_ifa() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/mopub/app-b320x50-a35-ios.json"));
        testDeviceIdFor(rtbRequestJson, DeviceIdentifierType.SYSTEM_NAME_IFA);
    }

    /**
     * Mopub sends IFA and ADID in request/device/ext/idfa
     */
    @Test
    public void device_android_adid() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/mopub/app-b320x50-a3-andr.json"));
        testDeviceIdFor(rtbRequestJson, DeviceIdentifierType.SYSTEM_NAME_ADID);
    }

    private void testDeviceIdFor(String rtbRequestJson, String deviceTypeId) throws IOException, NoBidException {
        //Json -> RTB
        MopubBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, MopubBidRequest.class);
        final String deviceId = rtbRequest.getDevice().getIfa();
        Assertions.assertThat(deviceId).isNotNull();

        // String dpidsha1 = rtbRequest.getDevice().getDpidsha1();

        //RTB -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).containsOnly(MapEntry.entry(deviceTypeId, deviceId));
    }
}
