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
import com.adfonic.adserver.rtb.mapper.OmaxRtbMapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.Imp;
import com.adfonic.adserver.rtb.util.AdServerStats;

/**
 * OMAX (Opera Mediaworks)
 * Documentation is attached onto Ticket https://tickets.byyd-tech.com/browse/MAD-3534
 * 
 * Standard OpenRtb 2.3 Exchange with {@link OmaxRtbMapper}
 * http://omax-rtb.byyd.net/rtb/omax/bid/a89c857a-bedb-4780-ac79-a6b72d4c069b
 */
@Controller
public class OmaxRtbController extends AbstractRTBv2BidController {

    public static final ExchangeBidAdapter<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, BidResponse>(
            OmaxRtbMapper.instance(), (Class) BidRequest.class, BidResponse.class);

    private final RtbBidSequence<BidRequest<? extends Imp>, BidResponse> sequence;

    @Autowired
    public OmaxRtbController(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener backupLoggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats astats) {
        super(rtbLogic, backupLogger, backupLoggingListener, offenceRegistry, astats);

        this.sequence = new RtbBidSequence<BidRequest<? extends Imp>, BidResponse>(RtbEndpoint.Omax, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet,
                astats);
    }

    @RequestMapping(value = "/rtb/omax/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.Omax, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

}
