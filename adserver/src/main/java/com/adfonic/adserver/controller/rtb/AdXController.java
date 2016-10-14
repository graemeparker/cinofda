package com.adfonic.adserver.controller.rtb;

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
import com.adfonic.adserver.rtb.adx.AdX;
import com.adfonic.adserver.rtb.adx.AdX.BidResponse;
import com.adfonic.adserver.rtb.mapper.AdXMapper;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.util.stats.FreqLogr;

@Controller
public class AdXController {

    private static final transient Logger LOG = Logger.getLogger(AdXController.class.getName());

    private static final int DUMMY_MINIMALPROC_TIME = 1;

    private final RtbBidSequence<AdX.BidRequest, AdX.BidResponse> sequence;

    @Autowired
    public AdXController(AdXMapper mapper, RtbBidLogic rtbLogic, OffenceRegistry offenceRegistry, RtbFisherman fishnet, BackupLoggingRtbBidEventListener biddingListener,
            BackupLogger backupLogger, AdServerStats counterManager) {
        AdXBidAdapter adapter = new AdXBidAdapter(mapper);
        this.sequence = new RtbBidSequence<AdX.BidRequest, AdX.BidResponse>(RtbEndpoint.DcAdX, adapter, rtbLogic, backupLogger, biddingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/dcadx/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/octet-stream", produces = "application/octet-stream")
    public void handleBidRequest(@PathVariable("publisherExternalID") String publisherExternalID,
            @RequestParam(value = "winUrlPath", defaultValue = Constant.WIN_URL_PATH) String winUrlPath, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.DcAdX, publisherExternalID, httpRequest, httpResponse, winUrlPath);
        this.sequence.execute(http);
    }

    /**
     * Probably a bad request which could not be mapped
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public BidResponse handleUnexpectedRequests(Exception x, HttpServletResponse response) {
        //LoggingUtils.logUnexpectedError(LOG, x, "AdX unexpected");
        FreqLogr.report(x);
        response.setContentType("application/octet-stream");
        response.setHeader("Expires", "0");
        response.setHeader("Pragma", "No-Cache");
        return BidResponse.newBuilder().setProcessingTimeMs(DUMMY_MINIMALPROC_TIME).build();
    }

}
