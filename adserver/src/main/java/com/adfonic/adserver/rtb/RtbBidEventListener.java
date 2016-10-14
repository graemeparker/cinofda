package com.adfonic.adserver.rtb;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;

public interface RtbBidEventListener {
    /**
     * The entire bid request was rejected as a whole even before the mapping of the request was complete
     */
    void bidRequestRejected(String publisherExternalID, String bidRequestID, String reason);

    /**
     * The entire bid request was rejected as a whole, i.e. we didn't even
     * bother looking at each "imp"
     */
    void bidRequestRejected(TargetingContext context, ByydRequest bidRequest, String reason);

    /**
     * We were not able to bid on a given "imp" or while request (imp is null then)
     */
    void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, String reason);

    void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, Exception exception);

    /**
     * We bid on a given "imp"
     */
    void bidMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, ByydBid bid, Impression impression, SelectedCreative selectedCreative);

    /**
     * This gets called if the TimeLimit expires while processing a bid request
     */
    void timeLimitExpired(TargetingContext context, ByydRequest bidRequest, TimeLimit timeLimit);
}
