package com.adfonic.adserver.controller;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.InvalidTrackingIdentifierException;
import com.adfonic.adserver.MutableWeightedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TargetingEventAdapter;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.domain.Gender;
import com.adfonic.domain.Medium;
import com.adfonic.domain.cache.dto.adserver.CapabilityDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.PostalCode;
import com.adfonic.geo.USState;
import com.adfonic.geo.USZipCode;
import com.adfonic.util.AcceptedLanguages;
import com.adfonic.util.Range;

@Controller
public class DiagnosticController extends AbstractAdServerController {
    private static final transient Logger LOG = Logger.getLogger(DiagnosticController.class.getName());

    @Autowired
    private TargetingEngine targetingEngine;

    /** Generic ad-generating request handler */
    @RequestMapping("/diag/{adSpaceExternalID}")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response, @PathVariable String adSpaceExternalID) throws ServletException, java.io.IOException {
        // Prevent caching
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");

        // Create the targeting context
        TargetingContext context;
        try {
            context = getTargetingContextFactory().createTargetingContext(request, false);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        // Look up the AdSpace
        AdSpaceDto adSpace = context.getAdserverDomainCache().getAdSpaceByExternalID(adSpaceExternalID);
        if (adSpace == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such AdSpace: " + adSpaceExternalID);
            return;
        }
        context.setAdSpace(adSpace);
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("AdSpace \"" + adSpace.getName() + "\" externalID=" + adSpace.getExternalID());
            LoggingUtils.log(LOG, Level.FINE, null, context, this.getClass(), "handleConversionFromServer",
                    "AdSpace \"" + adSpace.getName() + "\" externalID=" + adSpace.getExternalID());
        }

        try {
            // Make sure tracking identifier stuff is set up on the targeting context.
            getTrackingIdentifierLogic().establishTrackingIdentifier(context, response, false); // cookies are not allowed
        } catch (InvalidTrackingIdentifierException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        // Set up a listener to keep track of stuff
        DiagnosticEventListener listener = new DiagnosticEventListener();

        // Select the ad...we don't care about the return value here
        targetingEngine.selectCreative(adSpace, /*allowedFormats*/null, context, /*diagnosticMode*/true, false, /*timeConstraint*/null, listener);

        response.setContentType("text/plain");
        PrintStream out = new PrintStream(response.getOutputStream());

        out.print("<p><b>Selected:</b> ");
        if (listener.selected == null) {
            out.println("null<br/>");
            out.println("<b>Unfilled Reason:</b> " + context.getAttribute(TargetingContext.UNFILLED_REASON));
        } else {
            out.print(listener.selected.getName());
            out.print(" (campaign.id=<a href=\"campaign_digger.jsp?campaignId=");
            out.print(listener.selected.getCampaign().getId());
            out.print("\">");
            out.print(listener.selected.getCampaign().getId());
            out.print("</a> - ");
            out.print(listener.selected.getCampaign().getName());
            out.print(", priority=");
            out.print(listener.selected.getPriority());
            out.println(")");
        }
        out.println("</p>");

        if (listener.timeLimitExpired) {
            out.println("<p><b>***** TIME LIMIT EXPIRED *****</b></p>");
        }

        out.println("<p><b>HTTP Headers:</b><br/><table class=\"diagnostic\">");
        for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
            out.println("<tr><th nowrap>" + entry.getKey() + "</th><td>" + entry.getValue() + "</td></tr>");
        }
        out.println("</table></p>");

        out.print("<p><b>Device Properties:</b> ");
        Map<String, String> deviceProps = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (deviceProps == null || deviceProps.isEmpty()) {
            out.println("UNKNOWN/NONE");
        } else {
            out.println("<br/><table class=\"diagnosticSmall\">");
            for (Map.Entry<String, String> entry : deviceProps.entrySet()) {
                out.println("<tr><th>" + entry.getKey() + "</th><td>" + entry.getValue() + "</td></tr>");
            }
            out.println("</table>");
        }
        out.println("</p>");

        /*
        Map<FormatDto,DisplayTypeDto> displayTypesPerFormat = context.getAttribute(TargetingContext.DISPLAY_TYPES_BY_FORMAT);
        if (displayTypesPerFormat == null || displayTypesPerFormat.isEmpty()) {
            out.println("DisplayTypeDto per Format: UNKNOWN/NONE");
        } else {
            out.println("DisplayTypeDto per Format:");
            for (FormatDto format : displayTypesPerFormat.keySet()) {
                DisplayTypeDto displayType = displayTypesPerFormat.get(format);
                out.println("Format=" + format.getSystemName() + ", DisplayType=" + displayType.getSystemName());
            }
        }
        */

        out.println("<p><b>Derived Attributes:</b><br/><table class=\"diagnostic\">");
        out.print("<tr><th>Medium</th><td>");
        Medium medium = context.getAttribute(TargetingContext.MEDIUM);
        if (medium != null) {
            out.print(medium);
        } else {
            out.print("using pub type");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Country</th><td>");
        CountryDto country = context.getAttribute(TargetingContext.COUNTRY);
        long countryId = 0;
        if (country != null) {
            out.print(country.getIsoCode() + " (" + country.getName() + ")");
            countryId = country.getId();
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>TimeZone</th><td>");
        TimeZone timeZone = context.getAttribute(TargetingContext.TIME_ZONE);
        if (timeZone != null) {
            out.print(timeZone.getID());
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Model</th><td>");
        ModelDto model = context.getAttribute(TargetingContext.MODEL);
        if (model != null) {
            out.print(model.getVendor().getName() + " " + model.getName() + " (externalID=" + model.getExternalID() + ")");
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Operator</th><td>");
        OperatorDto operator = context.getAttribute(TargetingContext.OPERATOR);
        if (operator != null) {
            out.print(operator.getName());
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Platform</th><td>");
        PlatformDto platform = context.getAttribute(TargetingContext.PLATFORM);
        if (platform != null) {
            out.print(platform.getSystemName());
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Gender</th><td>");
        Gender gender = context.getAttribute(TargetingContext.GENDER);
        if (gender != null) {
            out.print(gender);
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Coordinates</th><td>");
        Coordinates coordinates = context.getAttribute(TargetingContext.COORDINATES);
        if (coordinates != null) {
            out.print(coordinates);
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Geotarget</th><td>");
        GeotargetDto geotarget = context.getAttribute(TargetingContext.GEOTARGET);
        if (geotarget != null) {
            out.print("id=" + geotarget.getId() + ", name=" + geotarget.getName());
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        Dma dma = context.getAttribute(TargetingContext.DMA);
        if (dma != null) {
            out.println("<tr><th>DMA</th><td>" + dma.getCode() + " / " + dma.getName() + "</td></tr>");
        }

        USState usState = context.getAttribute(TargetingContext.US_STATE);
        if (usState != null) {
            out.println("<tr><th>US State</th><td>" + usState + "</td></tr>");
        }

        USZipCode usZipCode = context.getAttribute(TargetingContext.US_ZIP_CODE);
        if (usZipCode != null) {
            out.println("<tr><th>US ZipCode</th><td>" + usZipCode.getZip() + " (" + usZipCode.getCity() + ", " + usZipCode.getState() + ")</td></tr>");
        }

        PostalCode ukPostalCode = context.getAttribute(TargetingContext.UK_POSTAL_CODE);
        if (ukPostalCode != null) {
            out.println("<tr><th>UK PostalCode</th><td>" + ukPostalCode.getPostalCode() + " (name=" + ukPostalCode.getName() + ", city=" + ukPostalCode.getCity() + ")</td></tr>");
        }

        out.print("<tr><th>Age Range</th><td>");
        Range<Integer> ageRange = context.getAttribute(TargetingContext.AGE_RANGE);
        if (ageRange != null) {
            out.print(ageRange);
        } else {
            out.print("UNKNOWN");
        }
        out.println("</td></tr>");

        out.print("<tr><th>Accepted Languages</th><td>");
        AcceptedLanguages acceptedLanguages = context.getAttribute(TargetingContext.ACCEPTED_LANGUAGES);
        if (acceptedLanguages == null || acceptedLanguages.isNone()) {
            out.print("none/unknown");
        } else if (acceptedLanguages.isAny()) {
            out.print("ANY");
        } else {
            out.println("<ul>");
            for (String isoCode : acceptedLanguages.getIsoCodes()) {
                LanguageDto language = context.getDomainCache().getLanguageByIsoCode(isoCode);
                if (language == null) {
                    out.println("<li>unknown:" + isoCode + "</li>");
                } else {
                    out.println("<li>" + language.getISOCode() + " (" + language.getName() + ")</li>");
                }
            }
            out.println("<ul>");
        }
        out.println("</td></tr>");
        out.println("</table></p>");

        // Derive the viewer's capabilities
        out.print("<p><b>Capabilities:</b> ");
        Collection<CapabilityDto> capabilities = context.getAttribute(TargetingContext.CAPABILITIES);
        if (capabilities == null || capabilities.isEmpty()) {
            out.print("UNKNOWN/NONE");
        } else {
            out.println("<ul>");
            for (CapabilityDto capability : capabilities) {
                out.println("<li>" + capability.getName() + "</li>");
            }
            out.println("</ul>");
        }
        out.println("</p>");

        for (Map.Entry<Integer, List<MutableWeightedCreative>> entry : listener.targetedByPriority.entrySet()) {
            out.println("<p><b># of priority="
                    + entry.getKey()
                    + " targeted creatives: "
                    + entry.getValue().size()
                    + "</b><br/><table class=\"diagnostic\"><tr><th>Name</th><th>Campaign</th><th>gender<br/>mix<br/>weight</th><th>age<br/>brange<br/>weight</th><th>ecpm<br/>weight</th></tr>");
            for (MutableWeightedCreative mwc : entry.getValue()) {
                out.print("<tr>");
                printMutableWeightedCreativeColumns(mwc, out);
                out.print("</tr>");
            }
            out.println("</table></p>");
        }

        out.print("<p><b># of creatives ELIMINATED: ");
        out.print(listener.eliminated.size());
        out.println("</b><br/><table class=\"diagnosticSmall\"><tr><th>Reason<br/>Eliminated</th><th>Priority</th><th>Name</th><th>Campaign</th><th>weight</th><th>expectedRevenue</th><th>bidPrice</th><th>expectedProfit</th><th>expectedSettlementPrice</th><th>winningProbability</th></tr>");
        for (Map.Entry<CreativeDto, String> entry : listener.eliminated.entrySet()) {
            CreativeDto wc = entry.getKey();
            out.print("<tr><td align=center>" + entry.getValue() + "</td>");
            //TODO : if we really need priority to show on UI then may need to change datastructure bur for now just commenting it
            out.print("<td align=center>" + wc.getPriority() + "</td>");
            printWeightedCreativeColumns(wc, adSpace, context, platform, countryId, out);
            out.println("</tr>");
        }
        out.println("</table></p>");

        for (Map.Entry<Integer, List<CreativeDto>> entry : listener.eligible.entrySet()) {
            out.print("<p><b># of priority=");
            out.print(entry.getKey());
            out.print(" eligible creatives: ");
            out.print(entry.getValue().size());
            out.println("</b><br/><table class=\"diagnosticSmall\"><tr><th>Name</th><th>Campaign</th><th>weight</th><th>expectedRevenue</th><th>bidPrice</th><th>expectedProfit</th><th>expectedSettlementPrice</th><th>winningProbability</th></tr>");
            for (CreativeDto wc : entry.getValue()) {
                out.print("<tr>");
                printWeightedCreativeColumns(wc, adSpace, context, platform, countryId, out);
                out.println("</tr>");
            }
            out.println("</table></p>");
        }
        out.close();
    }

    private static final class DiagnosticEventListener extends TargetingEventAdapter {
        private final Map<Integer, List<CreativeDto>> eligible = new TreeMap<Integer, List<CreativeDto>>();
        private final Map<CreativeDto, String> eliminated = new LinkedHashMap<CreativeDto, String>();
        private final Map<Integer, List<MutableWeightedCreative>> targetedByPriority = new TreeMap<Integer, List<MutableWeightedCreative>>();
        private CreativeDto selected = null;
        private boolean timeLimitExpired = false;

        @Override
        public void creativesEligible(AdSpaceDto adSpace, TargetingContext context, AdspaceWeightedCreative[] eligibleCreatives) {
            this.eligible.clear();
            CreativeDto oneEligibleCreative;

            for (AdspaceWeightedCreative oneAdspaceWeightedCreative : eligibleCreatives) {
                this.eligible.put(oneAdspaceWeightedCreative.getPriority(), new ArrayList<CreativeDto>(oneAdspaceWeightedCreative.getCreativeIds().length + 1));
                for (Long oneCreativeId : oneAdspaceWeightedCreative.getCreativeIds()) {
                    oneEligibleCreative = context.getAdserverDomainCache().getCreativeById(oneCreativeId);
                    this.eligible.get(oneAdspaceWeightedCreative.getPriority()).add(oneEligibleCreative);
                }

            }
        }

        @Override
        public void creativeEliminated(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative, CreativeEliminatedReason reason, String detailedReason) {
            this.eliminated.put(creative, detailedReason);
        }

        @Override
        public void creativesTargeted(AdSpaceDto adSpace, TargetingContext context, int priority, List<MutableWeightedCreative> targetedCreatives) {
            targetedByPriority.put(priority, new ArrayList<MutableWeightedCreative>());
            for (MutableWeightedCreative mwc : targetedCreatives) {
                // We need to make our own copies here because the one handed to
                // us is "reusable" and will fall out of scope as soon as the
                // targeting phase is complete.  That's an optimization to help
                // reduce garbage generation.  Since this diagnostic tool is only
                // run as a one-off here and there, I have no qualms about making
                // copies here.
                targetedByPriority.get(priority).add(new MutableWeightedCreative(mwc));
            }
        }

        @Override
        public void creativeSelected(AdSpaceDto adSpace, TargetingContext context, CreativeDto creative) {
            this.selected = creative;
        }

        @Override
        public void timeLimitExpired(AdSpaceDto adSpace, TargetingContext context, TimeLimit timeLimit) {
            this.timeLimitExpired = true;
        }
    }

    private static void printWeightedCreativeColumns(CreativeDto wc, AdSpaceDto adspace, TargetingContext context, PlatformDto platform, long countryId, PrintStream out)
            throws java.io.IOException {
        out.print("<td align=left>" + wc.getName() + "</td>");
        out.print("<td align=center><a href=\"campaign_digger.jsp?campaignId=" + wc.getCampaign().getId() + "\">" + wc.getCampaign().getId() + "</a> - "
                + wc.getCampaign().getName() + "</td>");
        EcpmInfo ecpmInfo = new EcpmInfo();
        context.getAdserverDomainCache().computeEcpmInfo(adspace, wc, platform, countryId, new BigDecimal(0.0), ecpmInfo);
        out.print("<td align=center>" + ecpmInfo.getWeight() + "</td>");
        out.print("<td align=center>" + ecpmInfo.getExpectedRevenue() + "</td>");
        out.print("<td align=center>" + ecpmInfo.getBidPrice() + "</td>");
        out.print("<td align=center>" + ecpmInfo.getExpectedProfit() + "</td>");
        out.print("<td align=center>" + ecpmInfo.getExpectedSettlementPrice() + "</td>");
        out.print("<td align=center>" + ecpmInfo.getWinningProbability() + "</td>");

    }

    private static void printMutableWeightedCreativeColumns(MutableWeightedCreative mwc, PrintStream out) throws java.io.IOException {
        out.print("<td align=left>" + mwc.getCreative().getName() + "</td>");
        out.print("<td align=center><a href=\"campaign_digger.jsp?campaignId=" + mwc.getCreative().getCampaign().getId() + "\">" + mwc.getCreative().getCampaign().getId()
                + "</a> - " + mwc.getCreative().getCampaign().getName() + "</td>");
        out.print("<td align=center>" + mwc.getEcpmWeight() + "</td>");
        out.print("<td align=center>" + mwc.getWeight() + "</td>");
    }
}
