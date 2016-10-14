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
import com.adfonic.adserver.rtb.nativ.ByydDeal;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.PmpV2.DealV2;
import com.adfonic.adserver.rtb.rubicon.RubiconBidRequest;
import com.adfonic.adserver.rtb.rubicon.RubiconRtbMapper;
import com.adfonic.domain.DeviceIdentifierType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RubiconMapperTest {

    private final ObjectMapper rtbJsonMapper = WebConfig.getRtbJsonMapper();
    private final RubiconRtbMapper rtbMapper = RubiconRtbMapper.instance();

    @Test
    public void device_pmp() throws Exception {
        //Json -> RTB

        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/rubicon/pmp-deals-BYYD-site-ios.json"));
        RubiconBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, RubiconBidRequest.class);
        DealV2 rubiconDeal1 = rtbRequest.getImp().get(0).getPmp().getDeals().get(0);
        DealV2 rubiconDeal2 = rtbRequest.getImp().get(0).getPmp().getDeals().get(1);

        //RTB -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);

        Assertions.assertThat(byydRequest.getMarketPlace().getDeals()).hasSize(2);

        ByydDeal byydDeal1 = byydRequest.getMarketPlace().getDeals().get(0);
        Assertions.assertThat(rubiconDeal1.getId()).isEqualTo(byydDeal1.getId());
        Assertions.assertThat(rubiconDeal1.getBidfloor().doubleValue()).isEqualTo(byydDeal1.getBidFloor());
        Assertions.assertThat(rubiconDeal1.getAt()).isEqualTo(2);

        Assertions.assertThat(rubiconDeal1.getWseat()).isNotEmpty();
        Assertions.assertThat(rubiconDeal1.getWseat()).isEqualTo(byydDeal1.getSeats()); //seat id

        ByydDeal byydDeal2 = byydRequest.getMarketPlace().getDeals().get(1);
        Assertions.assertThat(rubiconDeal2.getId()).isEqualTo(byydDeal2.getId());
        Assertions.assertThat(rubiconDeal2.getBidfloor().doubleValue()).isEqualTo(byydDeal2.getBidFloor());
        Assertions.assertThat(rubiconDeal2.getAt()).isEqualTo(2);

        Assertions.assertThat(rubiconDeal2.getWseat()).isNull();
    }

    /**
     * IFA and ADID in request.device.ifa (RTB standard way)
     */
    @Test
    public void device_ios_ifa() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/rubicon/app-b300x350-a35-ios.json"));
        testDeviceIdFor(rtbRequestJson, DeviceIdentifierType.SYSTEM_NAME_IFA);
    }

    /**
     * IFA and ADID in request.device.ifa (RTB standard way)
     */
    @Test
    public void device_android_adid() throws Exception {
        //Json -> RTB
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/rubicon/app-b320x50-a0-andr.json"));
        testDeviceIdFor(rtbRequestJson, DeviceIdentifierType.SYSTEM_NAME_ADID);
    }

    /**
     * No device ids from mobile website requests
     */
    @Test
    public void device_website() throws Exception {
        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/rubicon/site-b320x50-a0-ios.json"));
        RubiconBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, RubiconBidRequest.class);
        final String deviceId = rtbRequest.getDevice().getIfa();
        Assertions.assertThat(deviceId).isNull();

        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).isEmpty();

    }

    private void testDeviceIdFor(String rtbRequestJson, String deviceTypeId) throws IOException, NoBidException {
        //Json -> RTB
        RubiconBidRequest rtbRequest = rtbJsonMapper.readValue(rtbRequestJson, RubiconBidRequest.class);
        final String deviceId = rtbRequest.getDevice().getIfa();
        Assertions.assertThat(deviceId).isNotNull();

        //RTB -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        Assertions.assertThat(deviceIdMap).containsExactly(MapEntry.entry(deviceTypeId, deviceId));
    }
}
