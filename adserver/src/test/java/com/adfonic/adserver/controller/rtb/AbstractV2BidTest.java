package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.offence.OffenceRegistry.BidExceptionStats;
import com.adfonic.adserver.offence.OffenceSection;
import com.adfonic.adserver.offence.TroubledBidRequest;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.util.stats.FreqLogr;
import com.adfonic.util.stats.FreqLogr.ExceptionData;

public abstract class AbstractV2BidTest<C> extends AbstractBidTest<C> {

    @Override
    ByydResponse getByydResponse() throws IOException {
        return buildByydResponse("1");
    }

    @Test
    public void onEmptyInput() throws Exception {
        if (getEndpoint().getProtocol().getRequestMethod() == HttpMethod.GET) {
            return; // skip yieldlab has nothing to send
        }
        // When
        ResultActions actions = mockMvc.perform(mockRequest().content(""));
        String publisherExtId = getRequestUrlPath().substring(getRequestUrlPath().lastIndexOf('/') + 1);

        // Then 

        // Content Returned
        HttpStatus httpStatus = getEndpoint().getProtocol().getResponseNobidStatus();
        actions.andExpect(MockMvcResultMatchers.status().is(httpStatus.value()));
        if (httpStatus == HttpStatus.NO_CONTENT) {
            actions.andExpect(MockMvcResultMatchers.content().string(""));
        }
        actions.andExpect(MockMvcResultMatchers.content().contentType(getEndpoint().getProtocol().getResponseMediaType()));
        actions.andExpect(MockMvcResultMatchers.header().string("Expires", "0"));
        actions.andExpect(MockMvcResultMatchers.header().string("Pragma", "No-Cache"));

        Exception expectedException = getEndpoint().getProtocol().getBrokenInputException();

        // Offence recorded
        Assertions.assertThat(offenceRegistry.values()).hasSize(1);
        OffenceSection section = offenceRegistry.getSection(publisherExtId);
        Assertions.assertThat(section.values()).hasSize(1);
        //section.values().iterator().next()[0].getOffence().printStackTrace();
        BidExceptionStats[] stats = section.getStats(expectedException.getClass());
        if (stats == null) {
            Assertions.fail("Expected to find offence for " + expectedException + " in " + section);
        }
        Assertions.assertThat(stats).hasSize(1);

        Assertions.assertThat(stats[0].getCount()).isEqualTo(1);
        Assertions.assertThat(stats[0].getOffence()).isInstanceOf(expectedException.getClass());
        TroubledBidRequest[] snapshot = stats[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId);

        Assertions.assertThat(snapshot[0].getExceptionMessage()).startsWith(expectedException.getMessage());
        RtbExecutionContext<?, ?> context = snapshot[0].getExecutionContext();
        Assertions.assertThat(context.getHttpContext().getPublisherExternalId()).isEqualTo(publisherExtId);
        Assertions.assertThat(context.getHttpContext().getWinUrlPath()).isNotNull();
        Assertions.assertThat(context.getHttpContext().getHttpRequest()).isInstanceOf(MockHttpServletRequest.class);
        Assertions.assertThat(context.getHttpContext().getHttpResponse()).isInstanceOf(MockHttpServletResponse.class);
        Assertions.assertThat(context.getRtbRequest()).isNull();
        Assertions.assertThat(context.getRtbResponse()).isNull();
        Assertions.assertThat(context.getByydRequest()).isNull();
        Assertions.assertThat(context.getByydResponse()).isNull();

        // Not Logged (java.util.logging)
        List<LogRecord> nonDebugMessages = LogCapturingHandler.get().list().stream().filter(record -> record.getLevel().intValue() > Level.INFO.intValue())
                .collect(Collectors.toList());
        Assertions.assertThat(nonDebugMessages).isEmpty();

        // FreqLogr captured exception
        Map<Class<? extends Exception>, ConcurrentLinkedQueue<ExceptionData>> xstorage = FreqLogr.getXstorage();
        Assertions.assertThat(xstorage).hasSize(1);
        ConcurrentLinkedQueue<ExceptionData> xqueue = xstorage.get(expectedException.getClass());
        Assertions.assertThat(xqueue).hasSize(1);
        ExceptionData xdata = xqueue.peek();
        String[] xmessages = xdata.getMessages().snapshot();
        //Assertions.assertThat(lastLogRecord.getLoggerName()).isEqualTo(RtbBidSequence.class.getName());//getController().getClass().getName());
        Assertions.assertThat(xmessages[0]).isEqualTo("Bid failed for publisher " + publisherExtId);

        Throwable throwable = xdata.getException();
        Assertions.assertThat(throwable).isInstanceOf(expectedException.getClass());
        Assertions.assertThat(throwable.getMessage()).startsWith(expectedException.getMessage());

    }

    /**
     * "{}" -> 204 NoBid
     */
    @Test
    public void http204_OnEmptyJson() throws Exception {
        // When
        ResultActions actions = mockMvc.perform(mockRequest().content("{\"x\":\"y\"}"));

        // Then

        // Mocks invoked
        Mockito.verify(backupLoggerMock).startControllerRequest();

        // Content Returned
        HttpStatus httpStatus = getEndpoint().getProtocol().getResponseNobidStatus();
        actions.andExpect(MockMvcResultMatchers.status().is(httpStatus.value()));
        if (httpStatus == HttpStatus.NO_CONTENT) {
            actions.andExpect(MockMvcResultMatchers.content().string(""));
        }
        actions.andExpect(MockMvcResultMatchers.content().contentType(getEndpoint().getProtocol().getResponseMediaType()));
        actions.andExpect(MockMvcResultMatchers.header().string("Expires", "0"));
        actions.andExpect(MockMvcResultMatchers.header().string("Pragma", "No-Cache"));
        MvcResult mvcResult = actions.andReturn();

        String requestJson = IOUtils.toString(mvcResult.getRequest().getReader());

        String publisherExtId = getRequestUrlPath().substring(getRequestUrlPath().lastIndexOf('/') + 1);
        ByydRequest byydRequest = new ByydRequest(publisherExtId, null);
        NoBidException nbxPrototype = new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "request app/site");

        // Offence recorded
        Assertions.assertThat(offenceRegistry.values()).hasSize(1);
        OffenceSection section = offenceRegistry.getSection(publisherExtId);
        Assertions.assertThat(section.values()).hasSize(1);
        //section.values().iterator().next()[0].getOffence().printStackTrace();
        BidExceptionStats[] exceptionStats = section.getStats(NoBidException.class);
        Assertions.assertThat(exceptionStats).hasSize(1);
        Assertions.assertThat(exceptionStats[0].getCount()).isEqualTo(1);
        Exception offence = exceptionStats[0].getOffence();
        Assertions.assertThat(offence).isInstanceOf(NoBidException.class);
        Assertions.assertThat(((NoBidException) offence).getNoBidReason()).isEqualTo(NoBidReason.REQUEST_INVALID);
        Assertions.assertThat(((NoBidException) offence).getOffenceName()).isEqualTo(AdSrvCounter.MISS_FIELD.name());
        Assertions.assertThat(((NoBidException) offence).getOffenceValue()).isEqualTo("request app/site");
        Assertions.assertThat(((NoBidException) offence).getByydRequest().getId()).isNull();
        Assertions.assertThat(((NoBidException) offence).getAdSpace()).isNull();
        Assertions.assertThat(offence.getMessage()).isEqualTo(nbxPrototype.getMessage());
        TroubledBidRequest[] snapshot = exceptionStats[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId);
        Assertions.assertThat(snapshot[0].getCapturedAt()).isLessThanOrEqualTo(System.currentTimeMillis());
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nbxPrototype.getMessage());
        RtbExecutionContext<?, ?> context = snapshot[0].getExecutionContext();
        Assertions.assertThat(context.getHttpContext().getPublisherExternalId()).isEqualTo(publisherExtId);
        Assertions.assertThat(context.getPublisherExternalId()).isEqualTo(publisherExtId);
        Assertions.assertThat(context.getRtbRequest()).isInstanceOf(getEndpoint().getBidRequestClass());
        Assertions.assertThat(context.getRtbRequestContent()).isEqualTo(requestJson);
        Assertions.assertThat(context.getRtbRequestParsedAt()).isNotNull();
        Assertions.assertThat(context.getByydRequest()).isNull();

        //And logged
        //Assertions.assertThat(LogCapturingHandler.get().list()).hasSize(1);

        //for (ILoggingEvent event : LogbackCapturingAppender.get().list()) {
        //    System.err.println(event);
        //}
        /*
        List<ILoggingEvent> nonCounterMessages = LogbackCapturingAppender.get().list().stream().filter(record -> !record.getMessage().contains("Created counter:"))
                .collect(Collectors.toList());
        ILoggingEvent logRecord = nonCounterMessages.get(nonCounterMessages.size() - 1);

        Assertions.assertThat(logRecord.getMessage()).isEqualTo(String.valueOf(nbxPrototype));
        Assertions.assertThat(logRecord.getLoggerName()).isEqualTo(RtbBidSequence.class.getName());//getController().getClass().getName());
        */
    }
}
