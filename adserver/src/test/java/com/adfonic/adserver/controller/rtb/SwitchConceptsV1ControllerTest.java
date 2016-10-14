package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 * 
 * POST /rtb/bid/c350591b-40ae-4e94-b2f9-6a8ae2d1c86b HTTP/1.1
 * User-Agent: PECL::HTTP/1.7.1 (PHP/5.3.6)
 * Host: switchconcepts-rtb.byyd.net
 * Accept: * / *
 * Content-Type: application/json
 * Content-Length: 462
 * 
 * OpenRTB v1 
 * 
 */
public class SwitchConceptsV1ControllerTest extends AbstractV2BidTest<OpenRtbV1Controller> {

    private static final File TEST_FILE = new File("src/test/data/switch/site-320x250-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/bid/c350591b-40ae-4e94-b2f9-6a8ae2d1c86b";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.ORTBv1;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected OpenRtbV1Controller buildController() {
        return new OpenRtbV1Controller(rtbLogicMock, rtbWinLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

}
