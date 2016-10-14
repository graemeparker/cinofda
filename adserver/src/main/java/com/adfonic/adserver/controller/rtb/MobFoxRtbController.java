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
import com.adfonic.adserver.rtb.mapper.mobfox.MobFoxBidRequest;
import com.adfonic.adserver.rtb.mapper.mobfox.MobFoxRtbMapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.util.AdServerStats;

/**
 * MobFox 
 * http://docs.mobfox.com/docs/integrate-an-a-dsp
 * http://www.mobfox.com/dsp-resource-center/
 * 
 * OpenRtb 2.1 Exchange with extensions {@link MobFoxRtbMapper}
 * http://mobfox-rtb.byyd.net/rtb/mobfox/bid/e1b6a4d2-486c-4b9c-b905-6762244bbcdf
 */
@Controller
public class MobFoxRtbController extends AbstractRTBv2BidController {

    public static final ExchangeBidAdapter<MobFoxBidRequest, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<MobFoxBidRequest, BidResponse>(MobFoxRtbMapper.instance(),
            MobFoxBidRequest.class, BidResponse.class);

    private final RtbBidSequence<MobFoxBidRequest, BidResponse> sequence;

    @Autowired
    public MobFoxRtbController(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener backupLoggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats astats) {
        super(rtbLogic, backupLogger, backupLoggingListener, offenceRegistry, astats);

        this.sequence = new RtbBidSequence<MobFoxBidRequest, BidResponse>(RtbEndpoint.MobFox, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet, astats);
    }

    @RequestMapping(value = "/rtb/mobfox/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.MobFox, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

}
