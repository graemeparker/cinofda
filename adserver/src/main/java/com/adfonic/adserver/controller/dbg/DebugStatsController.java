package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.rtb.util.RtbStats;
import com.adfonic.adserver.rtb.util.RtbStats.StatsEntry;
import com.adfonic.adserver.rtb.util.RtbStats.StatsTargetingEventListener;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.codahale.metrics.Meter;

@Controller
@RequestMapping(DebugStatsController.URL_CONTEXT)
public class DebugStatsController {

    public static final String URL_CONTEXT = "/adserver/stats";

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @RequestMapping(method = RequestMethod.POST)
    public void setup(@RequestParam(name = "creative") String creativeIdent, @RequestParam(name = "adspace", required = false) String adSpaceIdent,
            @RequestParam(name = "duration", defaultValue = "60") Integer duration, HttpServletResponse httpResponse) throws IOException {

        AdserverDomainCache adCache = adserverCacheManager.getCache();
        CreativeDto creative = DbgUiUtil.getCreative(creativeIdent, adCache);
        Long adSpaceId = null;
        if (StringUtils.isNotBlank(adSpaceIdent)) {
            adSpaceId = DbgUiUtil.getAdSpace(adSpaceIdent, adCache).getId();
        }
        // Existing Targeting debugging can be replaced...
        RtbStats.i().setTargetingGathering(duration, creative.getId(), adSpaceId);
        httpResponse.sendRedirect(URL_CONTEXT);
    }

    private void printForm(PrintWriter writer) {
        StatsTargetingEventListener listener = RtbStats.i().getTargetingListener();
        if (listener == null) {
            writer.println("<h3>Set up RTB targeting debugging</h3>");
            writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
            writer.println("Creative: <input name='creative' />");
            writer.println("AdSpace: <input name='adspace' />");
            writer.println("Duration: <input name='duration' size='4'/> seconds");
            writer.println("<input type='submit' value='Start'/>");
            writer.println("</form>");
        } else {
            writer.print("Running since " + DbgUiUtil.format(listener.getStartedAt()) + " for " + listener.getDurationSecs() + " seconds.  Creative " + listener.getCreativeId()
                    + ", AdSpace: " + listener.getAdSpaceId());
        }
    }

    /**
     * Detail RTB Stats for individual Creative
     */
    @RequestMapping(path = "/creative/{creative}", method = RequestMethod.GET)
    public void creative(HttpServletResponse httpResponse, @PathVariable("creative") Long creativeId) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        AdserverDomainCache adCache = adserverCacheManager.getCache();
        Map<Long, StatsEntry> creativeStats = RtbStats.i().get(creativeId);
        writer.println(DbgUiUtil.HTML_OPEN);
        writer.print("<h3>Detail RTB Stats for Creative: " + creativeId + "</h3>");
        if (creativeStats != null) {
            printCreativeStats(writer, creativeId, adCache, creativeStats, true);
        } else {
            writer.println("Not RTB stats found for creative: " + creativeId);
        }
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    /**
     * Detail RTB Stats for individual AdSpace 
     */
    @RequestMapping(path = "/adspace/{adspace}", method = RequestMethod.GET)
    public void adspace(HttpServletResponse httpResponse, @PathVariable("adspace") Long adSpaceId) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        AdserverDomainCache adCache = adserverCacheManager.getCache();

        writer.println(DbgUiUtil.HTML_OPEN);
        writer.print("<h3>Detail RTB Stats for AdSpace: " + adSpaceId + "</h3>");
        printAdSpaceStats(writer, adSpaceId, adCache);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    /**
     * Summary RTB Stats for all creatives
     */
    @RequestMapping(method = RequestMethod.GET)
    public void index(HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        printForm(writer);

        AdserverDomainCache adCache = adserverCacheManager.getCache();

        Map<Long, ConcurrentHashMap<Long, StatsEntry>> creatives = RtbStats.i().get();
        writer.println("<h3>RTB stats for " + creatives.size() + " creatives</h3>");
        List<Long> sortedCreativeIds = new ArrayList<Long>(creatives.keySet());
        Collections.sort(sortedCreativeIds);
        for (Long creativeId : sortedCreativeIds) {
            ConcurrentHashMap<Long, StatsEntry> creativeStats = creatives.get(creativeId);
            printCreativeStats(writer, creativeId, adCache, creativeStats, false);
            writer.println("<hr/>");
        }

        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    private static void printAdSpaceStats(PrintWriter writer, Long adSpaceId, AdserverDomainCache adCache) {
        printAdSpaceLine(writer, adSpaceId, adCache);

        int totalBids = 0;
        int totalWins = 0;
        int totalImps = 0;
        int totalClicks = 0;

        Map<Long, ConcurrentHashMap<Long, StatsEntry>> creatives = RtbStats.i().get();
        for (Entry<Long, ConcurrentHashMap<Long, StatsEntry>> entry : creatives.entrySet()) {
            Long creativeId = entry.getKey();
            ConcurrentHashMap<Long, StatsEntry> creativeStats = entry.getValue();
            StatsEntry stats = creativeStats.get(adSpaceId);
            if (stats != null) {
                printCreativeLine(writer, creativeId, adCache);
                Meter bidsMeter = stats.getRtbBids();
                totalBids += bidsMeter.getCount();
                Meter winsMeter = stats.getRtbWins();
                totalWins += winsMeter.getCount();
                Meter impsMeter = stats.getImpressions();
                totalImps += impsMeter.getCount();
                Meter clicksMeter = stats.getClicks();
                totalClicks += clicksMeter.getCount();

                writer.print("Bids: " + bidsMeter.getCount() + ", Wins: " + winsMeter.getCount() + ", Imps: " + impsMeter.getCount() + ", Clicks: " + clicksMeter.getCount());
                writer.println("<br/>");

                printLosesTable(writer, stats.getRtbLoses());
                printTargetingTable(writer, stats.getEliminations());
            }
        }
        writer.println("Total Bids: " + totalBids + ", Wins: " + totalWins + ", Imps: " + totalImps + ", Clicks: " + totalClicks);
    }

    private static void printCreativeStats(PrintWriter writer, Long creativeId, AdserverDomainCache adCache, Map<Long, StatsEntry> creativeStats, boolean breakdown) {
        printCreativeLine(writer, creativeId, adCache);
        List<Long> sortedAdspaceIds = new ArrayList<Long>(creativeStats.keySet());
        Collections.sort(sortedAdspaceIds);
        int totalBids = 0;
        int totalWins = 0;
        int totalImps = 0;
        int totalClicks = 0;
        for (Long adspaceId : sortedAdspaceIds) {
            StatsEntry stats = creativeStats.get(adspaceId);
            Meter bidsMeter = stats.getRtbBids();
            totalBids += bidsMeter.getCount();
            Meter winsMeter = stats.getRtbWins();
            totalWins += winsMeter.getCount();
            Meter impsMeter = stats.getImpressions();
            totalImps += impsMeter.getCount();
            Meter clicksMeter = stats.getClicks();
            totalClicks += clicksMeter.getCount();
            if (breakdown) {
                String adspaceUrl = adSpaceLink(adspaceId);
                writer.print(adspaceUrl + " " + DbgUiUtil.adspaceLink(adspaceId) + ", Bids: " + bidsMeter.getCount() + ", Wins: " + winsMeter.getCount() + ", Imps: "
                        + impsMeter.getCount() + ", Clicks: " + clicksMeter.getCount());
                writer.println("<br/>");

                printLosesTable(writer, stats.getRtbLoses());
                printTargetingTable(writer, stats.getEliminations());
            }
        }
        writer.println("Total Bids: " + totalBids + ", Wins: " + totalWins + ", Imps: " + totalImps + ", Clicks: " + totalClicks);
    }

    private static void printTargetingTable(PrintWriter writer, Map<CreativeEliminatedReason, Meter> eliminations) {
        if (!eliminations.isEmpty()) {
            ArrayList<CreativeEliminatedReason> sortedReasons = new ArrayList<>(eliminations.keySet());
            Collections.sort(sortedReasons);
            writer.println("Mistargeting by reason");
            writer.println("<table border='0'>");
            writer.println("<tr><th>Reason</th><th>Count</th><th>Mean Rate</th><th>15m Rate</th></tr>");

            for (CreativeEliminatedReason reason : sortedReasons) {
                Meter reasonMeter = eliminations.get(reason);
                writer.println("<tr><td>" + reason + "</td><td>" + reasonMeter.getCount() + "</td><td>" + reasonMeter.getMeanRate() + "</td><td>"
                        + reasonMeter.getFifteenMinuteRate() + "</td></tr>");
            }
            writer.println("</table>");
        }
    }

    private static void printLosesTable(PrintWriter writer, Map<String, Meter> rtbLoses) {
        if (!rtbLoses.isEmpty()) {
            List<String> sortedLosesIds = new ArrayList<String>(rtbLoses.keySet());
            Collections.sort(sortedLosesIds);
            writer.println("Losses by reason");
            writer.println("<table border='0'>");
            writer.println("<tr><th>Type</th><th>Count</th><th>Mean Rate</th><th>15m Rate</th></tr>");
            for (String lossId : sortedLosesIds) {
                Meter lossMeter = rtbLoses.get(lossId);
                // long loses = lossMeter.getCount();
                writer.println("<tr><td>" + lossId + "</td><td>" + lossMeter.getCount() + "</td><td>" + lossMeter.getMeanRate() + "</td><td>" + lossMeter.getFifteenMinuteRate()
                        + "</td></tr>");
            }
            writer.println("</table>");
        }
    }

    private static AdSpaceDto printAdSpaceLine(PrintWriter writer, Long adSpaceId, AdserverDomainCache adCache) {
        String adspaceUrl = adSpaceLink(adSpaceId);
        AdSpaceDto adSpace = adCache.getAdSpaceById(adSpaceId);
        if (adSpace != null) {
            PublicationDto publication = adSpace.getPublication();
            writer.println(adspaceUrl + " " + DbgUiUtil.adspaceLink(adSpace.getId()) + " , Publication: " + DbgUiUtil.publicationLink(publication.getId()) + " "
                    + publication.getName());
        } else {
            writer.println(adspaceUrl + " " + adSpaceId + "  not in adserver cache");
        }
        writer.println("<br/>");
        return adSpace;
    }

    private static CreativeDto printCreativeLine(PrintWriter writer, Long creativeId, AdserverDomainCache adCache) {
        CreativeDto creative = adCache.getCreativeById(creativeId);
        String creativeUrl = creativeLink(creativeId);
        if (creative != null) {
            CampaignDto campaign = creative.getCampaign();
            writer.println(creativeUrl + " " + DbgUiUtil.creativeLink(creative.getId()) + " " + creative.getName() + ", Campaign: " + DbgUiUtil.campaignLink(campaign.getId())
                    + " " + campaign.getName());
        } else {
            writer.println(creativeUrl + " " + creativeId + "  not in adserver cache");
        }
        writer.println("<br/>");
        return creative;
    }

    public static String adSpaceLink(Long adSpaceId) {
        return "<a href='" + URL_CONTEXT + "/adspace/" + adSpaceId + "'>AdSpace</a>";
    }

    public static String creativeLink(Long creativeId) {
        return "<a href='" + URL_CONTEXT + "/creative/" + creativeId + "'>Creative</a>";
    }
}
