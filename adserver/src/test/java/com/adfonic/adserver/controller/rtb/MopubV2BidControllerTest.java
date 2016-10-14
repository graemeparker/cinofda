package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 *
 */
public class MopubV2BidControllerTest extends AbstractV2BidTest<MopubV2BidController> {

    private static File TEST_FILE = new File("src/test/data/mopub/app-b320x50-a3-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/mopub/bid/62e77526-8f80-4224-a07e-7d851f87048e";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.MopubV2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected MopubV2BidController buildController() {
        return new MopubV2BidController(rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
