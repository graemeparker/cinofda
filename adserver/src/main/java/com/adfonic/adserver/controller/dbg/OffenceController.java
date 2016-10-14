package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.controller.rtb.RtbHttpContext;
import com.adfonic.adserver.offence.OffenceHttpServletRequest;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.offence.OffenceRegistry.BidExceptionStats;
import com.adfonic.adserver.offence.OffenceSection;
import com.adfonic.adserver.offence.TroubledBidRequest;
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.nativ.ByydBase;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.protobuf.MessageLite;
import com.google.protobuf.UnknownFieldSet;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping("/adserver")
public class OffenceController {

    private static final ObjectMapper debugJsonMapper = DebugBidController.debugJsonMapper;
    static {

        debugJsonMapper.addMixIn(ByydBase.class, JacksonMixin.class);

        debugJsonMapper.addMixIn(MessageLite.class, IgnoreTypeMixin.class);
        debugJsonMapper.addMixIn(UnknownFieldSet.class, IgnoreTypeMixin.class);

        debugJsonMapper.addMixIn(com.google.protobuf.Descriptors.Descriptor.class, IgnoreTypeMixin.class);
        debugJsonMapper.addMixIn(com.google.protobuf.Descriptors.FileDescriptor.class, IgnoreTypeMixin.class);
        debugJsonMapper.addMixIn(com.google.protobuf.Descriptors.EnumDescriptor.class, IgnoreTypeMixin.class);
        debugJsonMapper.addMixIn(com.google.protobuf.Descriptors.EnumValueDescriptor.class, IgnoreTypeMixin.class);

        debugJsonMapper.addMixIn(TargetingContext.class, IgnoreTypeMixin.class);

        //Protobuf beans cannot be Jsonized easily - TODO https://code.google.com/p/protostuff/wiki/JsonSerialization
        debugJsonMapper.addMixIn(AdX.BidRequest.class, IgnoreTypeMixin.class);
        debugJsonMapper.addMixIn(OpenX.BidRequest.class, IgnoreTypeMixin.class);

        debugJsonMapper.addMixIn(HttpServletRequest.class, IgnoreTypeMixin.class);
        debugJsonMapper.addMixIn(HttpServletResponse.class, IgnoreTypeMixin.class);

        //RtbHttpExchange httpRequest, httpResponse
        debugJsonMapper.addMixIn(RtbHttpContext.class, JacksonMixin.class);
    }

    public static abstract class JacksonMixin {

        //BidBase.getRtbBidRequest()
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
        public abstract Object getRtbBidRequest();

    }

    @JsonIgnoreType
    public abstract class IgnoreTypeMixin {
        //ignore class completely
    }

    @Autowired
    private OffenceRegistry offenceRegistry;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @ResponseBody
    @RequestMapping(value = "/offences", method = RequestMethod.GET, produces = "application/json")
    public String offences() throws IOException {
        Map<String, List<DbgOffenceSummary>> retval = new HashMap<String, List<DbgOffenceSummary>>();
        Collection<OffenceSection> sections = offenceRegistry.sections();
        for (OffenceSection section : sections) {
            List<DbgOffenceSummary> sumlist = summarize(section);
            retval.put(section.getSectionId(), sumlist);
        }
        StringWriter sw = new StringWriter();
        debugJsonMapper.writeValue(sw, retval);
        return sw.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/offences", method = RequestMethod.DELETE, produces = "application/json")
    public String clearOffences() throws IOException {
        offenceRegistry.clear();
        return offences();
    }

    @ResponseBody
    @RequestMapping(value = "/offences/reset", method = RequestMethod.GET, produces = "application/json")
    public String resetOffences() throws IOException {
        return clearOffences();
    }

    @ResponseBody
    @RequestMapping(value = "/offences/{publisherSelector}", method = RequestMethod.GET, produces = "application/json")
    public String offences(@PathVariable("publisherSelector") String publisherSelector, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

        OffenceSection section = findOffenceSection(publisherSelector);
        if (section != null) {
            StringWriter sw = new StringWriter();
            debugJsonMapper.writeValue(sw, summarize(section));
            return sw.toString();
        } else {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Offence not found: " + publisherSelector);
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/offences/{publisherSelector}", method = RequestMethod.DELETE, produces = "application/json")
    public String clearPublisher(@PathVariable("publisherSelector") String publisherSelector, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        OffenceSection section = findOffenceSection(publisherSelector);
        if (section != null) {
            section.clear();
        }
        return offences(publisherSelector, httpRequest, httpResponse);
    }

    @ResponseBody
    @RequestMapping(value = "/offences/{publisherSelector}/{offenceType}/", method = RequestMethod.GET, produces = "application/json")
    public String offences(@PathVariable("publisherSelector") String publisherSelector, @PathVariable("offenceType") String offenceType, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {

        BidExceptionStats[] offenceStatsArr = findOffenceStats(publisherSelector, offenceType);
        if (offenceStatsArr != null) {
            List<DbgOffenceStats> retval = new ArrayList<DbgOffenceStats>(offenceStatsArr.length);
            for (BidExceptionStats exceptionStats : offenceStatsArr) {
                DbgOffenceStats dbgOffenceStats = new DbgOffenceStats(exceptionStats);
                retval.add(dbgOffenceStats);
            }
            StringWriter sw = new StringWriter();
            debugJsonMapper.writeValue(sw, retval);
            return sw.toString();
        } else {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Offence not found: " + publisherSelector + "/" + offenceType);
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/offences/{publisherSelector}/{offenceType}/{offenceIndex}", method = RequestMethod.GET, produces = "application/json")
    public String offence(@PathVariable("publisherSelector") String publisherSelector, @PathVariable("offenceType") String offenceType,
            @PathVariable("offenceIndex") Integer offenceIndex, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

        Object[] offenceStats = findOffenceStats(publisherSelector, offenceType);
        if (offenceStats != null) {
            Object exceptionStats = offenceStats[offenceIndex];
            StringWriter sw = new StringWriter();
            debugJsonMapper.writeValue(sw, exceptionStats);

            return sw.toString();
        } else {
            httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Offence not found: " + publisherSelector + "/" + offenceType);
            return null;
        }
    }

    private List<DbgOffenceSummary> summarize(OffenceSection section) {
        //String publisherExternalId = section.getSectionId();

        List<BidExceptionStats[]> offences = section.values();
        List<DbgOffenceSummary> retlist = new ArrayList<DbgOffenceSummary>(offences.size());
        for (BidExceptionStats[] statsArray : offences) {
            for (BidExceptionStats stats : statsArray) {
                DbgOffenceSummary summary = new DbgOffenceSummary(stats);
                retlist.add(summary);
            }
        }
        return retlist;

    }

    private OffenceSection findOffenceSection(String publisherSelector) {
        OffenceSection section = null;
        Long id = DbgUiUtil.tryToLong(publisherSelector);
        if (id != null) {
            AdserverDomainCache adCache = adserverCacheManager.getCache();
            for (PublisherDto publisher : DbgBuilder.getAllPublishers(adCache)) {
                if (publisher.getId().longValue() == id.longValue()) {
                    section = offenceRegistry.getSection(publisher.getExternalId());
                    break;
                }
            }

        } else {
            section = offenceRegistry.getSection(publisherSelector);
        }
        return section;
    }

    private BidExceptionStats[] findOffenceStats(String publisherSelector, String offenceType) {
        OffenceSection section = findOffenceSection(publisherSelector);
        if (section != null) {
            try {
                return section.getStats((Class<? extends Exception>) Class.forName(offenceType));
            } catch (Exception x) {
                x.printStackTrace();
                return null;
                // TODO return section.getMessageStats(offenceType);
            }

        } else {
            return null;
        }
    }

    public static class DbgOffenceStats extends DbgOffenceSummary {

        private final List<String> stackTrace;
        private final List<String> snapshots;

        public DbgOffenceStats(BidExceptionStats stats) {
            super(stats);
            StackTraceElement[] stArray = stats.getOffence().getStackTrace();
            if (stArray != null) {
                this.stackTrace = new ArrayList<String>(stArray.length);
                for (StackTraceElement stElement : stArray) {
                    StringBuilder lineBuilder = new StringBuilder();
                    lineBuilder.append(stElement.getClassName());
                    lineBuilder.append('.');
                    lineBuilder.append(stElement.getMethodName());
                    lineBuilder.append('(');
                    lineBuilder.append(stElement.getFileName());
                    lineBuilder.append(':');
                    lineBuilder.append(stElement.getLineNumber());
                    lineBuilder.append(')');
                    this.stackTrace.add(lineBuilder.toString());
                }
            } else {
                this.stackTrace = Collections.emptyList();
            }

            TroubledBidRequest[] troubles = stats.getSnapshot();
            this.snapshots = new ArrayList<String>(troubles.length);
            for (TroubledBidRequest trouble : troubles) {
                RtbExecutionContext<?, ?> executionContext = trouble.getExecutionContext();
                OffenceHttpServletRequest httpRequest = trouble.getHttpRequest();

                StringBuilder snapshotLine = new StringBuilder();
                snapshotLine.append(ISO8601Utils.format(new Date(executionContext.getExecutionStartedAt())));
                snapshotLine.append(' ').append(httpRequest.getRemoteAddr());
                snapshotLine.append(' ').append(httpRequest.getMethod()).append(' ').append(httpRequest.getRequestURI());

                snapshotLine.append(' ').append('[');
                long startedMs = executionContext.getExecutionStartedAt();
                Long rtbRequestAt = executionContext.getRtbRequestParsedAt();
                if (rtbRequestAt != null) {
                    snapshotLine.append((rtbRequestAt - startedMs)).append("ms");
                } else {
                    snapshotLine.append('-');
                }

                Long byydRequestAt = executionContext.getByydRequestMappedAt();
                snapshotLine.append(',');
                if (byydRequestAt != null) {
                    snapshotLine.append((byydRequestAt - startedMs)).append("ms");
                } else {
                    snapshotLine.append('-');
                }

                Long byydResponseAt = executionContext.getByydResponseCreatedAt();
                snapshotLine.append(',');
                if (byydResponseAt != null) {
                    snapshotLine.append((byydResponseAt - startedMs)).append("ms");
                } else {
                    snapshotLine.append('-');
                }

                Long rtbResponseAt = executionContext.getRtbResponseMappedAt();
                snapshotLine.append(',');
                if (rtbResponseAt != null) {
                    snapshotLine.append((rtbResponseAt - startedMs)).append("ms");
                } else {
                    snapshotLine.append('-');
                }
                snapshotLine.append(']');
                this.snapshots.add(snapshotLine.toString());
            }
        }

        public List<String> getStackTrace() {
            return stackTrace;
        }

        public List<String> getSnapshots() {
            return snapshots;
        }

    }

    public static class DbgOffenceSummary {
        private final String type;
        private final String message;
        private final int occurences;
        private final Date firstOccuredAt;
        private final Date lastOccuredAt;

        public DbgOffenceSummary(BidExceptionStats stats) {
            Exception exception = stats.getOffence();
            this.type = exception.getClass().getName();
            this.message = exception.getMessage();
            this.occurences = stats.getCount();
            this.firstOccuredAt = stats.getFirstOccuredAt();
            this.lastOccuredAt = stats.getLastOccuredAt();
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public int getOccurences() {
            return occurences;
        }

        public Date getFirstOccuredAt() {
            return firstOccuredAt;
        }

        public Date getLastOccuredAt() {
            return lastOccuredAt;
        }

    }
}
