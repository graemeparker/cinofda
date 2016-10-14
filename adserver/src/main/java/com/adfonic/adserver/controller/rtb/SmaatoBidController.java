package com.adfonic.adserver.controller.rtb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.smaato.SmaatoBidRequest;
import com.adfonic.adserver.rtb.smaato.SmaatoBidMapper;
import com.adfonic.adserver.rtb.util.AdServerStats;

/**
 * http://dspportal.smaato.com/documentation
 * 
 */
@Controller
public class SmaatoBidController extends AbstractRTBv2BidController {

    public static ExchangeBidAdapter<SmaatoBidRequest, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<SmaatoBidRequest, BidResponse>(SmaatoBidMapper.instance(),
            SmaatoBidRequest.class, BidResponse.class);

    private RtbBidSequence<SmaatoBidRequest, BidResponse> sequence;

    @Autowired
    public SmaatoBidController(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener backupLoggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, backupLoggingListener, offenceRegistry, counterManager);
        this.sequence = new RtbBidSequence<SmaatoBidRequest, BidResponse>(RtbEndpoint.SmaatoV2, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/smaato/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.SmaatoV2, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

}
