package com.adfonic.adserver.rtb.mapper;

import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.SiteOrApp;
import com.adfonic.adserver.rtb.open.v2.ext.nexage.NexageDevice;
import com.adfonic.domain.Medium;

public class NexageRTBv2Mapper extends OpenRTBv2ByHandMapper {

    private static final NexageRTBv2Mapper instance = new NexageRTBv2Mapper();

    public static NexageRTBv2Mapper instance() {
        return instance;
    }

    private NexageRTBv2Mapper() {
        //no outside made instances
        super(new NexageV1Mapper());
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> bidRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = super.mapRtbRequest(publisherExternalId, bidRequest, listener);

        SiteOrApp pub = byydRequest.getMedium() == Medium.APPLICATION ? bidRequest.getApp() : bidRequest.getSite();
        byydRequest.setPublicationRtbId(pub.getPublisher().getId() + "-" + pub.getId());

        return byydRequest;
    }

    @Override
    protected ByydDevice mapRtbDevice(com.adfonic.adserver.rtb.open.v2.Device rtbDevice, BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {
        ByydDevice byydDevice = super.mapRtbDevice(rtbDevice, rtbRequest, byydRequest);
        NexageDevice.DeviceExt deviceExt = ((NexageDevice) rtbDevice).getExt();
        if (deviceExt != null) {
            // Common - iOS and Android
            OpenRTBv2ByHandMapper.setIfaOrAdid(byydDevice, rtbDevice.getOs(), deviceExt.getNex_ifa());
        }
        // Also small amount of Android devices whith device.dpidmd5

        return byydDevice;
    }
}
