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
import com.adfonic.adserver.rtb.mapper.PubmaticRTBV2Mapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.pubmatic.PubmaticBidRequest;
import com.adfonic.adserver.rtb.util.AdServerStats;

@Controller
@SuppressWarnings("rawtypes")
public class PubmaticRTBv2Controller extends AbstractRTBv2BidController {

    public static final ExchangeBidAdapter<PubmaticBidRequest, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<PubmaticBidRequest, BidResponse>(PubmaticRTBV2Mapper.instance(),
            PubmaticBidRequest.class, BidResponse.class);
    private final RtbBidSequence<PubmaticBidRequest, BidResponse> sequence;

    @Autowired
    public PubmaticRTBv2Controller(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener loggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, loggingListener, offenceRegistry, counterManager);
        this.sequence = new RtbBidSequence<PubmaticBidRequest, BidResponse>(RtbEndpoint.PubmaticV2, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/pubmatic/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.PubmaticV2, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

}
