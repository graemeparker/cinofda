package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author mvanek
 * 
 * lon2adserver19
 * sudo /usr/sbin/tcpdump -Anni bond0 'tcp port 80 and src net 217.148.91.80' | grep 9d6e656c-58fe-46c2-a188-5b956803633c -A14
 * 
 * POST /rtb/v2/bid/9d6e656c-58fe-46c2-a188-5b956803633c HTTP/1.1
 * Host: mads-rtb.byyd.net
 * Accept: * / *
 * Content-Type: application/json
 * Content-Length: 1228
 * x-openrtb-version: 2.2
 * x-openrtb-exchange: mads
 *
 */
public class MadsBidControllerTest extends AbstractV2BidTest<OpenRTBv2Controller> {

    private static File TEST_FILE = new File("src/test/data/mads/app-b320x50-a35-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/v2/bid/9d6e656c-58fe-46c2-a188-5b956803633c";
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
