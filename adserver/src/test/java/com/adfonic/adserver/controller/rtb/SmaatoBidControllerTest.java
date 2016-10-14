package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 * 
 * Smaato dedicated instances: ch1adserver37, ch1adserver38, ch1adserver39
 * 
 * Mostly mobile sites
 */
public class SmaatoBidControllerTest extends AbstractV2BidTest<SmaatoBidController> {

    private static final File TEST_FILE = new File("src/test/data/smaato/app-b320x50-a0-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/smaato/bid/09d0d0da-1762-43d3-925b-1335fa895f6b";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.SmaatoV2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected SmaatoBidController buildController() {
        return new SmaatoBidController(rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
