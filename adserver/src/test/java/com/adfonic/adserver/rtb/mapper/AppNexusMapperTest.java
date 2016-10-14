package com.adfonic.adserver.rtb.mapper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.open.v2.Publisher;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid.AppNexusExtWrap;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid.CustomMacro;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest.AppNexusUdi;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusSeatBid;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AppNexus - Millenium (and Orange)
 * 
 * @author mvanek
 *
 */
public class AppNexusMapperTest {

    private final ObjectMapper rtbJsonMapper = WebConfig.getRtbJsonMapper();
    private final AppNexusV2Mapper rtbMapper = new AppNexusV2Mapper("static-byyd-1234", "endpoint-macro");

    /**
     * Android devices use standard RTB request.device.ifa
     * 
     * Some iOS devices use request.device.ifa, but majority use custom request.ext.udi.ifa
     * Some even use both!
     */
    @Test
    public void device_ios_ifa() throws Exception {
        //Json -> Rtb
        String bidRequestJson = FileUtils.readFileToString(new File("src/test/data/millennial/app-b320x400-a3-ios.json"));
        AppNexusBidRequest rtbRequest = rtbJsonMapper.readValue(bidRequestJson, AppNexusBidRequest.class);
        AppNexusUdi udi = rtbRequest.getExt().getUdi();
        Assertions.assertThat(udi).isNotNull();
        final String idfa = udi.getIdfa();
        String dpidsha1 = rtbRequest.getDevice().getDpidsha1();
        Assertions.assertThat(idfa).isNotNull();

        //Rtb -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        //Smaato sends more...
        Assertions.assertThat(deviceIdMap)
                .containsOnly(MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_IFA, idfa), MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_DPID, dpidsha1));
    }

    /**
     * MAD-2132 - AppNexus Bid Floors
     */
    @Test
    public void reserve_price() throws IOException, NoBidException {
        //Json -> AppNexus
        String bidRequestJson = FileUtils.readFileToString(new File("src/test/data/appnexus/MAD-2132-reserve_price.json"));
        AppNexusBidRequest appnexusRequest = rtbJsonMapper.readValue(bidRequestJson, AppNexusBidRequest.class);
        BigDecimal reserve_price = appnexusRequest.getImp().get(0).getReserve_price();
        Assertions.assertThat(reserve_price).isEqualTo(new BigDecimal("3.70000"));

        //AppNexus -> Byyd
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("wh-at-ev-er", appnexusRequest, null);
        BigDecimal bidfloor = byydRequest.getImp().getBidfloor();
        Assertions.assertThat(bidfloor).isEqualTo(reserve_price);
    }

    @Test
    public void custom_macros_and_other_response_fields() {
        AdSpaceDto adSpaceDto = new AdSpaceDto();
        adSpaceDto.setExternalID("adspace-ext-id-1234");
        ByydRequest byydRequest = new ByydRequest("zx-zx-zx", "1");
        ByydImp byydImp = new ByydImp("imp-id-1234");
        ByydBid bid = new ByydBid(byydImp);
        CreativeDto creative = new CreativeDto();
        creative.setExternalID("creative-ext-id-1234");
        bid.setCreative(creative);
        bid.setSeat("seat-y");
        ByydResponse byydResponse = new ByydResponse(byydRequest, bid);

        byydRequest.setAdSpace(adSpaceDto);
        // When
        AppNexusBidResponse appNexusResponse = (AppNexusBidResponse) rtbMapper.mapRtbResponse(byydResponse, byydRequest);

        // Then
        AppNexusSeatBid appNexusSeatBid = appNexusResponse.getSeatbid().get(0);
        Assertions.assertThat(appNexusSeatBid.getSeat()).isEqualTo("seat-y"); // AppNexus requires seat (configured staticaly)

        AppNexusExtWrap appnexus = appNexusSeatBid.getBid().get(0).getExt().getAppnexus();
        Assertions.assertThat(appnexus.getCrcode()).isEqualTo(creative.getExternalID()); // creative identification (approval check) 

        List<CustomMacro> custom_macros = appnexus.getCustom_macros(); //  macros finally
        Assertions.assertThat(custom_macros).hasSize(5);

        //TODO Assertions.assertThat(custom_macros).contains(new CustomMacro(AppNexusShared.IMPRESSION_ID_MACRO,""));
    }

    @Test
    public void appnexusRtbIdQuirks() throws IOException, NoBidException {
        //Json -> Rtb
        String bidRequestJson = FileUtils.readFileToString(new File("src/test/data/millennial/app-b320x400-a3-ios.json"));
        AppNexusBidRequest rtbRequest = rtbJsonMapper.readValue(bidRequestJson, AppNexusBidRequest.class);
        Publisher publisher = rtbRequest.getApp().getPublisher();

        //Rtb -> Byyd
        // When
        ByydRequest byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        // Then - appnexus uses app.bundle instead of traditional app.id
        String expectedRtbId = publisher.getId() + "-" + rtbRequest.getApp().getBundle();
        Assertions.assertThat(byydRequest.getPublicationRtbId()).isEqualTo(expectedRtbId);

        //But - publisher is not in bid request
        rtbRequest.getApp().setPublisher(null);
        // When
        byydRequest = rtbMapper.mapRtbRequest("publ-exid-what-ever", rtbRequest, null);
        // Then - constant APNXAPP prefix is used 
        expectedRtbId = "APNXAPP-" + rtbRequest.getApp().getBundle();
        Assertions.assertThat(byydRequest.getPublicationRtbId()).isEqualTo(expectedRtbId);
    }

}
