package com.adfonic.adserver.rtb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;

@Component
public class BackupLoggingRtbBidEventListener extends RtbBidEventAdapter {
    private final BackupLogger backupLogger;

    @Autowired
    public BackupLoggingRtbBidEventListener(BackupLogger backupLogger) {
        this.backupLogger = backupLogger;
    }

    @Override
    public void bidRequestRejected(TargetingContext context, ByydRequest bidRequest, String reason) {
        backupLogger.logRtbBidFailure("rejected", context, bidRequest, reason);
    }

    @Override
    public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, String reason) {
        backupLogger.logRtbBidFailure("no bid", context, bidRequest, reason);
    }

    @Override
    public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, Exception exception) {
        backupLogger.logRtbBidFailure("no bid", context, bidRequest, String.valueOf(exception));
    }

    @Override
    public void bidMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, ByydBid bid, Impression impression, SelectedCreative selectedCreative) {
        backupLogger.logRtbBidSuccess(impression, bid.getPrice(), impression.getCreationTime(), context);
    }

    @Override
    public void timeLimitExpired(TargetingContext context, ByydRequest bidRequest, TimeLimit timeLimit) {
        backupLogger.logRtbBidFailure("time limit", context, bidRequest);
    }

    @Override
    public void bidRequestRejected(String publisherExternalID, String bidRequestID, String reason) {
        backupLogger.logRtbBidFailure("rejected", null, null, reason, publisherExternalID);
    }
}
