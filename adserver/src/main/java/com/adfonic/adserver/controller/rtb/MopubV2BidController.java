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
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.controller.rtb.OpenRtbV2BidAdapter.ContentStoringReader;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidDetails;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl;
import com.adfonic.adserver.rtb.mapper.MopubRTBv2Mapper;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubRtbNofication;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubRtbNofication.Reason;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.util.stats.FreqLogr;
import com.fasterxml.jackson.databind.ObjectReader;

@Controller
public class MopubV2BidController extends AbstractRTBv2BidController {

    public static final OpenRtbV2BidAdapter<MopubBidRequest, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<MopubBidRequest, BidResponse>(MopubRTBv2Mapper.instance(),
            MopubBidRequest.class, BidResponse.class);
    private final RtbBidSequence<MopubBidRequest, BidResponse> sequence;

    private final ObjectReader notificationReader = WebConfig.getRtbJsonMapper().readerFor(MopubRtbNofication.class);

    @Autowired
    private RtbWinLogicImpl rtbLogic;

    @Autowired
    public MopubV2BidController(RtbBidLogic rtbBidLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener loggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats stats) {
        super(rtbBidLogic, backupLogger, loggingListener, offenceRegistry, stats);

        this.sequence = new RtbBidSequence<MopubBidRequest, BidResponse>(RtbEndpoint.MopubV2, ADAPTER, rtbBidLogic, backupLogger, loggingListener, offenceRegistry, fishnet, stats);
    }

    @RequestMapping(value = { "/rtb/mopub/bid/{publisherExternalID}", "/rtb/mopub/v2/bid/{publisherExternalID}" }, method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void rtbBid(@PathVariable("publisherExternalID") String publisherExternalID,// 
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.MopubV2, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

    /**
     * Documentation
     * https://dev.twitter.com/mopub-demand/marketplace/auction-notifications
     * 
     * Service can be enabled and configured in https://mpx-dashboard.mopub.com/bidders
     */
    @RequestMapping(value = "/rtb/mopub/notify", method = RequestMethod.POST, consumes = "application/json")
    public void rtbNotify(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");
        try {
            stats.increment(RtbExchange.Mopub, AsCounter.LossNotification);

            MopubRtbNofication notification;
            if (logger.isDebugEnabled()) {
                // Logging support of raw notification request
                ContentStoringReader reader = new ContentStoringReader(httpRequest);
                notification = notificationReader.readValue(reader);
                logger.debug("Loss notification:\n" + reader.getContent());
            } else {
                // Read from http request directly 
                notification = notificationReader.readValue(httpRequest.getInputStream());
            }

            Reason reason = notification.getBestReason();
            String reasonMessage = reason != null ? reason.getDescription() : "missing_reason";

            String impressionExtId = notification.getBidid();
            // Notification "bidid" is null when reason is timeout or invalid response
            if (impressionExtId != null) {
                RtbBidDetails bidDetails = rtbLogic.bidLoss(impressionExtId, reasonMessage); // Call our rtb loss logic (which is normaly timeout triggered)
                if (bidDetails == null) {
                    // This should happen very rarely
                    stats.increment(RtbExchange.Mopub, AsCounter.LossNotificationRtbDetailsNotFound);
                    stats.increment(RtbExchange.Mopub, AsCounter.RtbLoss + "." + reasonMessage);
                }
            } else {
                stats.increment(RtbExchange.Mopub, AsCounter.RtbLoss + "." + reasonMessage);
            }
        } catch (Exception x) {
            FreqLogr.report(x, "Mopub loss notification");
            stats.increment(RtbExchange.Mopub, AsCounter.LossNotificationError);
        }
    }
}
