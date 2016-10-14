package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 * 
 * Flurry uses directly OpenRTBv2Controller without extensions
 *
 */
public class FlurryBidControllerTest extends AbstractV2BidTest<OpenRTBv2Controller> {

    private static File TEST_FILE = new File("src/test/data/flurry/app-b320x50-a3-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/v2/bid/5e5f53af-c500-42a4-aec3-7a5ebfde64fc";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.ORTBv2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected OpenRTBv2Controller buildController() {
        return new OpenRTBv2Controller(rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
