package com.adfonic.adserver.rtb.mapper;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.ByydDevice;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.open.v2.BidRequest;
import com.adfonic.adserver.rtb.open.v2.SiteOrApp;
import com.adfonic.adserver.rtb.open.v2.ext.pubmatic.PubmaticApp;
import com.adfonic.adserver.rtb.open.v2.ext.pubmatic.PubmaticDevice;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.Medium;

public class PubmaticRTBV2Mapper extends OpenRTBv2ByHandMapper {

    private static final String FALLBACK_PUBLISHER_ID = "PUBM";

    private static final PubmaticRTBV2Mapper instance = new PubmaticRTBV2Mapper();

    public static PubmaticRTBV2Mapper instance() {
        return instance;
    }

    private PubmaticRTBV2Mapper() {
        //no outside made instances
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        ByydRequest byydRequest = super.mapRtbRequest(publisherExternalId, rtbRequest, listener);
        SiteOrApp publication = byydRequest.getMedium() == Medium.APPLICATION ? rtbRequest.getApp() : rtbRequest.getSite();
        String publicationId = publication.getId();
        if (publicationId == null) {
            if (byydRequest.getMedium() == Medium.APPLICATION) {
                PubmaticApp app = (PubmaticApp) rtbRequest.getApp();
                publicationId = app.getExt().getPmid();
            } else {
                // Pubmatic has only Apps - but in the future... who knows
                throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_PUBL_ID);
            }
        }

        String publisherId = null;
        if (publication.getPublisher() != null) {
            publisherId = publication.getPublisher().getId();
        }
        if (publisherId == null) {
            publisherId = FALLBACK_PUBLISHER_ID;
        }

        byydRequest.setPublicationRtbId(publisherId + "-" + publicationId);
        return byydRequest;
    }

    @Override
    protected ByydDevice mapRtbDevice(com.adfonic.adserver.rtb.open.v2.Device rtbDevice, BidRequest rtbRequest, ByydRequest byydRequest) throws NoBidException {

        ByydDevice byydDevice = super.mapRtbDevice(rtbDevice, rtbRequest, byydRequest);
        PubmaticDevice.DeviceExt deviceExt = ((PubmaticDevice) rtbDevice).getExt();
        //https://developer.pubmatic.com/documentation/device-id-parameter
        if (deviceExt != null) {
            if (deviceExt.getIdfa() != null) {
                //iOS - common
                OpenRTBv2ByHandMapper.setIfaOrAdid(byydDevice, rtbDevice.getOs(), deviceExt.getIdfa());
            }
            if (deviceExt.getOtherdeviceid() != null) {
                //Android - common
                OpenRTBv2ByHandMapper.setIfaOrAdid(byydDevice, rtbDevice.getOs(), deviceExt.getOtherdeviceid());
            }

            if (deviceExt.getAndroidadvid() != null) {
                byydDevice.setDeviceIdentifier(DeviceIdentifierType.SYSTEM_NAME_ADID, deviceExt.getAndroidadvid());
            }
        }

        return byydDevice;
    }
}
