package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 *
 */
public class PubmaticBidControllerTest extends AbstractV2BidTest<PubmaticRTBv2Controller> {

    private static final File TEST_FILE = new File("src/test/data/pubmatic/app-b302x50-a3-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/pubmatic/bid/1c2ba3d1-d8b0-42c0-88f7-09e5b8b97da4";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.PubmaticV2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected PubmaticRTBv2Controller buildController() {
        return new PubmaticRTBv2Controller(rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
