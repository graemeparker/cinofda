package com.adfonic.adserver.rtb;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;

public class RtbBidEventAdapter implements RtbBidEventListener {
    /** {@inheritDoc} */
    @Override
    public void bidRequestRejected(TargetingContext context, ByydRequest bidRequest, String reason) {
    }

    /** {@inheritDoc} */
    @Override
    public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, String reason) {
    }

    @Override
    public void bidNotMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, Exception exception) {
    }

    /** {@inheritDoc} */
    @Override
    public void bidMade(TargetingContext context, ByydRequest bidRequest, ByydImp imp, ByydBid bid, Impression impression, SelectedCreative selectedCreative) {
    }

    /** {@inheritDoc} */
    @Override
    public void timeLimitExpired(TargetingContext context, ByydRequest bidRequest, TimeLimit timeLimit) {
    }

    /** {@inheritDoc} */
    @Override
    public void bidRequestRejected(String publisherExternalID, String bidRequestID, String reason) {
    }

}
