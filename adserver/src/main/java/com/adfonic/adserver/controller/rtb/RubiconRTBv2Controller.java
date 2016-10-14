package com.adfonic.adserver.controller.rtb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.rubicon.RubiconBidRequest;
import com.adfonic.adserver.rtb.rubicon.RubiconRtbMapper;
import com.adfonic.adserver.rtb.rubicon.RubiconRtbNotification;
import com.adfonic.adserver.rtb.rubicon.RubiconRtbNotification.NotificationImpression;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.util.stats.FreqLogr;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * http://kb.rubiconproject.com/index.php/RTB/OpenRTB
 *
 */
@Controller
public class RubiconRTBv2Controller extends AbstractRTBv2BidController {

    public static final ExchangeBidAdapter<RubiconBidRequest, BidResponse> ADAPTER = new OpenRtbV2BidAdapter<RubiconBidRequest, BidResponse>(RubiconRtbMapper.instance(),
            RubiconBidRequest.class, BidResponse.class);

    private final RtbBidSequence<RubiconBidRequest, BidResponse> sequence;

    private final ObjectReader notificationReader = WebConfig.getRtbJsonMapper().readerFor(RubiconRtbNotification.class);

    @Autowired
    private RtbWinLogicImpl rtbLogic;

    @Autowired
    public RubiconRTBv2Controller(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener backupLoggingListener, OffenceRegistry offenceRegistry,
            RtbFisherman fishnet, AdServerStats counterManager) {
        super(rtbLogic, backupLogger, backupLoggingListener, offenceRegistry, counterManager);

        this.sequence = new RtbBidSequence<RubiconBidRequest, BidResponse>(RtbEndpoint.RubiconV2, ADAPTER, rtbLogic, backupLogger, loggingListener, offenceRegistry, fishnet,
                counterManager);
    }

    @RequestMapping(value = "/rtb/rubicon/bid/{publisherExternalID}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public void bid(@PathVariable("publisherExternalID") String publisherExternalID, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        RtbHttpContext http = new RtbHttpContext(RtbEndpoint.RubiconV2, publisherExternalID, httpRequest, httpResponse, Constant.WIN_URL_PATH);
        this.sequence.execute(http);
    }

    /**
     * Documentation
     * http://kb.rubiconproject.com/index.php/RTB/Notifications
     */
    @RequestMapping(value = "/rtb/rubicon/notify", method = RequestMethod.POST, consumes = "application/json")
    public void rtbNotify(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");
        try {
            stats.increment(RtbExchange.Rubicon, AsCounter.LossNotification);

            RubiconRtbNotification notification;
            if (logger.isDebugEnabled()) {
                // Logging support of raw notification request
                ContentStoringReader reader = new ContentStoringReader(httpRequest);
                notification = notificationReader.readValue(reader);
                logger.debug("Loss notification:\n" + reader.getContent());
            } else {
                // Read from http request directly 
                notification = notificationReader.readValue(httpRequest.getInputStream());
            }
            for (NotificationImpression nimp : notification.getImpressions()) {
                String result = nimp.getResult();
                if ("win".equals(result)) {
                    continue; // Ignore. We should not be even subscribed for wins...
                }
                String reasonMessage = nimp.getDetail();
                if (StringUtils.isBlank(reasonMessage)) {
                    reasonMessage = result;
                }
                String impressionExtId = nimp.getToken();
                if (impressionExtId != null) {
                    RtbBidDetails bidDetails = rtbLogic.bidLoss(impressionExtId, reasonMessage); // Call our rtb loss logic (which is normaly timeout triggered)
                    if (bidDetails == null) {
                        // This should happen very rarely
                        stats.increment(RtbExchange.Rubicon, AsCounter.LossNotificationRtbDetailsNotFound);
                        stats.increment(RtbExchange.Rubicon, AsCounter.RtbLoss + "." + reasonMessage);
                    }
                } else {
                    stats.increment(RtbExchange.Rubicon, AsCounter.RtbLoss + "." + reasonMessage);
                }

            }

        } catch (Exception x) {
            FreqLogr.report(x, "Rubicon loss notification");
            stats.increment(RtbExchange.Rubicon, AsCounter.LossNotificationError);
        }
    }
}
