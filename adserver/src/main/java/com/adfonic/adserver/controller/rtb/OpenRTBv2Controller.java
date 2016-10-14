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
import com.adfonic.adserver.rtb.mapper.OpenRTBv2ByHandMapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.util.AdServerStats;

@Controller
public class OpenRTBv2Controller extends AbstractRTBv2BidController {

    //private final BidRequestReaper reaper = new BidRequestReaper(100, 1000);

    public static final ExchangeBidAdapter<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, BidResponse>(
            OpenRTBv2ByHandMapper.instance(), (Class) BidRequest.class, BidResponse.class);

    private final RtbBidSequence<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, BidResponse> sequence;

    @Autowired
    public OpenRTBv2Controller(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener loggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, loggingListener, offenceRegistry, counterManager);
        this.sequence = new RtbBidSequence<com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp>, BidResponse>(RtbEndpoint.ORTBv2, ADAPTER,
                rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet, counterManager);
    }

    @RequestMapping(value = "/rtb/v2/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID,// 
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.ORTBv2, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);

        this.sequence.execute(http);
        //reaper.execute(sequence, http);
    }

}
