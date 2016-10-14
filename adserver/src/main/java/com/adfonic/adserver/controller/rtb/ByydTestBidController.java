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
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidRequest;
import com.adfonic.adserver.rtb.util.AdServerStats;

/**
 * This controller was created for Mopub migration to OpenRTB 2.3 It might be useful afterwards
 *  
 * While move to OpenRTB version 2.3 is gradual, breaking change is Mopub's removal of the RTB win notifications and using win notification through beacon.
 * We already support this win notification mode on other exchages so no other code changes 
 * 
 * This class is just a copy of MopubV2BidController with path mapping /rtb/test/bid/{publisherExternalID} and RtbEndpoint.ByydTest
 *  
 * @author mvanek
 *
 */
@Controller
public class ByydTestBidController extends AbstractRTBv2BidController {

    private final RtbBidSequence<MopubBidRequest, BidResponse> sequence;

    @Autowired
    public ByydTestBidController(RtbBidLogic rtbLogic, OffenceRegistry offenceRegistry, RtbFisherman fishnet, BackupLogger backupLogger,
            BackupLoggingRtbBidEventListener loggingListener, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, loggingListener, offenceRegistry, counterManager);

        this.sequence = new RtbBidSequence<MopubBidRequest, BidResponse>(RtbEndpoint.ByydTest, MopubV2BidController.ADAPTER, rtbLogic, backupLogger, loggingListener,
                offenceRegistry, fishnet, counterManager);
    }

    @RequestMapping(value = { "/rtb/test/bid/{publisherExternalID}" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID,// 
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.ByydTest, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }
}
