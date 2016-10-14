package com.adfonic.adserver.controller.fish;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.controller.dbg.DbgUiUtil;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.fish.RtbFisherman.FishingSession;
import com.adfonic.adserver.controller.fish.RtbFishnetConfig.CatchType;
import com.adfonic.adserver.controller.fish.RtbFishnetConfig.RtbOutcome;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

/**
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping("/adserver")
public class FishingController {

    @Autowired
    private RtbFisherman fisher;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    /**
     * Reset fisher if something goes wrong... 
     */
    @RequestMapping(value = "/fisher/reset", method = RequestMethod.GET)
    public void reset(HttpServletResponse httpResponse) throws IOException {
        fisher.reset();
        httpResponse.sendRedirect("/adserver/fisher");
    }

    @RequestMapping(value = "/fisher", method = RequestMethod.GET)
    public void view(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setCharacterEncoding("UTF-8");
        PrintWriter writer = httpResponse.getWriter();
        writer.println("<html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>");
        writer.println("<head><title>Rtb Fisher</title></head>");
        writer.println("<body>");

        boolean allExchanges = httpRequest.getParameter("allExchanges") != null;

        FishingSession session = fisher.getSession();
        if (session != null) {
            Date stopedAt = session.getStopedAt();
            if (stopedAt != null) {
                printStartForm(writer, session.getConfig(), allExchanges);
            } else {
                printStopForm(writer);
            }
            printSession(session, writer);
        } else {
            printStartForm(writer, null, allExchanges);
        }
        writer.println("</body>");
        writer.println("</html>");
    }

    @RequestMapping(value = "/fisher/start", method = RequestMethod.POST)
    public void start(HttpServletRequest httpRequest, HttpServletResponse httpResponse,//
            @RequestParam(value = "exchange", required = false) String exchangeIdent,//
            @RequestParam(value = "rtbRequestExpression", required = false) String rtbRequestExpression,//
            @RequestParam(value = "rtbRequestRegex", required = false) Boolean rtbRequestRegex,//
            @RequestParam(value = "rtbRequestNegate", required = false) Boolean rtbRequestNegate,//
            @RequestParam(value = "rtbResponseExpression", required = false) String rtbResponseExpression,//
            @RequestParam(value = "rtbResponseRegex", required = false) Boolean rtbResponseRegex,//
            @RequestParam(value = "rtbResponseNegate", required = false) Boolean rtbResponseNegate,//
            @RequestParam(value = "rtbOutcome", required = false) RtbFishnetConfig.RtbOutcome rtbOutcome,//
            @RequestParam(value = "adSpaceSpec", required = false) String adSpaceSpec,//
            @RequestParam(value = "publicationSpec", required = false) String publicationSpec,//
            @RequestParam(value = "creativeSpec", required = false) String creativeSpec,//
            @RequestParam(value = "campaignSpec", required = false) String campaignSpec,//
            @RequestParam(value = "limitSeconds", defaultValue = "60") Integer limitSeconds,//
            @RequestParam(value = "limitCatches", defaultValue = "1") Integer limitCatches,//
            @RequestParam(value = "action", defaultValue = "start") String action//
    ) throws IOException {

        if ("reset".equalsIgnoreCase(action)) {
            fisher.reset();
            httpResponse.sendRedirect("/adserver/fisher");
            return;
        }

        RtbFishnetConfig.Builder builder = new RtbFishnetConfig.Builder();
        builder.setCatchType(CatchType.TRAFFIC);
        builder.setCatchLimit(limitCatches).setTimeLimit(limitSeconds, TimeUnit.SECONDS);

        if (StringUtils.isNotEmpty(rtbRequestExpression)) {
            boolean isNegated = rtbRequestNegate != null && rtbRequestNegate.booleanValue();
            boolean isRegex = rtbRequestRegex != null && rtbRequestRegex.booleanValue();
            if (isRegex) {
                builder.addRtbRequestRegex(rtbRequestExpression, !isNegated);
            } else {
                String[] splits = rtbRequestExpression.split("&&");
                for (String split : splits) {
                    builder.addRtbRequestContains(split.trim(), !isNegated);
                }
            }
        }
        if (StringUtils.isNotEmpty(rtbResponseExpression)) {
            boolean isNegated = rtbResponseNegate != null && rtbResponseNegate.booleanValue();
            boolean isRegex = rtbResponseRegex != null && rtbResponseRegex.booleanValue();
            if (isRegex) {
                builder.addRtbResponseRegex(rtbResponseExpression, !isNegated);
            } else {
                String[] splits = rtbResponseExpression.split("&&");
                for (String split : splits) {
                    builder.addRtbResponseContains(split.trim(), !isNegated);
                }
            }
        }

        if (StringUtils.isNotEmpty(adSpaceSpec)) {
            AdSpaceDto adSpace = DbgUiUtil.findAdSpace(adSpaceSpec, adserverCacheManager.getCache());
            if (adSpace != null) {
                builder.setAdSpaceId(adSpace.getId());
                exchangeIdent = adSpace.getPublication().getPublisher().getExternalId();
            } else {
                httpResponse.sendError(400, "AdSpace not found: " + adSpaceSpec);
                return;
            }
        }

        if (StringUtils.isNotEmpty(publicationSpec)) {
            PublicationDto publication = DbgUiUtil.findPublication(publicationSpec, adserverCacheManager.getCache());
            if (publication != null) {
                builder.setPublicationId(publication.getId());
                exchangeIdent = publication.getPublisher().getExternalId();
            } else {
                httpResponse.sendError(400, "Publication not found: " + publicationSpec);
                return;
            }
        }

        if (StringUtils.isNotEmpty(creativeSpec)) {
            CreativeDto creative = DbgUiUtil.findCreative(creativeSpec, adserverCacheManager.getCache());
            if (creative != null) {
                builder.setCreativeId(creative.getId());
            } else {
                httpResponse.sendError(400, "Creative not found: " + creativeSpec);
                return;
            }
        }

        if (StringUtils.isNotEmpty(campaignSpec)) {
            CampaignDto campaign = DbgUiUtil.findCampaign(campaignSpec, adserverCacheManager.getCache());
            if (campaign != null) {
                builder.setCampaignId(campaign.getId());
            } else {
                httpResponse.sendError(400, "Campaign not found: " + campaignSpec);
                return;
            }
        }

        if (rtbOutcome != null) {
            builder.setRtbOutcome(rtbOutcome);
        }

        if (StringUtils.isBlank(exchangeIdent)) {
            httpResponse.sendError(400, "Exchange not specified");
            return;
        }
        RtbExchange exchange = RtbExchange.lookup(exchangeIdent);
        builder.setExchangeExternalId(exchange.getPublisherExternalId());

        RtbFishnetConfig config = builder.build();
        fisher.startSession(config);
        httpResponse.sendRedirect("/adserver/fisher");
    }

    @RequestMapping(value = "/fisher/stop", method = { RequestMethod.GET, RequestMethod.POST })
    public void stop(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        fisher.stopSession();
        httpResponse.sendRedirect("/adserver/fisher");
    }

    private void printStopForm(PrintWriter writer) {
        writer.println("<form method='POST' action='/adserver/fisher/stop' accept-charset='UTF-8'>");
        writer.println("<input type='submit' value='Stop'/>");
        writer.println("</form>");
    }

    private void printStartForm(PrintWriter writer, RtbFishnetConfig rtbFishnetConfig, boolean allExchanges) {
        //writer.println("<html><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/><body>");
        writer.println("<form method='POST' action='/adserver/fisher/start' accept-charset='UTF-8'>");
        RtbExchange rtbExchange = null;
        if (rtbFishnetConfig != null) {
            rtbExchange = RtbExchange.lookup(rtbFishnetConfig.getExchangeExternalId());
        }

        DbgUiUtil.printExchangesRadios(writer, adserverCacheManager.getCache(), allExchanges, rtbExchange);
        writer.println("<br/>");
        writer.println("Rtb Request: &nbsp;&nbsp;<input name='rtbRequestExpression' size='32' title='Use &amp;&amp; to separate multiple searched strings' />");
        writer.println("Regex: <input type='checkbox' name='rtbRequestRegex'/>");
        writer.println("Negate: <input type='checkbox' name='rtbRequestNegate'/>");
        writer.println("AdSpace: <input name='adSpaceSpec'/>");
        writer.println("Publication: <input name='publicationSpec'/>");
        writer.println("<br/>");
        writer.println("Rtb Response: <input name='rtbResponseExpression' size='32' />");
        writer.println("Regex: <input type='checkbox' name='rtbResponseRegex'/>");
        writer.println("Negate: <input type='checkbox' name='rtbResponseNegate'/>");
        writer.println("Creative: &nbsp;<input name='creativeSpec'/>");
        writer.println("Campaign: <input name='campaignSpec'/>");
        writer.println("<br/>");
        writer.println("Rtb Outcome: <select name='rtbOutcome'>");
        writer.println("<option value=''></option>");
        writer.println("<option value='" + RtbOutcome.BID_MADE + "'>Bid</option>");
        writer.println("<option value='" + RtbOutcome.NOBID + "'>Nobid</option>");
        writer.println("<option value='" + RtbOutcome.FAILURE + "'>Failure</option>");
        writer.println("<option value='" + RtbOutcome.INVALID + "'>Invalid</option>");
        writer.println("<option value='" + RtbOutcome.DROPPED + "'>Dropped</option>");
        writer.println("</select>");
        writer.println("Seconds: &nbsp;<input name='limitSeconds' value='60' size='3'/>");
        writer.println("Catches: <input name='limitCatches' value='1'/ size='3'>");
        writer.println("<br/>");
        writer.println("<input type='submit' name='action' value='Start'/>");
        writer.println("<input type='submit' name='action' value='Reset'/>");
        writer.println("</form>");
    }

    private void printSession(FishingSession session, PrintWriter writer) {
        writer.println("<b>Session</b><br/>");
        writer.println("Started: " + DbgUiUtil.format(session.getStartedAt()) + ", Ended: " + DbgUiUtil.format(session.getStopedAt()) + ", Matches: " + session.getMatchCount()
                + " of " + session.getConfig().getCatchLimit() + ", Misses: " + session.getMissCount());
        writer.println("<hr/>");

        List<RtbExecutionContext<?, ?>> catches = session.getCatches();
        for (int i = 0; i < catches.size(); ++i) {
            RtbExecutionContext<?, ?> exctx = catches.get(i);
            ByydRequest byydRequest = exctx.getByydRequest();

            writer.println("<b><a href='/adserver/bidebug?fisherIndex=" + i + "' target='_blank'>Request</a></b><br/>");

            if (byydRequest != null) {
                AdSpaceDto adSpace = byydRequest.getAdSpace();
                if (adSpace != null) {
                    writer.print("AdSpace: " + DbgUiUtil.adspaceLink(adSpace.getId()));
                    writer.print(", Publication: " + DbgUiUtil.publicationLink(adSpace.getPublication().getId()) + " '" + adSpace.getPublication().getName() + "'");
                } else {
                    writer.print(" RtbId: " + byydRequest.getPublicationRtbId());
                }
            } else {
                writer.println(" From: " + exctx.getPublisherExternalId());
            }

            writer.println("<br/>");
            DbgUiUtil.writeRtbTimestamps(writer, exctx);

            RtbExchange exchange = RtbExchange.lookup(session.getConfig().getExchangeExternalId());

            writer.println("<pre>");
            String requestString = exctx.getRtbRequestString();
            if (exchange.getEndpoint().getProtocol().getRequestMediaType() == MediaType.APPLICATION_JSON) {
                requestString = DbgUiUtil.indentJson(requestString);
            }

            writer.println(requestString);
            writer.println("</pre>");
            writer.println("<hr/>");

            String responseString = exctx.getRtbResponseString();
            if (responseString != null) {
                writer.println("<b>Response</b>");
                writer.println("<br/>");

                ByydResponse byydResponse = exctx.getByydResponse();
                CreativeDto creative = null;
                if (byydResponse != null) {
                    creative = byydResponse.getBid().getCreative();
                }
                if (creative != null) {
                    writer.print("Creative: " + DbgUiUtil.creativeLink(creative.getId()) + " '" + creative.getName() + "'");
                    writer.print(", Campaign: " + DbgUiUtil.campaignLink(creative.getCampaign().getId()) + " '" + creative.getCampaign().getName() + "'");
                }

                writer.println("<pre>");

                if (exchange.getEndpoint().getProtocol().getResponseMediaType() == MediaType.APPLICATION_JSON) {
                    responseString = DbgUiUtil.indentJson(responseString);
                }
                responseString = StringEscapeUtils.escapeHtml(responseString); //ad markup is html

                writer.println(responseString);
                writer.println("</pre>");
                writer.println("<hr/>");
            }
            Exception exception = exctx.getException();
            if (exception != null) {
                if (exception instanceof NoBidException) {
                    NoBidException nbx = ((NoBidException) exception);
                    writer.print("Nobid Reason: " + nbx.getNoBidReason());
                    if (nbx.getOffenceName() != null) {
                        writer.print(" : " + nbx.getOffenceName());
                    }
                    if (nbx.getOffenceValue() != null) {
                        writer.print(" : " + nbx.getOffenceValue());
                    }

                } else {
                    writer.println(exception);
                    writer.println("<pre>");
                    StackTraceElement[] stackTrace = exception.getStackTrace();
                    for (StackTraceElement element : stackTrace) {
                        writer.print(element.getClassName());
                        writer.print("(" + element.getFileName() + ":" + element.getLineNumber() + ")");
                        writer.println();
                    }
                    writer.println("</pre>");
                }
                writer.println("<hr/>");
            }
        }
    }

}
