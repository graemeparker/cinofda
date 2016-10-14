package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 *
 */
public class NexageBidControllerTest extends AbstractV2BidTest<NexageV2Controller> {

    private static final File TEST_FILE = new File("src/test/data/nexage/app-b320x50-a3-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/nexage/v2/bid/924146eb-ae41-41b1-bacb-4e445faa99a3";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.NexageV2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected NexageV2Controller buildController() {
        return new NexageV2Controller(rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
