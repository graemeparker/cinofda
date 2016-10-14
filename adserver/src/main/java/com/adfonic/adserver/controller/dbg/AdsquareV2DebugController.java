package com.adfonic.adserver.controller.dbg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.adfonic.adserver.impl.DidEater;
import com.adfonic.adserver.rtb.impl.AdsquareWorker;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.http.ApiClient;
import com.byyd.adsquare.v2.AdsqrEnrichQueryResponse;
import com.byyd.adsquare.v2.AmpAudience;
import com.byyd.adsquare.v2.AmpCompany;
import com.byyd.adsquare.v2.AmpConfiguredClient;
import com.byyd.adsquare.v2.AmpSupplySidePlatform;
import com.byyd.adsquare.v2.EnrichmentApiClient;
import com.byyd.breaker.CircuitTargetTemplate.TargetBreaker;

/**
 * API documentation - http://docs.adsquare.com/ (demo / integration)
 * 
 * @author mvanek
 *
 */
@Controller
@RequestMapping(AdsquareV2DebugController.URL_CONTEXT)
public class AdsquareV2DebugController {

    public static final String URL_CONTEXT = "/adserver/adsquare";

    @Autowired
    private AmpConfiguredClient ampClient;

    @Autowired
    private EnrichmentApiClient enrichClient;

    @Autowired
    private AdserverDomainCacheManager adserverCacheManager;

    @Autowired
    private AdsquareWorker adsquareWorker;

    public void printForm(PrintWriter writer) {
        writer.println("<form method='POST' action='" + URL_CONTEXT + "' accept-charset='UTF-8'>");
        writer.println("Latitude: <input name='latitude' size='15' />");
        writer.println("Longitude: <input name='longitude' size='15' />");
        writer.println("<br/>");
        writer.println("DeviceId: <input name='deviceId' size='44' />");
        writer.println("<br/>");
        DbgUiUtil.printExchangesRadios(writer, adserverCacheManager.getCache(), true, null);
        writer.println("<br/>");
        writer.println("<input type='submit' value='Query'/>");
        writer.println("<a href='" + URL_CONTEXT + "/audiences'>Audiences</a>");
        writer.println("<a href='" + URL_CONTEXT + "/companies'>Companies</a>");
        writer.println("<a href='" + URL_CONTEXT + "/ssps'>Ssps</a>");
        writer.println("</form>");
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "text/html")
    public void formView(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        PrintWriter writer = httpResponse.getWriter();
        writer.println(DbgUiUtil.HTML_OPEN);
        ApiClient apiClient = enrichClient.getApiClient();
        writer.println("Enrichment Connections: " + apiClient.getTotalStats());
        writer.println("<a href='" + URL_CONTEXT + "/reset'>Reset</a><br/>");
        List<TargetBreaker<HttpHost>> targetBreakers = apiClient.getBreakerTemplate().getTargetBreakers();
        writer.print("Enrichment Servers: ");
        for (TargetBreaker<HttpHost> targetBreaker : targetBreakers) {
            HttpHost target = targetBreaker.getTarget();
            int port = target.getPort();
            String statusUrl = target.getSchemeName() + "://" + target.getHostName() + (port != -1 ? ":" + port : "") + "/ping";
            boolean broken = targetBreaker.getBreaker().isBroken();
            writer.print("<a href='" + statusUrl + "'>" + target.getHostName() + (broken ? " DOWN" : "") + "</a> ");
        }
        writer.println("<br/>");
        ThreadPoolExecutor reportingExecutor = adsquareWorker.getReportingExecutor();
        writer.println("Reporting Executor: ActiveCount: " + reportingExecutor.getActiveCount() + ", PoolSize: " + reportingExecutor.getPoolSize() + ", CorePoolSize: "
                + reportingExecutor.getCorePoolSize() + ", MaxPoolSize: " + reportingExecutor.getMaximumPoolSize() + ", QueueSize: " + reportingExecutor.getQueue().size());
        writer.println("<br/>");
        printForm(writer);
        writer.println(DbgUiUtil.HTML_CLOSE);
    }

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public String formPost(@RequestParam(name = "latitude", required = false) Double latitude, @RequestParam(name = "longitude", required = false) Double longitude,//
            @RequestParam(name = "deviceId", required = false) String deviceId, @RequestParam(name = "exchange", required = false) String exchangeIdent) throws IOException {

        String deviceIdRaw = null;
        String deviceIdSha1 = null;
        String deviceIdMd5 = null;
        String deviceType = null;
        Integer sspId = StringUtils.isNotBlank(exchangeIdent) ? AdsquareWorker.getSspId(RtbExchange.lookup(exchangeIdent).getPublisherId()) : null;

        if (StringUtils.isNotBlank(deviceId)) {
            if (deviceId.length() == DidEater.RAW_HEX_LENGTH) {
                deviceIdRaw = deviceId; // should we calculate hashes or raw is enough
            } else if (deviceId.length() == DidEater.SHA1_HEX_LENGTH) {
                deviceIdSha1 = deviceId;
            } else if (deviceId.length() == DidEater.MD5_HEX_LENGTH) {
                deviceIdMd5 = deviceId;
            } else {
                deviceIdRaw = deviceId; // Well, most likely error but try it anyway
            }
            deviceType = "Mobile";
        }

        if (StringUtils.isBlank(deviceId) && latitude == null) {
            return "No input. No query!";
        } else {
            AdsqrEnrichQueryResponse adsquareResponse = enrichClient.query(latitude, longitude, deviceIdRaw, deviceIdSha1, deviceIdMd5, deviceType, sspId);
            return DebugBidController.debugJsonMapper.writeValueAsString(adsquareResponse);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/audiences", method = RequestMethod.GET, produces = "application/json")
    public String audiences(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        List<AmpAudience> audiences = ampClient.audiences();
        return DebugBidController.debugJsonMapper.writeValueAsString(audiences);
    }

    @ResponseBody
    @RequestMapping(value = "/companies", method = RequestMethod.GET, produces = "application/json")
    public String companies(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        List<AmpCompany> companies = ampClient.companies();
        return DebugBidController.debugJsonMapper.writeValueAsString(companies);
    }

    @ResponseBody
    @RequestMapping(value = "/ssps", method = RequestMethod.GET, produces = "application/json")
    public String ssps(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        List<AmpSupplySidePlatform> ssps = ampClient.ssps();
        return DebugBidController.debugJsonMapper.writeValueAsString(ssps);
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET, produces = "text/html")
    public void reset(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        enrichClient.reset();
        httpResponse.sendRedirect(URL_CONTEXT);
    }
}
