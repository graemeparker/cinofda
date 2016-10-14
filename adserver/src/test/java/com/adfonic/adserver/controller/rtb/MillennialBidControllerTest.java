package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.adfonic.adserver.rtb.mapper.AppNexusV2Mapper;

/**
 * 
 * @author mvanek
 * 
 * Millenium (and Orange) use Appnexus Controller and Mapper but it's own publisherExternalId
 *
 */
public class MillennialBidControllerTest extends AbstractV2BidTest<AppNexusV2Controller> {

    private static File TEST_FILE = new File("src/test/data/millennial/app-b320x50-a3-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/appnexus/v2/bid/6a91c79e-d662-40e3-9589-d37da13760b8";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.AppNexusV2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected AppNexusV2Controller buildController() {
        AppNexusV2Mapper v2mapper = new AppNexusV2Mapper("seat-x", "-emea");
        return new AppNexusV2Controller(v2mapper, rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
