package com.adfonic.adserver.controller.rtb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.mapper.NexageRTBv2Mapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.nexage.NexageBidRequest;
import com.adfonic.adserver.rtb.util.AdServerStats;

@Controller
public class NexageV2Controller extends AbstractRTBv2BidController {

    public static final OpenRtbV2BidAdapter<NexageBidRequest, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<NexageBidRequest, BidResponse>(NexageRTBv2Mapper.instance(),
            NexageBidRequest.class, BidResponse.class);
    private final RtbBidSequence<NexageBidRequest, BidResponse> sequence;

    @Autowired
    public NexageV2Controller(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener backupLoggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, backupLoggingListener, offenceRegistry, counterManager);
        this.sequence = new RtbBidSequence<NexageBidRequest, BidResponse>(RtbEndpoint.NexageV2, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/nexage/v2/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, @RequestParam(value = "winUrlPath", defaultValue = Constant.WIN_URL_PATH) String winUrlPath,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.NexageV2, publisherExternalID, httpRequest, httpResponse, winUrlPath);
        this.sequence.execute(http);
    }

}
