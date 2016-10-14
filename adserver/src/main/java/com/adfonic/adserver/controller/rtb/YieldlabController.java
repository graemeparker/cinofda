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
import com.adfonic.adserver.controller.WebConfig;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.yieldlab.YieldLabMapper;
import com.adfonic.adserver.rtb.yieldlab.YieldlabBidAdapter;
import com.adfonic.adserver.rtb.yieldlab.YieldlabBidResponse;

@Controller
public class YieldlabController {

    public static final YieldlabBidAdapter ADAPTER = new YieldlabBidAdapter(WebConfig.getRtbJsonMapper(), new YieldLabMapper());

    private final RtbBidSequence<HttpServletRequest, YieldlabBidResponse> sequence;

    @Autowired
    public YieldlabController(RtbBidLogic rtbLogic, OffenceRegistry offenceRegistry, RtbFisherman fishnet, BackupLogger backupLogger,
            BackupLoggingRtbBidEventListener loggingListener, AdServerStats counterManager) {

        this.sequence = new RtbBidSequence<HttpServletRequest, YieldlabBidResponse>(RtbEndpoint.YieldLab, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry,
                fishnet, counterManager);

    }

    @RequestMapping(value = "/rtb/yieldlab/bid/{publisherExternalID}", method = RequestMethod.GET, produces = "application/json")
    public void bid(@PathVariable String publisherExternalID,// 
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.YieldLab, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

}
