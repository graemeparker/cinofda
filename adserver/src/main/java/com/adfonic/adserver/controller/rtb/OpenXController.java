package com.adfonic.adserver.controller.rtb;

import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.openx.OpenX;
import com.adfonic.adserver.rtb.openx.OpenX.BidResponse;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.util.stats.FreqLogr;

@Controller
public class OpenXController {

    private static final transient Logger LOG = Logger.getLogger(OpenXController.class.getName());

    private static final int DEFAULT_API_VERSION = 7;

    public static final OpenXBidAdapter ADAPTER = new OpenXBidAdapter();

    private final RtbBidSequence<OpenX.BidRequest, OpenX.BidResponse> sequence;

    @Autowired
    public OpenXController(RtbBidLogic rtbLogic, BackupLoggingRtbBidEventListener biddingListener, BackupLogger backupLogger, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats counterManager) {
        this.sequence = new RtbBidSequence<OpenX.BidRequest, OpenX.BidResponse>(RtbEndpoint.OpenX, ADAPTER, rtbLogic, backupLogger, biddingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/openx/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/octet-stream", produces = "application/octet-stream")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID,//
            @RequestParam(value = "winUrlPath", defaultValue = Constant.WIN_URL_PATH) String winUrlPath,// 
            @RequestParam(value = "tmax", required = false) Long tmax,// 
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.OpenX, publisherExternalID, httpRequest, httpResponse, winUrlPath);
        this.sequence.execute(http);
    }

    /**
     * Probably a bad request which could not be mapped
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BidResponse handleUnexpectedRequests(Exception x, HttpServletResponse response) {
        //LoggingUtils.logUnexpectedError(LOG, e, "Unexpected exception in OpenX RTB");
        FreqLogr.report(x);
        response.setContentType("application/octet-stream");
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        return BidResponse.newBuilder().setApiVersion(DEFAULT_API_VERSION).setAuctionId(UUID.randomUUID().toString()).build();
    }
}
