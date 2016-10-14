package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 * 
 * POST /rtb/bid/b5489eb7-8300-439e-8c97-c69ff2eabc1a HTTP/1.1
 * host: adiquity-rtb.byyd.net
 * Content-type: application/json
 * User-Agent: Adiquity AdFetcher/1.9
 * X-Forwarded-For: 31.24.34.163
 * X-Forwarded-Port: 80
 * X-Forwarded-Proto: http
 * Content-Length: 546
 * Connection: keep-alive
 * 
 * OpenRTB v1 
 * 
 */
public class AdiquityV1ControllerTest extends AbstractBidTest<OpenRtbV1Controller> {

    private static File TEST_FILE = new File("src/test/data/adiquity/app-320x50-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/bid/b5489eb7-8300-439e-8c97-c69ff2eabc1a";
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
