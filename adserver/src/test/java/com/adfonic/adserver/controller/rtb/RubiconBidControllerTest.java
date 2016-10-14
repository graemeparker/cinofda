package com.adfonic.adserver.controller.rtb;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.rtb.nativ.ByydResponse;

/**
 * 
 * @author mvanek
 *
 */
public class RubiconBidControllerTest extends AbstractV2BidTest<RubiconRTBv2Controller> {

    private static final File TEST_FILE = new File("src/test/data/rubicon/app-b320x50-a0-andr.json");

    @Override
    protected String getRequestUrlPath() {
        return "/rtb/rubicon/bid/98970e34-eb0a-4221-bb94-c10715b93d3f";
    }

    @Override
    protected RtbEndpoint getEndpoint() {
        return RtbEndpoint.RubiconV2;
    }

    @Override
    byte[] getBidRequestPayload() throws IOException {
        return FileUtils.readFileToByteArray(TEST_FILE);
    }

    @Override
    protected RubiconRTBv2Controller buildController() {
        return new RubiconRTBv2Controller(rtbLogicMock, backupLoggerMock, bidListenerMock, offenceRegistry, fisherman, counterManager);
    }

    @Test
    public void testMAD_2050() throws Exception {
        // Rubicon sends custom ids (1000) in bidreq/imp/banner/api

        String rtbRequestJson = FileUtils.readFileToString(new File("src/test/data/rubicon/MAD-2050-api-1000.json"));
        com.adfonic.adserver.rtb.rubicon.RubiconBidRequest rtbRequest = WebConfig.getRtbJsonMapper().readValue(rtbRequestJson,
                com.adfonic.adserver.rtb.rubicon.RubiconBidRequest.class);
        ByydResponse byydResponse = buildByydResponse(rtbRequest.getImp().iterator().next().getId());
        setRtbLogicResponse(byydResponse);

        // When
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(getRequestUrlPath(), "98970e34-eb0a-4221-bb94-c10715b93d3f").content(rtbRequestJson)
                .contentType(getEndpoint().getProtocol().getRequestMediaType());
        ResultActions actions = mockMvc.perform(requestBuilder);
        // Then
        actions.andExpect(MockMvcResultMatchers.status().isOk());
        actions.andExpect(MockMvcResultMatchers.header().string("Expires", "0"));
        actions.andExpect(MockMvcResultMatchers.header().string("Pragma", "No-Cache"));

        Mockito.verify(backupLoggerMock, Mockito.times(1)).startControllerRequest();
        Mockito.verify(rtbLogicMock, Mockito.times(1)).bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class));
        //verify logged warning...
        //LogRecord firstLogRecord = LogCapturingHandler.get().last();
        //Assertions.assertThat(firstLogRecord.getLoggerName()).isEqualTo(RtbBidSequence.class.getName());
        //Assertions.assertThat(firstLogRecord.getMessage()).isEqualTo("Unrecognized APIFramework id: 1000");
    }
}
