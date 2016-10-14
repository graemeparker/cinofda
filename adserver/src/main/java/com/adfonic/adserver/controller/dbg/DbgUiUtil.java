package com.adfonic.adserver.controller.dbg;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.fasterxml.jackson.databind.JsonNode;

public class DbgUiUtil {

    public static final String HTML_OPEN = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head><body>";
    public static final String HTML_CLOSE = "</body></html>";

    private static final FastDateFormat FDF = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static String span(String text, int maxLength) {
        if (text != null && text.length() > maxLength) {
            return "<span title='" + text + "'>" + text.substring(0, maxLength - 3) + "..." + "</span>";
        } else {
            return text;
        }
    }

    public static String publisherLink(Long publisherId, String name) {
        return "<a target='_blank' href='/adcache/publisher/" + publisherId + "'>" + name + "</a>";
    }

    public static String advertiserLink(Long advertiserId) {
        return "<a target='_blank' href='/adcache/advertiser/" + advertiserId + "'>" + advertiserId + "</a>";
    }

    public static String creativeLink(Long creativeId) {
        return creativeLink(creativeId, String.valueOf(creativeId));
    }

    public static String creativeLink(Long creativeId, String label) {
        return "<a target='_blank' href='/adcache/creative/" + creativeId + "'>" + label + "</a>";
    }

    public static String campaignLink(Long campaignId) {
        return campaignLink(campaignId, String.valueOf(campaignId));
    }

    public static String campaignLink(Long campaignId, String label) {
        return "<a target='_blank' href='/adcache/campaign/" + campaignId + "'>" + label + "</a>";
    }

    public static String adspaceLink(Long adSpaceId) {
        return "<a target='_blank' href='/adcache/adspace/" + adSpaceId + "'>" + adSpaceId + "</a>";
    }

    public static String publicationLink(Long publicationId) {
        return "<a target='_blank' href='/internal/publication_digger.jsp?publicationId=" + publicationId + "'>" + publicationId + "</a>";
    }

    public static String impressionLink(String impressionExternalId) {
        return "<a target='_blank' href='/adserver/aspike/" + impressionExternalId + "'>" + impressionExternalId + "</a>";
    }

    public static void printExchangesRadios(PrintWriter writer, AdserverDomainCache adCache, boolean allRtbExchanges, RtbExchange selectedRtbExchange) {
        int cnt = 0;
        for (RtbExchange exchange : RtbExchange.values()) {
            //print only if it is in cache and has some adspaces...
            Long adCachePublisherId = adCache.getPublisherIdByExternalID(exchange.getPublisherExternalId());
            if (allRtbExchanges || (adCachePublisherId != null && adCache.getPublisherRtbAdSpacesMap(adCachePublisherId) != null)) {
                writer.print(publisherLink(exchange.getPublisherId(), exchange.name()) + " <input type='radio' name='exchange' value='" + exchange.getPublisherExternalId() + "'");
                if (selectedRtbExchange != null && adCachePublisherId != null && selectedRtbExchange.getPublisherId() == adCachePublisherId.longValue()) {
                    writer.print(" checked='checked'");
                }
                writer.println(" />");
                ++cnt;
                if (cnt % 6 == 0) {
                    writer.println("<br/>");
                }
            }
        }
        //OK, probably QA that have its's own publisher ids, just print what is in cache
        if (cnt == 0) {
            //It would be nice to show only those present in local adserver cache
            Set<PublisherDto> publishers = DbgBuilder.getAllPublishers(adCache);
            for (PublisherDto publisher : publishers) {
                String label;
                RtbExchange exchange = RtbExchange.getByPublisherId(publisher.getId());
                if (exchange != null) {
                    label = exchange.name();
                } else {
                    label = publisher.getId() + " / " + publisher.getExternalId();
                    writer.println("<br/>");
                    cnt = 0;
                }

                ++cnt;
                if (cnt % 6 == 0) {
                    writer.println("<br/>");
                }
                String publisherLink = publisherLink(publisher.getId(), label);
                writer.println(publisherLink + " <input type='radio' name='exchange' value='" + publisher.getExternalId() + "'");
                if (selectedRtbExchange != null && selectedRtbExchange.getPublisherId() == publisher.getId().longValue()) {
                    writer.print(" checked='checked'");
                }
                writer.println(" />");
            }
        }
    }

    public static AdSpaceDto findAdSpace(String identifier, AdserverDomainCache adCache) {
        AdSpaceDto adSpace;
        Long id = tryToLong(identifier);
        if (id != null) {
            adSpace = adCache.getAdSpaceById(id);
        } else {
            adSpace = adCache.getAdSpaceByExternalID(identifier);
        }
        return adSpace;
    }

    public static PublicationDto findPublication(String identifier, AdserverDomainCache adCache) {
        Long id = tryToLong(identifier);
        if (id != null) {
            for (AdSpaceDto adspace : adCache.getAllAdSpaces()) {
                if (adspace.getPublication().getId().equals(id)) {
                    return adspace.getPublication();
                }
            }
        } else {
            for (AdSpaceDto adspace : adCache.getAllAdSpaces()) {
                if (adspace.getPublication().getExternalID().equals(identifier)) {
                    return adspace.getPublication();
                }
            }
        }
        return null;
    }

    public static PublisherDto findPublisher(String identifier, AdserverDomainCache adCache) {
        Long id = tryToLong(identifier);
        if (id == null) {
            id = adCache.getPublisherIdByExternalID(identifier);
        }
        Map<String, AdSpaceDto> adSpacesMap = adCache.getPublisherRtbAdSpacesMap(id);
        if (adSpacesMap != null && !adSpacesMap.isEmpty()) {
            AdSpaceDto adSpace = adSpacesMap.values().iterator().next();
            return adSpace.getPublication().getPublisher();
        } else {
            return null;
        }
    }

    public static PublisherDto getPublisher(String identifier, AdserverDomainCache adCache) throws IllegalArgumentException {
        if (StringUtils.isBlank(identifier)) {
            throw new IllegalArgumentException("Publisher identifier not submitted");
        }
        PublisherDto publisher = findPublisher(identifier, adCache);
        if (publisher != null) {
            return publisher;
        } else {
            throw new IllegalArgumentException("Publisher not found in cache " + identifier);
        }
    }

    public static CreativeDto getCreative(String identifier, AdserverDomainCache adCache) throws IllegalArgumentException {
        if (StringUtils.isBlank(identifier)) {
            throw new IllegalArgumentException("Creative identifier not submitted");
        }
        CreativeDto creative = findCreative(identifier, adCache);
        if (creative != null) {
            return creative;
        } else {
            throw new IllegalArgumentException("Creative not found in cache " + identifier);
        }
    }

    public static AdSpaceDto getAdSpace(String identifier, AdserverDomainCache adCache) throws IllegalArgumentException {
        if (StringUtils.isBlank(identifier)) {
            throw new IllegalArgumentException("AdSpace identifier not submitted");
        }
        AdSpaceDto adSpace = findAdSpace(identifier, adCache);
        if (adSpace != null) {
            return adSpace;
        } else {
            throw new IllegalArgumentException("AdSpace not found in cache " + identifier);
        }
    }

    public static CreativeDto findCreative(String identifier, AdserverDomainCache adCache) {
        CreativeDto creative;
        Long id = tryToLong(identifier);
        if (id != null) {
            creative = adCache.getCreativeById(id);
        } else {
            creative = adCache.getCreativeByExternalID(identifier);
        }
        return creative;
    }

    public static CampaignDto findCampaign(String identifier, AdserverDomainCache adCache) {
        Long id = tryToLong(identifier);
        if (id != null) {
            for (CreativeDto creative : adCache.getAllCreatives()) {
                if (creative.getCampaign().getId().equals(id)) {
                    return creative.getCampaign();
                }
            }
        } else {
            for (CreativeDto creative : adCache.getAllCreatives()) {
                if (creative.getCampaign().getExternalID().equals(identifier)) {
                    return creative.getCampaign();
                }
            }
        }
        return null;
    }

    public static Long tryToLong(String string) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException nfx) {
            return null;
        }
    }

    public static String indentJson(String json) {
        try {
            JsonNode jsonNode = DebugBidController.debugJsonMapper.readTree(json);
            return DebugBidController.debugJsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception x) {
            //ignore...
            return json;
        }
    }

    public static void writeRtbTimestamps(PrintWriter writer, RtbExecutionContext<?, ?> exctx) {
        writer.println("Started: " + DbgUiUtil.format(exctx.getExecutionStartedAt()));
        writer.println(", Completed: " + DbgUiUtil.millis(exctx.getExecutionStartedAt(), exctx.getExecutionCompletedAt()));
        writer.print("(Parsed: " + DbgUiUtil.millis(exctx.getExecutionStartedAt(), exctx.getRtbRequestParsedAt()));
        writer.print(", MapIn: " + DbgUiUtil.millis(exctx.getRtbRequestParsedAt(), exctx.getByydRequestMappedAt()));
        writer.print(", Pretarget: " + DbgUiUtil.millis(exctx.getByydRequestMappedAt(), exctx.getTargetingStartedAt()));
        writer.print(", Targeting: " + DbgUiUtil.millis(exctx.getTargetingStartedAt(), exctx.getTargetingCompletedAt()));
        writer.print(", Response: " + DbgUiUtil.millis(exctx.getTargetingCompletedAt(), exctx.getByydResponseCreatedAt()));
        writer.print(", MapOut: " + DbgUiUtil.millis(exctx.getByydResponseCreatedAt(), exctx.getRtbResponseMappedAt()));
        writer.print(", Written: " + DbgUiUtil.millis(exctx.getRtbResponseMappedAt(), exctx.getRtbResponseWrittenAt()));
        writer.print(")");
    }

    public static String format(Date date) {
        if (date != null) {
            return FDF.format(date);
        } else {
            return "-";
        }
    }

    public static String format(Long timestamp) {
        if (timestamp != null) {
            return FDF.format(timestamp);
        } else {
            return "-";
        }
    }

    public static String millis(Long from, Long until) {
        if (from == null || until == null) {
            return "-";
        } else {
            return (until - from) + " ms";
        }
    }
}
