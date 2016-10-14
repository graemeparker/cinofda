package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.controller.rtb.RtbEndpoint.RtbProtocol;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.offence.OffenceRegistry.BidExceptionStats;
import com.adfonic.adserver.offence.OffenceSection;
import com.adfonic.adserver.offence.TroubledBidRequest;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.TargetingContextUtil;
import com.adfonic.adserver.rtb.util.TargetingContextUtil.ContextBuilder;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.util.stats.CounterManager;
import com.adfonic.util.stats.FreqLogr;

/**
 * Abstract base class
 */
public abstract class AbstractBidTest<C> {

    static {
        System.setProperty(FreqLogr.FLUSH_SYSPRO, "9000000"); // long enough to actually do not flush at all
    }
    // Mapper dependencies
    @Mock
    protected TargetingContextUtil ctxtUtil;
    @Mock
    protected ContextBuilder contextBuilder;
    @Mock
    protected TargetingContext targetingContext;

    // Controller dependencies
    @Mock
    protected RtbBidLogic rtbLogicMock;

    @Mock
    protected RtbWinLogicImpl rtbWinLogicMock;

    @Mock
    protected BackupLoggingRtbBidEventListener bidListenerMock;
    @Mock
    protected BackupLogger backupLoggerMock;

    @Mock
    private AdserverDomainCacheManager adCacheManager;

    protected AdServerStats counterManager;

    protected OffenceRegistry offenceRegistry;

    protected RtbFisherman fisherman;

    protected MockMvc mockMvc;

    private C controller;

    protected abstract C buildController();

    protected abstract String getRequestUrlPath();

    protected abstract RtbEndpoint getEndpoint();

    protected String getPublisherExtId() {
        return getRequestUrlPath().substring(getRequestUrlPath().lastIndexOf('/') + 1);
    }

    @Before
    public void before() {

        FreqLogr.getXstorage().clear();
        MockitoAnnotations.initMocks(this);
        Mockito.when(ctxtUtil.builder()).thenReturn(contextBuilder);
        Mockito.when(contextBuilder.set(Mockito.anyString(), Mockito.anyObject())).thenReturn(contextBuilder);
        Mockito.when(contextBuilder.get()).thenReturn(targetingContext);

        fisherman = new RtbFisherman();
        offenceRegistry = new OffenceRegistry(10, 10);
        counterManager = new AdServerStats(new CounterManager(), adCacheManager);

        mockMvc = MockMvcBuilders.standaloneSetup(buildController()).build();//.setMessageConverters(array).build();
        LogCapturingHandler.install(true);
        LogbackCapturingAppender.install(true);

        AdServerFeatureFlag.OFFENCE_REGISTRY.setEnabled(true);
    }

    @After
    public void after() {
        LogCapturingHandler.remove();
        LogbackCapturingAppender.remove();
        offenceRegistry = null;
    }

    public C getController() {
        if (controller == null) {
            controller = buildController();
        }
        return controller;
    }

    /**
     * Create Byyd response returnesd from RtbLogic 
     * Response should match to request returned from getBidHttpRequest()
     */
    ByydResponse getByydResponse() throws IOException {
        return buildByydResponse("1");
    }

    /**
     * Create valid bid request 
     */
    MockHttpServletRequestBuilder getBidHttpRequest() throws IOException {
        byte[] rtbBidRawPayload = getBidRequestPayload();
        return mockRequest().content(rtbBidRawPayload);
    }

    abstract byte[] getBidRequestPayload() throws IOException;

    protected void setRtbLogicException(Exception exception) throws NoBidException {
        ByydResponse invocation = rtbLogicMock.bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class));
        Mockito.when(invocation).thenThrow(exception);
    }

    /**
     * Set RtbLogic response
     */
    protected void setRtbLogicResponse(final com.adfonic.adserver.rtb.nativ.ByydResponse byydResponse) throws NoBidException {

        final AdSpaceDto adSpaceDto = new AdSpaceDto();
        adSpaceDto.setId(1l);
        adSpaceDto.setExternalID("adspace-ext-id");
        Mockito.when(targetingContext.getAdSpace()).thenReturn(adSpaceDto);

        ByydRequest byydRequest = new ByydRequest(getPublisherExtId(), "");
        byydRequest.setAdSpace(adSpaceDto);

        Mockito.when(rtbLogicMock.bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class))).thenAnswer(
                new Answer<ByydResponse>() {

                    @Override
                    public ByydResponse answer(InvocationOnMock invocation) throws Throwable {
                        RtbExecutionContext rtbContext = (RtbExecutionContext) invocation.getArguments()[0];
                        rtbContext.getByydRequest().setAdSpace(adSpaceDto);
                        // simulate side effects we do inside RtbLogicImpl.getBidResponse...
                        return byydResponse;
                    }
                });

    }

    protected ByydResponse buildByydResponse(String impId) {
        ByydImp imp = new ByydImp(impId);
        imp.setW(320);
        imp.setH(50);
        ByydBid bid = new ByydBid(imp);
        bid.setAdid("adid-123456"); //ID that references the ad to be served if the bid wins.
        bid.setAdm("<xhtml>Actual ad markup</xhtml>"); //Actual ad markup. XHTML if a response to a banner object, or VAST XML if a response to a video object.
        bid.setAdomain("example.com"); //Advertiser’s primary or top-level domain for advertiser checking.
        bid.setAttr(new HashSet<Integer>(Arrays.asList(1))); //Array of creative attributes. See Table 6.3 Creative Attributes.
        bid.setCid("campaign-123456"); //Campaign ID or similar that appears within the ad markup
        bid.setCrid("creative-123456"); //Creative ID for reporting content issues or defects. This could also be used as a reference to a creative ID that is posted with an exchange.
        bid.setDestination("http://example.com/final/destination");

        bid.setIabId("IAB19-36");
        bid.setIurl("http://example.com/sample/image.gif"); // Sample image URL (without cache busting) for content checking
        bid.setNurl("http://example.com/rtb/win/notice"); //Win notice URL
        bid.setPrice(new BigDecimal(1)); //required - Bid price in CPM

        CreativeDto creative = new CreativeDto();
        creative.setId(1l);
        creative.setExternalID("cre-ext-id-1");
        creative.setName("unit test");
        creative.setCreationDate(new Date());
        bid.setCreative(creative);

        ByydResponse nResponse = new ByydResponse(new ByydRequest("publisher-ext-id", "req-id-123456"), bid);

        return nResponse;
    }

    /**
     * /invalid/path/ -> 404
     */
    @Test
    public void http404_OnWrongHttpUrl() throws Exception {
        // When
        RtbProtocol protocol = getEndpoint().getProtocol();
        MockHttpServletRequestBuilder mockBuilder = mockRequest("/invalid/path/{publisherExternalID}", protocol.getRequestMethod(), protocol.getRequestMediaType());
        ResultActions actions = mockMvc.perform(mockBuilder);
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isNotFound());
        // No offence
        Assertions.assertThat(offenceRegistry.values()).hasSize(0);
        // No logged 
        Assertions.assertThat(LogCapturingHandler.get().list()).isEmpty();
    }

    /**
     * GET -> 405 Method not allowed
     */
    @Test
    public void http405_OnWrongHttpMethod() throws Exception {
        // When
        HttpMethod httpMethod = getEndpoint().getProtocol().getRequestMethod();
        if (httpMethod == HttpMethod.GET) {
            httpMethod = HttpMethod.POST;
        } else {
            httpMethod = HttpMethod.GET;
        }
        MockHttpServletRequestBuilder mockBuilder = mockRequest(getRequestUrlPath(), httpMethod, getEndpoint().getProtocol().getRequestMediaType());
        ResultActions actions = mockMvc.perform(mockBuilder);
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
        // No offence
        Assertions.assertThat(offenceRegistry.values()).hasSize(0);
        // No logged 
        Assertions.assertThat(LogCapturingHandler.get().list()).isEmpty();
    }

    /**
     * text/plain -> 415 Unsupported media type
     */
    @Test
    public void http415_OnWrongContentType() throws Exception {
        HttpMethod method = getEndpoint().getProtocol().getRequestMethod();
        if (method == HttpMethod.GET) {
            //GET requests have no Content-Type
            return;
        }
        // When
        MockHttpServletRequestBuilder mockBuilder = mockRequest(getRequestUrlPath(), method, MediaType.TEXT_HTML);
        ResultActions actions = mockMvc.perform(mockBuilder);
        //Then
        actions.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());
        // No offence
        Assertions.assertThat(offenceRegistry.values()).hasSize(0);
        // No logged 
        Assertions.assertThat(LogCapturingHandler.get().list()).isEmpty();
    }

    @Test
    public void onBidMade() throws Exception {

        String publisherExtId = getRequestUrlPath().substring(getRequestUrlPath().lastIndexOf('/') + 1);
        setRtbLogicResponse(getByydResponse());
        // When
        MockHttpServletRequestBuilder bidRequest = getBidHttpRequest();
        ResultActions actions = mockMvc.perform(bidRequest);

        // Then

        // No offences
        if (!offenceRegistry.values().isEmpty()) {
            OffenceSection section = offenceRegistry.values().iterator().next();
            BidExceptionStats[] stats = section.values().iterator().next();
            Exception offence = stats[0].getOffence();
            Assertions.fail("Unexpected offence", offence);
        }

        // Mocks were called 
        Mockito.verify(rtbLogicMock).bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class));

        Mockito.verify(backupLoggerMock).startControllerRequest();

        // Response Returned
        actions.andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()));
        actions.andExpect(MockMvcResultMatchers.content().contentType(getEndpoint().getProtocol().getResponseMediaType()));
        actions.andExpect(MockMvcResultMatchers.header().string("Expires", "0"));
        actions.andExpect(MockMvcResultMatchers.header().string("Pragma", "No-Cache"));

        // Context filled
        RtbExecutionContext<?, ?> context = (RtbExecutionContext<?, ?>) actions.andReturn().getRequest().getAttribute(RtbExecutionContext.RTB_CONTEXT);
        //System.out.println(context.getRtbRequestContent());
        Assertions.assertThat(context.getPublisherExternalId()).isEqualTo(publisherExtId);
        Assertions.assertThat(context.getRtbRequest()).isInstanceOf(getEndpoint().getBidRequestClass());
        Assertions.assertThat(context.getRtbRequestContent()).isNotNull();
        //TODO Assertions.assertThat(context.getRtbRequestContent()).isEqualTo(getValidInput());
        Assertions.assertThat(context.getRtbRequestParsedAt()).isNotNull();
        Assertions.assertThat(context.getByydRequest()).isNotNull();
        Assertions.assertThat(context.getByydRequestMappedAt()).isNotNull();
        Assertions.assertThat(context.getByydResponse()).isNotNull();
        Assertions.assertThat(context.getByydResponseCreatedAt()).isNotNull();
        Assertions.assertThat(context.getRtbResponse()).isNotNull();
        Assertions.assertThat(context.getRtbResponseMappedAt()).isNotNull();

        //offenceRegistry.values().iterator().next().values().iterator().next()[0].getOffence().printStackTrace();

    }

    /**
     * RtbLogic throws NoBidException
     * Extremely common scenario > 95% requests
     */
    @Test
    public void onNoBidException() throws Exception {

        // Given
        String publisherExtId = getRequestUrlPath().substring(getRequestUrlPath().lastIndexOf('/') + 1);

        ByydRequest byydRequest = new ByydRequest(publisherExtId, "byyd-req-" + System.currentTimeMillis());
        NoBidException noBidException = new NoBidException(byydRequest, NoBidReason.NOTHING_TO_BID, AdSrvCounter.FORMAT_INVALID, "test-" + System.currentTimeMillis());
        setRtbLogicException(noBidException);

        // When
        ResultActions actions = mockMvc.perform(getBidHttpRequest());

        // Then

        // No offences
        if (!offenceRegistry.values().isEmpty()) {
            OffenceSection section = offenceRegistry.values().iterator().next();
            BidExceptionStats[] stats = section.values().iterator().next();
            Exception offence = stats[0].getOffence();
            Assertions.fail("Unexpected offence", offence);
        }

        // Mocks were called 
        Mockito.verify(rtbLogicMock).bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class));

        Mockito.verify(backupLoggerMock).startControllerRequest();

        // Content Returned
        HttpStatus httpStatus = getEndpoint().getProtocol().getResponseNobidStatus();
        actions.andExpect(MockMvcResultMatchers.status().is(httpStatus.value()));

        if (httpStatus == HttpStatus.NO_CONTENT) {
            //Rtb V2
            actions.andExpect(MockMvcResultMatchers.content().string(""));
        } else {
            //Rtb V1, protobuf...
            //actions.andExpect(MockMvcResultMatchers.content().string(""));
        }
        actions.andExpect(MockMvcResultMatchers.content().contentType(getEndpoint().getProtocol().getResponseMediaType()));
        actions.andExpect(MockMvcResultMatchers.header().string("Expires", "0"));
        actions.andExpect(MockMvcResultMatchers.header().string("Pragma", "No-Cache"));

        // Logged
        /*
        Assertions.assertThat(LogCapturingHandler.get().list()).hasSize(1);
        LogRecord lastLogRecord = LogCapturingHandler.get().last();
        Assertions.assertThat(lastLogRecord.getLoggerName()).isEqualTo(RtbBidSequence.class.getName());//getController().getClass().getName());
        Assertions.assertThat(lastLogRecord.getMessage()).isEqualTo(noBidException.toString());
        */
    }

    /**
     * RtbLogic throws NullPointerException
     */
    @Test
    public void onNullPointerException() throws Exception {

        NullPointerException nullException = new NullPointerException("test-" + System.currentTimeMillis());
        setRtbLogicException(nullException);

        MockHttpServletRequestBuilder mockHttpRequest = getBidHttpRequest();
        String publisherExtId = getPublisherExtId();

        // When
        ResultActions actions = mockMvc.perform(mockHttpRequest);

        // Then 

        // Ensure that mock was called 
        Mockito.verify(rtbLogicMock).bid(Mockito.any(RtbExecutionContext.class), Mockito.eq(bidListenerMock), Mockito.any(TargetingEventListener.class));
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

        // Offence Recorded
        Assertions.assertThat(offenceRegistry.values()).hasSize(1);
        OffenceSection section = offenceRegistry.getSection(publisherExtId);
        Assertions.assertThat(section.values()).hasSize(1);
        BidExceptionStats[] stats = section.getStats(NullPointerException.class);
        Assertions.assertThat(stats).hasSize(1);
        Assertions.assertThat(stats[0].getCount()).isEqualTo(1);
        Assertions.assertThat(stats[0].getOffence()).isEqualTo(nullException);
        TroubledBidRequest[] snapshot = stats[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getCapturedAt()).isLessThanOrEqualTo(System.currentTimeMillis());
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullException.getMessage());
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId);

        // Logged
        /*
        Assertions.assertThat(LogCapturingHandler.get().list()).hasSize(1);
        LogRecord lastLogRecord = LogCapturingHandler.get().last();
        Assertions.assertThat(lastLogRecord.getLoggerName()).isEqualTo(RtbBidSequence.class.getName());//getController().getClass().getName());
        String logMessage = String.format(UNEXPECTED_MSG, "Bid failed for " + publisherExtId);
        Assertions.assertThat(lastLogRecord.getMessage()).endsWith(logMessage);

        Throwable throwable = lastLogRecord.getThrown();
        Assertions.assertThat(throwable).isEqualTo(nullException);
        */
    }

    protected MockHttpServletRequestBuilder mockRequest() {
        RtbProtocol protocol = getEndpoint().getProtocol();
        return mockRequest(getRequestUrlPath(), protocol.getRequestMethod(), protocol.getRequestMediaType());
    }

    protected MockHttpServletRequestBuilder mockRequest(String urlPath, HttpMethod httpMethod, MediaType mediaType) {
        MockHttpServletRequestBuilder mockBuilder;
        if (httpMethod == HttpMethod.POST) {
            mockBuilder = MockMvcRequestBuilders.post(urlPath, "publ-extid-irelevant");
        } else if (httpMethod == HttpMethod.GET) {
            mockBuilder = MockMvcRequestBuilders.get(urlPath, "publ-extid-irelevant");
        } else {
            throw new IllegalStateException("Unsupported " + httpMethod);
        }
        if (mediaType != null) {
            mockBuilder.contentType(mediaType);
        }
        return mockBuilder;
    }

    public static class LogbackCapturingAppender extends AppenderBase<ILoggingEvent> {

        private static final String APPENDER_NAME = "test_intercept_appender";
        List<ILoggingEvent> storage = new ArrayList<>();

        public LogbackCapturingAppender() {
            setName(APPENDER_NAME);
            start();
        }

        @Override
        protected void append(ILoggingEvent e) {
            storage.add(e);
        }

        public List<ILoggingEvent> getStorage() {
            return storage;
        }

        public static LogbackCapturingAppender get() {
            return install(false);
        }

        public static void remove() {
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            LogbackCapturingAppender appender = (LogbackCapturingAppender) root.getAppender(APPENDER_NAME);
            if (appender != null) {
                root.detachAppender(appender);
            }
        }

        public static LogbackCapturingAppender install(boolean clear) {
            Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            LogbackCapturingAppender appender = (LogbackCapturingAppender) root.getAppender(APPENDER_NAME);
            if (appender == null) {
                appender = new LogbackCapturingAppender();
                LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
                appender.setContext(lc);
                root.addAppender(appender);
            } else if (clear) {
                appender.storage.clear();
            }
            return appender;
        }

        public synchronized List<ILoggingEvent> list() {
            return new ArrayList<ILoggingEvent>(storage);
        }
    }

    /**
     * AdServer is logging via java.util.logging
     */
    public static class LogCapturingHandler extends Handler {

        private static Level consoleLevel;

        public static LogCapturingHandler remove() throws SecurityException {
            java.util.logging.Logger rootLogger = getRootLogger();
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    //restore console
                    if (consoleLevel != null) {
                        handler.setLevel(consoleLevel);
                        consoleLevel = null;
                    }
                } else if (handler instanceof LogCapturingHandler) {
                    rootLogger.removeHandler(handler);
                    return (LogCapturingHandler) handler;
                }
            }
            return null;
        }

        public static LogCapturingHandler get() {
            return install(false);
        }

        public static LogCapturingHandler install(boolean clear) {
            java.util.logging.Logger rootLogger = getRootLogger();
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    //silence console
                    if (consoleLevel == null) {
                        consoleLevel = handler.getLevel();
                        handler.setLevel(Level.OFF);
                    }
                } else if (handler instanceof LogCapturingHandler) {
                    //is already installed
                    if (clear) {
                        ((LogCapturingHandler) handler).clear();
                    }
                    return (LogCapturingHandler) handler; //leave
                }
            }
            LogCapturingHandler handler = new LogCapturingHandler();
            rootLogger.addHandler(handler);
            return handler;
        }

        private static java.util.logging.Logger getRootLogger() {
            return LogManager.getLogManager().getLogger("");
        }

        private List<LogRecord> storage = new BoundedQueue<LogRecord>(100);

        public synchronized LogRecord last() {
            if (storage.size() > 0) {
                return storage.get(storage.size() - 1);
            } else {
                return null;
            }
        }

        public synchronized LogRecord first() {
            if (storage.size() > 0) {
                return storage.get(0);
            } else {
                return null;
            }
        }

        public synchronized List<LogRecord> list() {
            return new ArrayList<LogRecord>(storage);
        }

        @Override
        public synchronized void publish(LogRecord record) {
            storage.add(record);
        }

        public synchronized void clear() {
            storage.clear();
        }

        @Override
        public void flush() {
            //nothing
        }

        @Override
        public void close() throws SecurityException {
            //nothing
        }

        class BoundedQueue<E> extends LinkedList<E> {

            private static final long serialVersionUID = 1L;

            private int limit;

            public BoundedQueue(int limit) {
                this.limit = limit;
            }

            @Override
            public boolean add(E o) {
                super.add(o);
                while (size() > limit) {
                    super.remove();
                }
                return true;
            }
        }
    }

}
