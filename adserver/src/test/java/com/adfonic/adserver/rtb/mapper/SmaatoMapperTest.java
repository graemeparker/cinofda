package com.adfonic.adserver.rtb.mapper;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.smaato.SmaatoBidRequest;
import com.adfonic.adserver.rtb.smaato.SmaatoBidMapper;
import com.adfonic.adserver.rtb.smaato.SmaatoUdi;
import com.adfonic.domain.DeviceIdentifierType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SmaatoMapperTest {

    private final ObjectMapper rtbJsonMapper = WebConfig.getRtbJsonMapper();
    private final SmaatoBidMapper rtbMapper = SmaatoBidMapper.instance();

    /**
     * MAD_2061
     * Smaato sends Device Ids in request/ext/udi section
     */
    @Test
    public void device_ios_ifa() throws Exception {
        //Json -> Smaato
        String bidRequestJson = FileUtils.readFileToString(new File("src/test/data/smaato/app-b728x90-a0-ios.json"));
        SmaatoBidRequest rtbRequest = rtbJsonMapper.readValue(bidRequestJson, SmaatoBidRequest.class);
        SmaatoUdi udi = rtbRequest.getExt().getUdi();
        Assertions.assertThat(udi).isNotNull();
        final String deviceId = udi.getIdfa();
        Assertions.assertThat(deviceId).isNotNull();

        //Smaato -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        //Smaato sends more...
        Assertions.assertThat(deviceIdMap).contains(MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_IFA, deviceId));
    }

    /**
     * MAD_2061
     * Smaato sends Device Ids in request/ext/udi section
     */
    @Test
    public void device_android_adid() throws Exception {
        //Json -> Smaato
        String bidRequestJson = FileUtils.readFileToString(new File("src/test/data/smaato/app-b320x50-a3-andr.json"));
        SmaatoBidRequest rtbRequest = rtbJsonMapper.readValue(bidRequestJson, SmaatoBidRequest.class);
        SmaatoUdi udi = rtbRequest.getExt().getUdi();
        Assertions.assertThat(udi).isNotNull();
        final String deviceId = udi.getGoogleadid();
        Assertions.assertThat(deviceId).isNotNull();

        //Smaato -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).containsExactly(MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_ADID, deviceId));
    }
}
