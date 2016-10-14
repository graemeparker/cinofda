package com.adfonic.adserver.controller.rtb;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.logging.LoggingUtils;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl;
import com.adfonic.adserver.rtb.mapper.OpenRTBv1QuickNdirty;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.util.stats.FreqLogr;

@Controller
@SuppressWarnings("rawtypes")
public class OpenRtbV1Controller {

    private static final transient Logger LOG = Logger.getLogger(OpenRtbV1Controller.class.getName());

    private static final OpenRTBv1QuickNdirty bidMapper = OpenRTBv1QuickNdirty.getInstance();
    public static final OpenRtbV1BidAdapter ADAPTER = new OpenRtbV1BidAdapter();

    private final RtbBidLogic rtbLogic;
    private final BackupLoggingRtbBidEventListener loggingListener;
    private final BackupLogger backupLogger;
    private final RtbWinLogicImpl rtbWinLogic;

    private final RtbBidSequence<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse> sequence;

    @Autowired
    public OpenRtbV1Controller(RtbBidLogic rtbLogic, RtbWinLogicImpl rtbWinLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener loggingListener,
            OffenceRegistry offenceRegistry, RtbFisherman fishnet, AdServerStats counterManager) {
        this.rtbLogic = rtbLogic;
        this.rtbWinLogic = rtbWinLogic;
        this.backupLogger = backupLogger;
        this.loggingListener = loggingListener;

        this.sequence = new RtbBidSequence<com.adfonic.adserver.rtb.open.v1.BidRequest, com.adfonic.adserver.rtb.open.v1.BidResponse>(RtbEndpoint.ORTBv1, ADAPTER, rtbLogic,
                backupLogger, loggingListener, offenceRegistry, fishnet, counterManager);
    }

    @RequestMapping(value = "/rtb/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID,// 
            @RequestParam(value = "winUrlPath", defaultValue = Constant.WIN_URL_PATH) String winUrlPath,//
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws NoBidException {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.ORTBv1, publisherExternalID, httpRequest, httpResponse, winUrlPath);
        this.sequence.execute(http);
    }

    /**
     * Handle an RTB win notice.  An RTB exchange is notifying us that our bid won.
     * We use the "Ad Served on the Win Notice" method of delivering the ad, which
     * means now is the time to write the ad content to the response.
     * @param httpRequest
     * @param httpResponse
     * @param impressionExternalID the key we use to look up the respective RtbBidDetails in cache
     * @param settlementPrice the actual settlement price for the win
     * @throws java.io.IOException
     */
    @RequestMapping(value = Constant.WIN_URL_PATH + "/{impressionExternalID}")
    public void handleWinNotice(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathVariable String impressionExternalID,
            @RequestParam(value = Constant.SP_URL_PARAM, required = false) String settlementPrice) throws java.io.IOException {
        if (LOG.isLoggable(Level.FINE)) {
            //LOG.fine("Win notice for impressionExternalID=" + impressionExternalID + ", settlementPrice=" + settlementPrice);
            LoggingUtils.log(LOG, Level.FINE, null, null, this.getClass(), "handleWinNotice", "Win notice for impressionExternalID=" + impressionExternalID + ", settlementPrice="
                    + settlementPrice);
        }

        String[] rendered = rtbWinLogic.winOnRtbNurl(impressionExternalID, settlementPrice, httpRequest);
        if (rendered == null) {
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");

        String contentType = rendered[1] + "; charset=utf-8";
        httpResponse.setContentType(contentType);

        if (contentType.startsWith(Constant.APPL_XML)) {
            addCorsHeaders(httpRequest, httpResponse);
        }
        httpResponse.getWriter().write(rendered[0]);
    }

    /**
     * This is only tiny extension that smuggles exchange name into win notification url so it can be easily find in access log.
     * Otherwise it works exactly same as original win notification handler
     */
    @RequestMapping(value = Constant.WIN_URL_PATH + "/{exchange}/{impressionExternalID}")
    public void handleWinNoticeExchange(HttpServletRequest httpRequest, HttpServletResponse httpResponse, @PathVariable String impressionExternalID,
            @RequestParam(value = Constant.SP_URL_PARAM, required = false) String settlementPrice) throws java.io.IOException {
        // Just call original handler
        handleWinNotice(httpRequest, httpResponse, impressionExternalID, settlementPrice);
    }

    /**
     * Rubicon VAST player requires CORS headers - http://kb.rubiconproject.com/index.php/RTB/VideoRTBBestPractices
     */
    public static void addCorsHeaders(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String origin = httpRequest.getHeader("Origin");
        if (StringUtils.isBlank(origin)) {
            origin = "*"; // Star is actually illegal value when Access-Control-Allow-Credentials: true  
        }
        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Headers", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET");
        httpResponse.setHeader("Access-Control-Max-Age", "10080");
    }

    @ExceptionHandler(NoBidException.class)
    @ResponseStatus(HttpStatus.OK)
    public void noBid(NoBidException nobid, HttpServletRequest request, Writer writer, HttpServletResponse response) throws IOException {
        response.setContentType(Constant.APPL_JSON_UTF8);
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        writer.write("{\"id\":\"" + nobid.getByydRequest().getId() + "\",\"nbr\":" + nobid.getNoBidReason().getV1nbr() + "}");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    public void handleBadRequests(Exception x, HttpServletRequest request, Writer writer, HttpServletResponse response) throws IOException {
        //LoggingUtils.logUnexpectedError(LOG, e, null);
        FreqLogr.report(x);
        response.setContentType(Constant.APPL_JSON_UTF8);
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        writer.write("{}");//empty response indicates no bid
    }

}
