package com.adfonic.adserver.controller.dbg;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Instant;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.controller.dbg.DebugBidContext.CreativePurpose;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgCreativeEliminationDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgCreativePickedDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgNoBidDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgTargetingDto;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.controller.rtb.AdXBidAdapter;
import com.adfonic.adserver.controller.rtb.OpenRtbV2BidAdapter;
import com.adfonic.adserver.controller.rtb.RtbBidSequence;
import com.adfonic.adserver.controller.rtb.RtbEndpoint;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.controller.rtb.RtbHttpContext;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.impl.RtbBidLogicImpl;
import com.adfonic.adserver.rtb.mapper.AdXMapper;
import com.adfonic.adserver.rtb.mapper.AppNexusV2Mapper;
import com.adfonic.adserver.rtb.mapper.OpenXMapper;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.LocationTargetDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

@Controller
public class DebugBidController {

    public static final String DBG_WIN_URL = "/rtb/debug/win";
    public static final String DBG_LOSS_URL = "/rtb/debug/loss";
    public static final String DBG_IMPRESSION_URL = "/rtb/debug/bc";
    public static final String DBG_CLICK_THROUGH_URL = "/rtb/debug/ct";
    public static final String DBG_CLICK_REDIRECT_URL = "/rtb/debug/cr";

    private static final transient Logger LOG = LoggerFactory.getLogger(DebugBidController.class.getName());

    @Autowired
    private RtbBidLogicImpl rtbLogic;

    @Autowired
    private DynamicProperties dynaProps;

    @Autowired
    private AppNexusV2Mapper appnexusMapper;

    @Autowired
    private AdXMapper adxMapper;

    @Autowired
    private RtbFisherman fisher;

    @Autowired
    private AdServerStats counterManager;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Autowired
    private DomainCacheManager domainCacheManager;

    @Autowired
    private DebugBidUiController debugUiController;

    @Autowired
    private BackupLogger backupLogger;

    @Autowired
    private BackupLoggingRtbBidEventListener loggingListener;

    @Autowired
    private OffenceRegistry offenceRegistry;

    // Cannot use Spring MVC integrated json mapper as it is configured specifically for RTB controllers
    // So responses are marshalled manually here... 
    public static final ObjectMapper debugJsonMapper = new ObjectMapper();
    static {
        debugJsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        debugJsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        debugJsonMapper.setDateFormat(new ISO8601DateFormat());
        //debugJsonMapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
        //debugJsonMapper.configure(org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        debugJsonMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        debugJsonMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        debugJsonMapper.addMixIn(SegmentDto.class, JacksonMixin.class);
        debugJsonMapper.registerModule(new JodaTimeModule());
        debugJsonMapper.registerModule(new MetricsModule(TimeUnit.MINUTES, TimeUnit.MILLISECONDS, true));
    }

    public static abstract class JacksonMixin {

        @JsonIgnore
        //this can be extremely big collection
        private Set<LocationTargetDto> locationTargets = new HashSet<LocationTargetDto>();

        //@JsonIgnore
        //private Set<Long> geotargetIds = new HashSet<Long>();

        //@JsonIgnore
        //private Set<String> ipAddresses = new HashSet<String>();
    }

    public static class JodaInstantSerializer extends StdScalarSerializer<Instant> {

        private static final long serialVersionUID = 1L;

        public JodaInstantSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonGenerationException {
            String dateTimeAsString = ISODateTimeFormat.dateTime().print(instant);
            jsonGenerator.writeString(dateTimeAsString);
        }
    }

    @PostConstruct
    public void init() {
        //AppNexus is not used directly. Only through Orange and Millennial (with same bid request mapper)
        OpenRtbV2BidAdapter<AppNexusBidRequest, BidResponse> appNexusAdapter = new OpenRtbV2BidAdapter<AppNexusBidRequest, BidResponse>(appnexusMapper, AppNexusBidRequest.class,
                BidResponse.class);
        RtbExchange.Appnexus.setAdapter(appNexusAdapter);
        RtbExchange.Orange.setAdapter(appNexusAdapter);
        RtbExchange.Millennial.setAdapter(appNexusAdapter);
        RtbExchange.AdX.setAdapter(new AdXBidAdapter(adxMapper));
    }

    /**
     * Handler request from lite UI
     */
    @RequestMapping(value = "/rtb/debug/bidpost", method = RequestMethod.POST)
    public void debugUiPost(@RequestParam("exchange") String exchangeIdent, @RequestParam("bidbody") String bidbody, @RequestParam(name = "debug", required = false) Boolean debug,//
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

        DebugBidHttpServletRequest httpRequestWrapper;
        RtbExchange exchange = RtbExchange.lookup(exchangeIdent);
        if (exchange.getEndpoint() == RtbEndpoint.DcAdX) {
            byte[] bytes = AdXMapper.protoText2Bytes(removeProtobufUndefinedFields(bidbody));
            httpRequestWrapper = new DebugBidHttpServletRequest(httpRequest, bytes, "application/octet-stream");
        } else if (exchange.getEndpoint() == RtbEndpoint.OpenX) {
            byte[] bytes = OpenXMapper.protoText2Bytes(removeProtobufUndefinedFields(bidbody));
            httpRequestWrapper = new DebugBidHttpServletRequest(httpRequest, bytes, "application/octet-stream");
        } else if (exchange.getEndpoint() == RtbEndpoint.YieldLab) {
            Map<String, String> parameters = splitUrlQuery(bidbody);
            httpRequestWrapper = new DebugBidHttpServletRequest(httpRequest, parameters, "");
        } else {
            final byte[] bytes = bidbody.getBytes(Charset.forName("utf-8"));
            httpRequestWrapper = new DebugBidHttpServletRequest(httpRequest, bytes, "application/json;charset=utf-8");
        }
        if (Boolean.TRUE.equals(debug)) {
            String creativeSpec = httpRequest.getParameter("creativeSpec");
            String pCreativePurpose = httpRequest.getParameter("creativePurpose");
            CreativePurpose creativePurpose = StringUtils.isNotBlank(pCreativePurpose) ? CreativePurpose.valueOf(pCreativePurpose) : null;
            debugBidRequest(httpRequestWrapper, httpResponse, exchangeIdent, creativeSpec, creativePurpose);
        } else {
            // Execute request like original exchange bid controller
            RtbHttpContext httpContext = new RtbHttpContext(exchange.getEndpoint(), exchange.getPublisherExternalId(), httpRequestWrapper, httpResponse, Constant.WIN_URL_PATH);
            RtbBidSequence bidSequence = new RtbBidSequence(exchange.getEndpoint(), exchange.getAdapter(), rtbLogic, backupLogger, loggingListener, offenceRegistry, fisher,
                    counterManager);
            bidSequence.execute(httpContext);
        }

    }

    /**
     * Execute Bid request like from normal Bidding Enpoint does but collect and print debugging messages into output
     * 
     * @param publisherId - ExternalId or short name (mopub/appnexus/...)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/rtb/debug/bid/{publisherId}", method = RequestMethod.POST)
    public void debugBidRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse,// 
            @PathVariable("publisherId") String publisherId,//
            @RequestParam(value = "creativeSpec", required = false) String creativeSpec, // 
            @RequestParam(value = "creativeEnforce", required = false) CreativePurpose creativePurpose) throws IOException {

        // Rest call with Accept header 
        String acceptHeader = httpRequest.getHeader("Accept");
        boolean restJsonResponse = acceptHeader != null && acceptHeader.contains("json");

        RtbExchange exchange = RtbExchange.lookup(publisherId);
        DbgBidDto dbgBid = new DbgBidDto(exchange);

        OffenceRegistry offenceRegistry = new OffenceRegistry(10, 10);
        DebugBackupLogger backupLogger = new DebugBackupLogger();
        List<String> messages = new ArrayList<String>();
        DebugRtbBidEventListener bidEventListener = new DebugRtbBidEventListener(dbgBid, messages);

        Long debugCreativeId = null;
        if (StringUtils.isNotEmpty(creativeSpec)) {
            AdserverDomainCache adCache = adserverCacheManager.getCache();
            CreativeDto creative = DbgUiUtil.findCreative(creativeSpec, adCache);
            if (creative == null) {
                throw new IllegalArgumentException("Creative not in cache: " + creativeSpec);
            }
            debugCreativeId = creative.getId();
        }
        DebugBidContext debugContext = new DebugBidContext(debugCreativeId, creativePurpose);
        httpRequest.setAttribute(TargetingContext.DEBUG_CONTEXT, debugContext);
        dbgBid.setDebugContext(debugContext);

        TargetingEventListener bidTargetListener = new DebugTargetingEventListener(dbgBid, messages, null);
        dbgBid.setBiddingEvents(messages);

        DebugBidHttpServetResponse httpResponseWrapper = new DebugBidHttpServetResponse(httpResponse);

        RtbHttpContext httpContext = new RtbHttpContext(exchange.getEndpoint(), exchange.getPublisherExternalId(), httpRequest, httpResponseWrapper, DBG_WIN_URL);
        RtbBidSequence bidSequence = new RtbBidSequence(exchange.getEndpoint(), exchange.getAdapter(), rtbLogic, backupLogger, bidEventListener, offenceRegistry, fisher,
                counterManager);

        RtbExecutionContext executionCtx = null;
        try {
            executionCtx = bidSequence.execute(httpContext, bidEventListener, bidTargetListener);
        } catch (Exception exception) {
            LOG.error("Bid debug failed: " + exception, exception);
            while (exception.getCause() != null && exception.getCause() instanceof Exception) {
                exception = (Exception) exception.getCause();
            }
            dbgBid.setException(exception);
        } finally {

            if (executionCtx != null) {
                Exception exception = executionCtx.getException();
                if (exception != null && dbgBid.getException() == null) {
                    if (exception instanceof NoBidException) {
                        NoBidException nbx = (NoBidException) exception;
                        dbgBid.setNobidReason(new DbgNoBidDto(nbx.getNoBidReason(), nbx.getOffenceName(), String.valueOf(nbx.getOffenceValue())));
                    } else {
                        dbgBid.setException(exception);
                    }
                }
                dbgBid.setRtbRequest(executionCtx.getRtbRequestString());
                dbgBid.setRtbResponse(executionCtx.getRtbResponseString());

                ByydRequest byydRequest = executionCtx.getByydRequest();
                if (byydRequest != null) {
                    dbgBid.setByydRequest(byydRequest);
                    TargetingContext targetingContext = bidEventListener.getContext();
                    if (targetingContext != null) {
                        dbgBid.setTargetingContext(toStringMap(targetingContext));
                    }
                }
                dbgBid.setByydResponse(executionCtx.getByydResponse());
            }
            httpResponse.setStatus(200);
            if (restJsonResponse) {
                httpResponse.setContentType("application/json;charset=utf-8");
                debugJsonMapper.writeValue(httpResponse.getOutputStream(), dbgBid);
            } else {
                httpResponse.setContentType("text/html");
                debugUiController.printBidOutput(httpResponse.getWriter(), dbgBid, executionCtx, adserverCacheManager.getCache(), domainCacheManager.getCache(), dynaProps);
            }
        }
    }

    private static Map<String, String> toStringMap(TargetingContext context) {
        Map<String, String> result = new TreeMap<String, String>();

        Map<String, Object> contextMap = context.getAttributes();
        for (Entry<String, Object> entry : contextMap.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * Yieldlab uses , separated multivalues instead of repeated parameter 
     */
    public static Map<String, String> splitUrlQuery(String query) {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String[] pairs = query.trim().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException uex) {
                throw new IllegalStateException("No utf-8", uex);
            }
        }
        return query_pairs;
    }

    /**
     * AdX is sending protobuf request with undefined fields. 
     * TextFormat cannot figure out such fields names when prints textual representation and so it prints numbers from binary format 
     * so when TextFormat parses textual representation back, it fails on this numbers 
     */
    private String removeProtobufUndefinedFields(String protobufTextMessage) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(protobufTextMessage));
        Writer sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] strings = line.split(":");
            if (strings.length > 1) {
                if (DbgUiUtil.tryToLong(strings[0].trim()) == null) {
                    pw.println(line);
                }
            } else {
                pw.println(line);
            }
        }
        return sw.toString();
    }

    public static class DebugTargetingEventListener implements TargetingEventListener {

        private final List<String> messages;
        private final Long creativeId; //nullable
        private final DbgBidDto dbgBid;
        private DbgTargetingDto dbgTarget = new DbgTargetingDto();
        private TargetingContext context;

        public DebugTargetingEventListener(DbgBidDto dbgBid, List<String> messages) {
            this(dbgBid, messages, null);
        }

        public DebugTargetingEventListener(DbgBidDto dbgBid, List<String> messages, Long creativeId) {
            this.dbgBid = dbgBid;
            this.dbgTarget = new DbgTargetingDto();
            this.dbgBid.setTargetingInfo(dbgTarget);
            if (messages == null) {
                throw new IllegalArgumentException("Null messages");
            }
            this.messages = messages;
            this.creativeId = creativeId;
        }

        public TargetingContext getContext() {
            return context;
        }

        /**
         * Elimination of whole AdSpace at the beginning of the targetting because request is missing something
         */
        @Override
        public void unfilledRequest(AdSpaceDto adSpace, TargetingContext context) {
            UnfilledReason unfilledReason = context.getAttribute(TargetingContext.UNFILLED_REASON);
            String message = "adSpace: " + adSpace.getId() + " unfilledRequest " + unfilledReason;
            LOG.info(message);
            dbgBid.addBiddingEvent(message);
            this.context = context;
        }

        /**
         * creativesEligible is called immediately after this
         */
        @Override
        public void attributesDerived(AdSpaceDto adSpace, TargetingContext context) {
            String message = "adSpace: " + adSpace.getId() + " attributesDerived, Formats: " + adSpace.getFormatIds() + ", Integration: "
                    + adSpace.getPublication().getDefaultIntegrationTypeId();
            LOG.info(message);
            messages.add(message);
            this.context = context;
        }

        /**
         * Basically just context.getAdserverDomainCache().getEligibleCreatives(adSpace.getId())
         */
        @Override
        public void creativesEligible(AdSpaceDto adSpace, TargetingContext context, AdspaceWeightedCreative[] eligibleCreatives) {
            String message = "adSpace: " + adSpace.getId() + " eligible creatives: " + Arrays.asList(eligibleCreatives);
            LOG.info(message);
            dbgTarget.setEligible(eligibleCreatives);
            this.context = context;
        }

        @Override
        public void creativeEliminated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, CreativeEliminatedReason reason, String detailedReason) {
            this.context = context;
            if (creativeId == null || creativeId.equals(creative.getId())) {
                String message = "adSpace: " + adSpace.getId() + " creative: " + creative.getId() + " eliminated: " + reason + " message: " + detailedReason;
                LOG.info(message);
                dbgTarget.addEliminated(new DbgCreativeEliminationDto(creative.getId(), reason, detailedReason));
            }
        }

        @Override
        public void creativeSelected(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative) {
            this.context = context;
            if (creativeId == null || creativeId.equals(creative.getId())) {
                String message = "adSpace: " + adSpace.getId() + " creative: " + creative.getId() + " selected";
                LOG.info(message);
                dbgTarget.addSelected(creative.getId());
            }
        }

        @Override
        public void creativesTargeted(AdSpaceDto adSpace, TargetingContext context, int priority, List<MutableWeightedCreative> targetedCreatives) {
            this.context = context;
            String message = "adSpace: " + adSpace.getId() + " creativesTargeted: " + new LinkedList<>(targetedCreatives);
            LOG.info(message);
            for (MutableWeightedCreative mwCreative : targetedCreatives) {
                dbgTarget.addTargeted(new DbgCreativePickedDto(priority, mwCreative.getCreative().getId(), mwCreative.getEcpmWeight()));
            }
        }

        @Override
        public void timeLimitExpired(AdSpaceDto adSpace, TargetingContext context, TimeLimit timeLimit) {
            this.context = context;
            String message = "adSpace: " + adSpace.getId() + " timeLimitExpired timeLimit: " + timeLimit;
            LOG.info(message);
            messages.add(message);
            dbgBid.addBiddingEvent("Bidding reached time limit: " + timeLimit.getDuration());
        }
    }

    public static class DebugRtbBidEventListener implements RtbBidEventListener {

        private final List<String> messages;

        private final DbgBidDto dbgBid;

        private TargetingContext context;

        public DebugRtbBidEventListener(DbgBidDto bidDto, List<String> messages) {
            this.messages = messages;
            this.dbgBid = bidDto;
        }

        private String getAdSpaceId(TargetingContext context) {
            this.context = context;
            AdSpaceDto adSpace = context.getAdSpace();
            if (adSpace != null) {
                return String.valueOf(adSpace.getId());
            } else {
                return "?";
            }
        }

        public TargetingContext getContext() {
            return context;
        }

        /**
         * When something nasty happens inside RTB Mapper...
         */
        @Override
        public void bidRequestRejected(String publisherExternalID, String bidRequestID, String reason) {
            String message = "Bid rejected: Exchange: " + publisherExternalID + ", BidRequestID: " + bidRequestID + ", Reason: " + reason;
            messages.add(message);
            LOG.info(message);
            dbgBid.addBiddingEvent("Bid unacceptable: " + reason);
        }

        /**
         * After this event, NoBidException is usually thrown with TECHNICAL_ERROR, IMPRESSION_VIOLATES_FILTER or IMPRESSION_NOT_NEEDED reason
         */
        @Override
        public void bidRequestRejected(TargetingContext context, ByydRequest bidRequest, String reason) {
            String message = "Bid rejected: AdSpace: " + getAdSpaceId(context) + " BidRequestID: " + bidRequest.getId() + ", Reason: " + reason;
            messages.add(message);
            LOG.info(message);
            dbgBid.addBiddingEvent("Bid rejected: " + reason);
        }

        /**
         * Do not bid on Imp
         * This might be theoretically called multiple times if there are more Imps in request
         */
        @Override
        public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, String reason) {
            String impId = imp != null ? imp.getImpid() : "?";
            String message = "Bid not made: AdSpace: " + getAdSpaceId(context) + ", BidRequestID: " + bidRequest.getId() + ", Imp: " + impId + ", Reason: " + reason;
            messages.add(message);
            LOG.info(message);
            if (imp != null) {
                dbgBid.addImpressionNoBid(imp, reason);
            } else {
                //On NoBidException imp==null. We do not need to cover that case...
                //dbgBid.addBiddingEvent("Bid not made: " + reason);
            }
        }

        @Override
        public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, Exception exception) {
            dbgBid.setException(exception);
            bidNotMade(context, bidRequest, imp, String.valueOf(exception));
        }

        /**
         * Bid on Imp
         * This might be theoretically called multiple times if there are more Imps in request
         */
        @Override
        public void bidMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, ByydBid bid, Impression impression, SelectedCreative selectedCreative) {
            String message = "adSpace: " + getAdSpaceId(context) + " bidMade, impression: " + impression.getExternalID() + ", creative: " + selectedCreative.getCreative().getId()
                    + ", EcpmWeight:" + selectedCreative.getEcpmWeight();
            messages.add(message);
            LOG.info(message);
            dbgBid.setImpressionBid(bid, selectedCreative, impression);
        }

        /**
         * Bid can still be made...theoretically if we have already some selected/targeted creative
         */
        @Override
        public void timeLimitExpired(TargetingContext context, ByydRequest bidRequest, TimeLimit timeLimit) {
            String message = "adSpace: " + getAdSpaceId(context) + " timeLimitExpired , bidRequest: " + bidRequest + ", timeLimit: " + timeLimit;
            messages.add(message);
            LOG.info(message);
            dbgBid.addBiddingEvent("Targeting reached time limit: " + timeLimit.getDuration());
        }

    }

    static class DebugBackupLogger implements BackupLogger {

        @Override
        public void startFilterRequest() {
            // TODO Auto-generated method stub
        }

        @Override
        public void startControllerRequest() {
            // TODO Auto-generated method stub
        }

        @Override
        public void endFilterRequest() {
            // TODO Auto-generated method stub
        }

        @Override
        public void logBidServed(Impression impression, Date eventTime, TargetingContext context, ByydRequest bidRequest) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logAdServed(Impression impression, Date eventTime, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logImpression(Impression impression, Date eventTime, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logUnfilledRequest(UnfilledReason unfilledReason, Date eventTime, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logAdRequestFailure(String reason, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logRtbBidSuccess(Impression impression, BigDecimal price, Date eventTime, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logRtbBidFailure(String reason, TargetingContext context, ByydRequest req, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logRtbLoss(Impression impression, Date eventTime, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logRtbWinSuccess(Impression impression, BigDecimal settlementPrice, Date eventTime, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logRtbWinFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logBeaconSuccess(Impression impression, Date eventTime, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logBeaconFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logBeaconFailure(Impression impression, String reason, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logClickSuccess(Impression impression, AdSpaceDto adSpace, Date eventTime, Long campaignId, TargetingContext context) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logClickFailure(String impressionExternalID, String reason, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }

        @Override
        public void logClickFailure(Impression impression, String reason, TargetingContext context, String... extraValues) {
            // TODO Auto-generated method stub
        }
    }

}

class DebugBidHttpServetResponse extends HttpServletResponseWrapper {

    private PrintWriter writer;
    private StringWriter stringWriter;
    private ServletOutputStream stream;

    public DebugBidHttpServetResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            stringWriter = new StringWriter();
            writer = new PrintWriter(stringWriter);
        }
        return writer;
    }

    @Override
    public void setContentType(String type) {
        //super.setContentType(type);
    }

    @Override
    public void setHeader(String name, String value) {
        //super.setHeader(name, value);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null) {
            stream = new FakeServletOutputStream();
        }
        return stream;
    };

    static class FakeServletOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        public byte[] getContent() {
            return byteStream.toByteArray();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(int b) throws IOException {
            byteStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            byteStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byteStream.write(b, off, len);
        }

    }
}

class DebugBidHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] body;

    private String contentType;

    private HttpServletRequest httpRequest;

    private Map<String, String> parameters;

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public DebugBidHttpServletRequest(HttpServletRequest httpRequest, byte[] body, String contentType) {
        super(httpRequest);
        this.httpRequest = httpRequest;
        this.body = body;
        this.contentType = contentType;
    }

    public DebugBidHttpServletRequest(HttpServletRequest httpRequest, Map<String, String> parameters, String contentType) {
        super(httpRequest);
        this.httpRequest = httpRequest;
        this.parameters = parameters;
    }

    @Override
    public String getMethod() {
        if (body != null) {
            return "POST";
        } else {
            return "GET";
        }
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    /*
        @Override
        public Enumeration<String> getParameterNames() {
            return super.getParameterNames();
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return super.getParameterMap();
        }
    */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new DelegatingServletInputStream(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(new String(body, Charset.forName("utf-8"))));
    }

    @Override
    public String getHeader(String name) {
        if ("Content-Type".equals(name)) {
            return contentType;
        } else {
            return null;
        }
        //return httpRequest.getHeader(name);
    }
}

class DelegatingServletInputStream extends ServletInputStream {

    private final InputStream sourceStream;

    /**
     * Create a DelegatingServletInputStream for the given source stream.
     * @param sourceStream the source stream (never <code>null</code>)
     */
    public DelegatingServletInputStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
    }

    /**
     * Return the underlying source stream (never <code>null</code>).
     */
    public final InputStream getSourceStream() {
        return this.sourceStream;
    }

    @Override
    public int read() throws IOException {
        return this.sourceStream.read();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.sourceStream.close();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        //ignore
    }
}

class JodaTimeModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public JodaTimeModule() {
        super();
        addSerializer(Instant.class, new JodaInstantSerializer());
    }

    static class JodaInstantSerializer extends StdScalarSerializer<Instant> {

        private static final long serialVersionUID = 1L;

        public JodaInstantSerializer() {
            super(Instant.class);
        }

        @Override
        public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonGenerationException {
            String dateTimeAsString = ISODateTimeFormat.dateTime().print(instant);
            jsonGenerator.writeString(dateTimeAsString);
        }
    }
}
