package com.adfonic.adserver.rtb.mapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.domain.DeviceIdentifierType;

/**
 * 
 * @author mvanek
 *
 */
public class OpenXMapperTest {

    private static final OpenXMapper mapper = OpenXMapper.instance();

    @Test
    public void test_IFA() throws Exception {
        OpenX.BidRequest rtbRequest = buildBidRequest("app-320x50-iphone-ifa.proto.txt");

        //When 
        ByydRequest byydRequest = mapper.mapRequest(RtbExchange.OpenX.getPublisherExternalId(), rtbRequest);
        //Then 
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        MapEntry adidEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_IFA, "37BF031B-8DDB-4F63-88EF-46C426A969C0");
        Assertions.assertThat(deviceIdMap).containsExactly(adidEntry);
    }

    @Test
    public void test_ADID() throws Exception {
        OpenX.BidRequest rtbRequest = buildBidRequest("app-320x50-a3-andr.proto.txt");

        //When 
        ByydRequest byydRequest = mapper.mapRequest(RtbExchange.OpenX.getPublisherExternalId(), rtbRequest);
        //Then
        Map<String, String> deviceIdMap = byydRequest.getDevice().getDeviceIdentifiers();
        MapEntry adidEntry = MapEntry.entry(DeviceIdentifierType.SYSTEM_NAME_ADID, "9be523c8-ed8e-474f-8048-1f678191dab1");
        Assertions.assertThat(deviceIdMap).containsExactly(adidEntry);
    }

    private OpenX.BidRequest buildBidRequest(String filename) throws IOException {
        return OpenXMapper.protoText2Request(FileUtils.readFileToString(new File("src/test/data/openx/" + filename)));
    }
}
