package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.adfonic.adserver.Constant;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.controller.dbg.DebugBidContext.CreativePurpose;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgCreativeEliminationDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgCreativePickedDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgImpressionBidDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgNoBidDto;
import com.adfonic.adserver.controller.dbg.dto.DbgBidDto.DbgTargetingDto;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.controller.rtb.RtbEndpoint.RtbProtocol;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.impl.FrequencyCapper;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.dec.SecurityAlias;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.domain.RtbConfig.DecryptionScheme;
import com.adfonic.domain.RtbConfig.RtbWinNoticeMode;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.HttpUtils;

@Controller
public class DebugBidUiController {

    @Autowired
    private RtbFisherman fisher;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Autowired
    private FrequencyCapper frequencyCapper;

    @RequestMapping(value = { "/adserver/bidebug", "/rtb/debug/bid" }, method = RequestMethod.GET)
    public void debugUiGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setCharacterEncoding("UTF-8");
        PrintWriter writer = httpResponse.getWriter();
        writer.println("<html><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/><body>");
        writer.println("<form method='POST' action='/rtb/debug/bidpost' accept-charset='UTF-8'>");

        String rtbRequestString = null;
        RtbExchange rtbExchange = null;
        String fisherIndex = httpRequest.getParameter("fisherIndex");
        if (StringUtils.isNotBlank(fisherIndex)) {
            List<RtbExecutionContext<?, ?>> catches = fisher.getSession().getCatches();
            RtbExecutionContext<?, ?> context = catches.get(Integer.parseInt(fisherIndex));
            rtbRequestString = context.getRtbRequestString();
            rtbExchange = RtbExchange.lookup(context.getPublisherExternalId());
        }

        boolean allExchanges = httpRequest.getParameter("allExchanges") != null;

        DbgUiUtil.printExchangesRadios(writer, adserverCacheManager.getCache(), allExchanges, rtbExchange);

        writer.println("<br/>");
        writer.println("<textarea name='bidbody' cols='80' rows='40'>");
        if (rtbRequestString != null) {
            writer.println(rtbRequestString);
        }
        writer.println("</textarea>");
        writer.println("<br/>");
        writer.println("Creative: <input name='creativeSpec'/>");
        writer.println("<select name='creativePurpose'>");
        CreativePurpose[] values = CreativePurpose.values();
        for (CreativePurpose item : values) {
            writer.println("<option value='" + item.name() + "'>" + item.name() + "</option>");
        }
        writer.println("</select>");
        writer.println("<input type='checkbox' name='debug' value='true' checked>Debug");
        writer.println("<input type='submit'/>");
        writer.println("</form>");
        writer.println("</body></html>");
    }

    protected void printBidOutput(PrintWriter writer, DbgBidDto dbgBid, RtbExecutionContext<?, ?> exctx, AdserverDomainCache adCache, DomainCache doCache,
            DynamicProperties dynaProps) throws IOException {

        RtbExchange exchange = dbgBid.getExchange();

        AdSpaceDto adSpace = null;
        ByydRequest byydRequest = dbgBid.getByydRequest();
        if (byydRequest != null) {
            adSpace = byydRequest.getAdSpace();
        }

        ByydResponse byydResponse = dbgBid.getByydResponse();
        CreativeDto creative = null;
        CampaignDto campaign = null;
        if (byydResponse != null) {
            creative = byydResponse.getBid().getCreative();
            campaign = creative.getCampaign();
        }

        writer.println("<p><strong>RTB Bid Debug");
        DbgUiUtil.writeRtbTimestamps(writer, exctx);
        writer.println("</strong></p>");
        writer.println("<br/>");
        writer.print("AdSpace: " + (adSpace != null ? DbgUiUtil.adspaceLink(adSpace.getId()) : "-"));
        writer.print(", Publication: " + (adSpace != null ? DbgUiUtil.publicationLink(adSpace.getPublication().getId()) + " - '" + adSpace.getPublication().getName() + "'" : "-"));
        writer.println("<br/>");

        DebugBidContext debugContext = dbgBid.getDebugContext();
        if (debugContext != null && creative != null) {
            Long debugCreativeId = debugContext.getCreativeId();
            // Returned creative is not the one we preferred 
            if (debugCreativeId != null && debugContext.getCreativePurpose() == CreativePurpose.Prefer && !creative.getId().equals(debugCreativeId)) {
                DbgCreativeEliminationDto elimination = findElimination(debugCreativeId, dbgBid.getTargetingInfo().getEliminated());
                String eliminationMessage;
                if (elimination != null) {
                    eliminationMessage = elimination.getReason() + " - " + elimination.getMessage();
                } else {
                    eliminationMessage = "Investigate <a href='#creative-" + debugCreativeId + "'>targeting messages</a>";
                }
                writer.print("<p>Preferred creative " + debugCreativeId + " <b>is NOT bidded</b>: " + eliminationMessage + "</p>");
            }
        }

        writer.print("Creative: " + (creative != null ? DbgUiUtil.creativeLink(creative.getId()) + " '" + creative.getName() + "'" : "-"));
        writer.print(", Campaign: " + (creative != null ? DbgUiUtil.campaignLink(campaign.getId()) + " '" + campaign.getName() + "'" : "-"));
        writer.print(", RtbCache: " + (dbgBid.getBidImpression() != null ? DbgUiUtil.impressionLink(dbgBid.getBidImpression().getImpression().getExternalID()) : "-"));
        writer.println("<br/>");

        if (byydResponse != null) {
            // Print links for Byyd notification/trackers endpoints
            DbgImpressionBidDto impressionDto = dbgBid.getBidImpression();
            String impressionExternalID = impressionDto.getImpression().getExternalID();
            // Override IP and User-Agent. On AdServer they are compared with values from bid request to prevent frauds  
            String userAgentParam = "h.user-agent=" + byydRequest.getDevice().getUserAgent();
            String ipAddressParam = "r.ip=" + byydRequest.getDevice().getIp();
            RtbConfigDto rtbConfig = adSpace.getPublication().getPublisher().getRtbConfig();

            BigDecimal rtbBidPrice = impressionDto.getImpression().getRtbBidPrice();
            String finalRtbBidPrice;
            DecryptionScheme decryptionScheme = rtbConfig.getDecryptionScheme();
            if (decryptionScheme != null) {
                SecurityAlias alias = SecurityAlias.valueOfCached(rtbConfig.getSecurityAlias());
                finalRtbBidPrice = alias.getPriceCrypter(rtbConfig.getDecryptionScheme()).encodePrice(rtbBidPrice);
            } else {
                finalRtbBidPrice = rtbBidPrice.toPlainString();
            }

            String settlePriceParam = Constant.SP_URL_PARAM + "=" + finalRtbBidPrice;
            boolean clickRedirect = false;
            Long extTypeId = creative.getExtendedCreativeTypeId();
            if (extTypeId != null) {
                ExtendedCreativeTypeDto extType = doCache.getExtendedCreativeTypeById(extTypeId);
                clickRedirect = extType.isClickRedirectRequired();
            }
            String clickTrackUrl;
            if (clickRedirect) {
                clickTrackUrl = DebugBidController.DBG_CLICK_REDIRECT_URL + "/" + adSpace.getExternalID() + "/" + impressionExternalID + "?debug=true&" + userAgentParam + "&"
                        + ipAddressParam + "&redir=" + HttpUtils.urlEncode(creative.getDestination().getRealDestination());

            } else {
                // click-through otherwise
                clickTrackUrl = DebugBidController.DBG_CLICK_THROUGH_URL + "/" + adSpace.getExternalID() + "/" + impressionExternalID + "?debug=true&" + userAgentParam + "&"
                        + ipAddressParam;
            }

            String impressionTrackUrl = DebugBidController.DBG_IMPRESSION_URL + "/" + adSpace.getExternalID() + "/" + impressionExternalID + ".gif" + "?debug=true&"
                    + userAgentParam + "&" + ipAddressParam;

            if (rtbConfig.getWinNoticeMode() == RtbWinNoticeMode.OPEN_RTB) {
                // Win notification is a server to server call - no need to send fake user agent or ip address 
                String winNotificationUrl = DebugBidController.DBG_WIN_URL + "/" + impressionExternalID + "?" + settlePriceParam;
                writer.println("Track: <a href='" + winNotificationUrl + "' target='_blank'>RTB Win</a>");
                writer.println(", ");
            } else if (rtbConfig.getWinNoticeMode() == RtbWinNoticeMode.BEACON) {
                impressionTrackUrl += "&" + settlePriceParam;
            }

            // Support for Rubicon video on win notification hack
            if (exchange == RtbExchange.Rubicon) {
                if (byydRequest.getImp().getAdObject() == AdObject.VIDEO) {
                    String winUrl = byydResponse.getBid().getNurl();
                    winUrl = winUrl.replace("${AUCTION_PRICE:BF}", finalRtbBidPrice);
                    writer.print("Track: <a href='" + winUrl + "' target='_blank'>Rubicon VAST win</a>, ");
                }
            }

            if (exchange.getEndpoint().getLossUrlContext() != null) {
                writer.println("Track: <a href='" + DebugBidController.DBG_LOSS_URL + "/" + impressionExternalID + "' target='_blank'>RTB Loss</a>");
                writer.println(", ");
            }

            writer.print("Track: <a href='" + impressionTrackUrl + "' target='_blank'>Impression</a>");
            writer.print(", Track: <a href='" + clickTrackUrl + "' target='_blank'>Click</a>");

            if (campaign.isConversionTrackingEnabled()) {
                String trackerBaseUrl = dynaProps.getProperty(DcProperty.TrackerBaseUrl);
                // Looking into tracker access log, /cb/ is more common than /scb/ 
                String conversionTrackUrl = trackerBaseUrl + "/cb/" + campaign.getAdvertiser().getExternalID() + "/conversion.gif?debug=true";
                writer.println(", Track: <a href='" + conversionTrackUrl + "' target='_blank'>Conversion</a>");
            }

            if (campaign.isInstallTrackingEnabled()) {
                String trackerBaseUrl = dynaProps.getProperty(DcProperty.TrackerBaseUrl);
                String appBundle = campaign.getApplicationID(); // Id from Google play market or Apple iTunes
                String installTrackUrl = trackerBaseUrl + "/is/" + appBundle + "?debug=true";
                Map<String, String> dids = byydRequest.getDevice().getDeviceIdentifiers();
                // d.hifa and d.dpid are most common in tracker access log
                for (Entry<String, String> entry : dids.entrySet()) {
                    installTrackUrl += "&d." + entry.getKey() + "=" + entry.getValue();
                }
                writer.println(", Track: <a href='" + installTrackUrl + "' target='_blank'>Installation</a>");
            }

            if (campaign.getCapImpressions() != null) {
                writer.print(", FreqCap: ");
                Map<Long, String> deviceIdentifiers = impressionDto.getImpression().getDeviceIdentifiers();
                if (deviceIdentifiers != null && !deviceIdentifiers.isEmpty()) {
                    Map.Entry<Long, String> firstEntry = deviceIdentifiers.entrySet().iterator().next();
                    String freqCapKey = firstEntry.getKey() + "." + firstEntry.getValue();

                    int impressionCount = frequencyCapper.getImpressionCount(freqCapKey, creative);
                    writer.println(impressionCount + " of " + campaign.getCapImpressions());
                } else {
                    writer.println("No device id");
                }
            }

            //XXX what is creative.getCampaign().isInstallTrackingAdXEnabled() ??? 
        }

        writer.println("<hr/>");

        String rtbResponse = dbgBid.getRtbResponse();
        if (rtbResponse != null) {
            writer.println("<p><strong>Response to " + exchange + "</strong></p>");
            writer.println("<pre>");
            RtbProtocol protocol = exchange.getEndpoint().getProtocol();
            if (protocol.getResponseMediaType() == MediaType.APPLICATION_JSON) {
                rtbResponse = DbgUiUtil.indentJson(rtbResponse);
            }
            rtbResponse = StringEscapeUtils.escapeHtml(rtbResponse);
            writer.println(rtbResponse);
            writer.println("</pre>");

            writer.println("<hr/>");
        }

        DbgNoBidDto nobidReason = dbgBid.getNobidReason();
        if (nobidReason != null) {
            writer.println("<p><strong>Nobid Reason</strong></p>");
            writer.println("Reason: " + nobidReason.getReason() + ", Offence: " + nobidReason.getOffenceName() + ", Value: " + nobidReason.getOffensiveValue());
            writer.println("<hr/>");
        }

        Exception exception = dbgBid.getException();
        if (exception != null) {
            if (exception instanceof NoBidException) {
                NoBidException nbx = (NoBidException) dbgBid.getException();
                writer.println(nbx.getMessage());
            } else {
                writer.println("<p><strong>" + exception + "</strong></p>");
                StackTraceElement[] stackTrace = exception.getStackTrace();
                writer.println("<pre>");
                for (int i = 0; i < stackTrace.length; i++) {
                    StackTraceElement element = stackTrace[i];
                    writer.println(element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
                }
                writer.println("</pre>");
            }
            writer.println("<hr/>");
        }

        List<String> messages = dbgBid.getBiddingEvents();
        if (messages != null) {
            writer.println("<p><strong>Bid Events</strong></p>");
            writer.println("<pre>");
            for (int i = 0; i < messages.size(); i++) {
                writer.println(messages.get(i));
            }
            writer.println("</pre>");
            writer.println("<hr/>");
        }

        DbgTargetingDto targetingInfo = dbgBid.getTargetingInfo();
        if (targetingInfo != null) {
            List<DbgCreativePickedDto> targetedList = targetingInfo.getTargeted();
            if (!targetedList.isEmpty()) {
                writer.println("<p><strong>Targeted Creatives: " + targetedList.size() + " </strong></p>");
                writer.println("<table border='0'>");
                writer.println("<tr><th>Campaign</th><th>Creative</th><th>Priority</th><th>EcpmWeight</th><th>Selected / Eliminated</th></tr>");
                for (DbgCreativePickedDto item : targetedList) {
                    long creativeId = item.getCreativeId();
                    CreativeDto creativeDto = adCache.getCreativeById(creativeId);
                    CampaignDto campaignDto = creativeDto.getCampaign();

                    String eliminationMessage = null;
                    DbgCreativeEliminationDto elimination = findElimination(creativeId, targetingInfo.getEliminated());
                    if (elimination != null) {
                        eliminationMessage = elimination.getReason() + " - " + elimination.getMessage();
                    }
                    String selemination;
                    if (debugContext.getCreativeId() != null && creativeId == debugContext.getCreativeId().longValue()) {
                        selemination = "<b>Bidded as debug preferred</b>";
                    } else if (creative != null && creativeId == creative.getId()) {
                        selemination = "<b>Bidded as random selected</b>";
                    } else {
                        selemination = "Not random selected";
                    }

                    if (eliminationMessage != null) {
                        selemination += " - " + eliminationMessage;
                    }

                    writer.println("<tr>");
                    writer.println("<a name='creative-" + item.getCreativeId() + "'></a>");
                    writer.println("<td>" + DbgUiUtil.campaignLink(campaignDto.getId()) + " " + DbgUiUtil.span(campaignDto.getName(), 50) + "</td>");
                    writer.println("<td>" + DbgUiUtil.creativeLink(creativeDto.getId()) + " " + DbgUiUtil.span(creativeDto.getName(), 50) + "</td>");
                    writer.println("<td>" + item.getPriority() + "</td>");
                    writer.println("<td>" + item.getEcpmWeight() + "</td>");
                    writer.println("<td>" + selemination + "</td>");

                    writer.println("</tr>");
                }
                writer.println("</table>");
                writer.println("<hr/>");
            }

            List<Long> selectedList = targetingInfo.getSelected();
            if (!selectedList.isEmpty()) {
                writer.println("<p><strong>Selected Creatives</strong></p>");
                for (Long creativeId : selectedList) {
                    writer.println(DbgUiUtil.creativeLink(creativeId));
                }
                writer.println("<hr/>");
            }

            List<DbgCreativeEliminationDto> eliminatedList = targetingInfo.getEliminated();
            if (!eliminatedList.isEmpty()) {
                writer.println("<p><strong>Eliminated Creatives: " + eliminatedList.size() + " </strong></p>");
                writer.println("<table border='0'>");
                writer.println("<tr><th>Campaign</th><th>Creative</th><th>Reason</th><th>Message</th></tr>");
                for (DbgCreativeEliminationDto item : eliminatedList) {
                    CreativeDto creativeDto = adCache.getCreativeById(item.getCreativeId());
                    CampaignDto campaignDto = creativeDto.getCampaign();
                    writer.println("<tr>");
                    writer.println("<a name='creative-" + item.getCreativeId() + "'></a>");
                    writer.println("<td>" + DbgUiUtil.campaignLink(campaignDto.getId()) + " " + DbgUiUtil.span(campaignDto.getName(), 50) + "</td>");
                    writer.println("<td>" + DbgUiUtil.creativeLink(creativeDto.getId()) + " " + DbgUiUtil.span(creativeDto.getName(), 50) + "</td>");
                    writer.println("<td>" + item.getReason() + "</td>");
                    writer.println("<td>" + item.getMessage() + "</td>");
                    writer.println("</tr>");
                }
                writer.println("</table>");
                writer.println("<hr/>");
            }

            AdspaceWeightedCreative[] eligibleList = targetingInfo.getEligible();
            if (eligibleList != null) {
                writer.println("<p><strong>Eligible Creatives </strong></p>");
                for (AdspaceWeightedCreative item : eligibleList) {
                    writer.print("Priority: " + item.getPriority() + ", " + item.getCreativeIds().length + "  Creatives: ");
                    Long[] creativeIds = item.getCreativeIds();
                    for (int i = 0; i < creativeIds.length; ++i) {
                        Long creativeId = creativeIds[i];
                        writer.print(DbgUiUtil.creativeLink(creativeId));
                        if (i != creativeIds.length - 1) {
                            writer.print(", ");
                        }
                    }
                }
                writer.println("<hr/>");
            }
        }

        Map<String, String> targetingMap = dbgBid.getTargetingContext();
        if (targetingMap != null && targetingMap.isEmpty() == false) {
            writer.println("<p><strong>Targeting Context</strong></p>");
            writer.println("<pre>");
            for (Entry<String, String> entry : targetingMap.entrySet()) {
                writer.println(entry.getKey() + ": '" + entry.getValue() + "'");
            }
            writer.println("</pre>");
            writer.println("<hr/>");
        }

        if (byydRequest != null) {
            writer.println("<p><strong>Byyd Request</strong></p>");
            writer.println("<pre>");
            try {
                writer.println(DebugBidController.debugJsonMapper.writeValueAsString(byydRequest));
            } catch (Exception x) {
                x.printStackTrace(writer);
            }
            writer.println("</pre>");
            writer.println("<hr/>");
        }

        if (byydResponse != null) {
            writer.println("<p><strong>Byyd Response</strong></p>");
            writer.println("<pre>");
            try {
                writer.println(StringEscapeUtils.escapeHtml(DebugBidController.debugJsonMapper.writeValueAsString(byydResponse)));
            } catch (Exception x) {
                x.printStackTrace(writer);
            }
            writer.println("</pre>");
            writer.println("<hr/>");
        }

        DbgImpressionBidDto bidImpression = dbgBid.getBidImpression();
        if (bidImpression != null) {
            writer.println("<p><strong>Byyd Impression</strong></p>");
            writer.println("<pre>");
            try {
                writer.println(DebugBidController.debugJsonMapper.writeValueAsString(bidImpression));
            } catch (Exception x) {
                x.printStackTrace(writer);
            }
            writer.println("</pre>");
        }
        writer.flush();
    }

    private DbgCreativeEliminationDto findElimination(long creativeId, List<DbgCreativeEliminationDto> eliminationList) {
        if (eliminationList != null) {
            for (DbgCreativeEliminationDto item : eliminationList) {
                if (creativeId == item.getCreativeId().longValue()) {
                    return item;
                }
            }
        }
        return null;
    }
}
