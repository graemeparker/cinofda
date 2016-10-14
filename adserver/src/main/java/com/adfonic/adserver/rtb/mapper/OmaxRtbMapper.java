package com.adfonic.adserver.rtb.mapper;

import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.Imp;
import com.adfonic.adserver.rtb.util.AsCounter;

public class OmaxRtbMapper extends OpenRTBv2ByHandMapper {

    private static final String RTB_ID_PUBLISHER = "Omax";

    private static final OmaxRtbMapper instance = new OmaxRtbMapper();

    public static OmaxRtbMapper instance() {
        return instance;
    }

    private OmaxRtbMapper() {
        // No outsider instances
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = super.mapRtbRequest(publisherExternalId, rtbRequest, listener);
        setExchangeBasedRtbId(RTB_ID_PUBLISHER, rtbRequest, byydRequest);
        return byydRequest;
    }

    @Override
    @Deprecated
    protected ByydImp mapRtbImp(Imp rtbImp, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = super.mapRtbImp(rtbImp, byydRequest);

        // XXX Temporarily here to disable complex ad formats by default 
        if (rtbImp.getVideo() != null && !AdServerFeatureFlag.OMAX_VIDEO.isEnabled()) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AsCounter.OmaxVideo);
        }
        if (rtbImp.getNative() != null && !AdServerFeatureFlag.OMAX_NATIVE.isEnabled()) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AsCounter.OmaxNative);
        }
        return byydImp;
    }

}
