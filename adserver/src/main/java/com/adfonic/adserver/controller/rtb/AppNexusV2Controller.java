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
import com.adfonic.adserver.rtb.mapper.AppNexusV2Mapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBidRequest;
import com.adfonic.adserver.rtb.util.AdServerStats;

@Controller
@SuppressWarnings("rawtypes")
public class AppNexusV2Controller extends AbstractRTBv2BidController {

    private final RtbBidSequence<AppNexusBidRequest, BidResponse> sequence;

    @Autowired
    public AppNexusV2Controller(AppNexusV2Mapper bidMapper, RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener loggingListener,
            OffenceRegistry offenceRegistry, RtbFisherman fishnet, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, loggingListener, offenceRegistry, counterManager);

        OpenRtbV2BidAdapter<AppNexusBidRequest, BidResponse> adapter = new OpenRtbV2BidAdapter<AppNexusBidRequest, BidResponse>(bidMapper, AppNexusBidRequest.class,
                BidResponse.class);
        this.sequence = new RtbBidSequence<AppNexusBidRequest, BidResponse>(RtbEndpoint.AppNexusV2, adapter, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/appnexus/v2/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, @RequestParam(value = "winUrlPath", defaultValue = Constant.WIN_URL_PATH) String winUrlPath, //
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.AppNexusV2, publisherExternalID, httpRequest, httpResponse, winUrlPath);
        sequence.execute(http);
    }

}
