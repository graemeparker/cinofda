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
import com.adfonic.adserver.rtb.open.v2.ext.nexage.NexageBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.nexage.NexageDevice;
import com.adfonic.domain.DeviceIdentifierType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NexageMapperTest {

    private final ObjectMapper rtbJsonMapper = WebConfig.getRtbJsonMapper();
    private final NexageRTBv2Mapper rtbMapper = NexageRTBv2Mapper.instance();

    /**
     * MAD_2339
     * Nexage sends IFA and ADID in request/device/ext/nex_ifa
     */
    @Test
    public void device_ios_ifa() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/nexage/app-b320x50-a0-ios.json"));
        testDeviceIdFor(rtbRequestJson, DeviceIdentifierType.SYSTEM_NAME_IFA);
    }

    /**
     * MAD_2339
     * Nexage sends IFA and ADID in request/device/ext/nex_ifa
     */
    @Test
    public void device_android_adid() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/nexage/app-b320x50-a3-andr.json"));
        testDeviceIdFor(rtbRequestJson, DeviceIdentifierType.SYSTEM_NAME_ADID);
    }

    private void testDeviceIdFor(String rtbRequestJson, String deviceTypeId) throws IOException, NoBidException {
        //Json -> RTB
        NexageBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, NexageBidRequest.class);
        NexageDevice.DeviceExt deviceExt = rtbRequest.getDevice().getExt();
        Assertions.assertThat(deviceExt).isNotNull();
        final String deviceId = deviceExt.getNex_ifa();
        Assertions.assertThat(deviceId).isNotNull();

        //RTB -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).containsExactly(MapEntry.entry(deviceTypeId, deviceId));
    }
}
