package com.adfonic.adserver.rtb.mapper;

import java.util.List;

import com.adfonic.adserver.Constant;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.itlookup.OverridingCustomRangeITDeriver;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.open.v2.App;
import com.adfonic.adserver.rtb.open.v2.Site;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBid;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidRequest;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidResponse;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubImp;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubImp.MopubImpExt;
import com.adfonic.domain.Medium;
import com.adfonic.ortb.nativead.NativeAdResponse.NativeAdResponseWrapper;

public class MopubRTBv2Mapper extends OpenRTBv2ByHandMapper {

    private static final MopubRTBv2Mapper instance = new MopubRTBv2Mapper();

    public static MopubRTBv2Mapper instance() {
        return instance;
    }

    private final MopubRTBv1ResponseMapper v1ResponseMapper = new MopubRTBv1ResponseMapper();

    private MopubRTBv2Mapper() {
        super(new MopubRTBv1Mapper());
    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExtId, com.adfonic.adserver.rtb.open.v2.BidRequest<? extends com.adfonic.adserver.rtb.open.v2.Imp> rtbRequest,
            RtbBidEventListener listener) throws NoBidException {

        MopubBidRequest mopubBidRequest = (MopubBidRequest) rtbRequest;
        ByydRequest byydRequest = super.mapRtbRequest(publisherExtId, mopubBidRequest, listener);

        if (byydRequest.getMedium() == Medium.APPLICATION) {
            App app = mopubBidRequest.getApp();
            byydRequest.setPublicationRtbId(app.getPublisher().getId() + "-" + app.getId());
            String pubUrlStr;
            if ((pubUrlStr = app.getStoreurl()) != null) {
                byydRequest.setPublicationUrlString("Store url: " + pubUrlStr);
            } else if ((pubUrlStr = app.getBundle()) != null) {
                byydRequest.setPublicationUrlString("bundle: " + pubUrlStr);
            }
        } else {
            Site site = mopubBidRequest.getSite();
            byydRequest.setPublicationRtbId(site.getPublisher().getId() + "-" + site.getId());
        }

        return byydRequest;
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse byydResponse, ByydRequest byydRequest) {
        MopubBidResponse mopubResponse = new MopubBidResponse();
        v2(v1ResponseMapper.mapRtbResponse(byydResponse, mopubResponse), byydResponse);
        NativeAdResponseWrapper nativeAdResponse = byydResponse.getBid().getNativeAdResponse();
        MopubBid rtbBid = mopubResponse.getSeatbid().get(0).getBid().get(0);
        if (nativeAdResponse != null) {
            // Mopub supports standard OpenRTB way of returning native markup as escaped embedded json in adm field, but this way is more readable
            rtbBid.getExt().setAdmnative(nativeAdResponse);
            rtbBid.setAdm(null); // clear standard location
        } else {
            // Mopub 2.3 special imptrackers field. Clearing and billing is based on this  
            // Note that native Ads have own imptrackers in markup so do not repeat them here
            List<String> impTrackUrls = byydResponse.getImpTrackUrls();
            rtbBid.getExt().setImptrackers(impTrackUrls);
        }
        return mopubResponse;
    }

    @Override
    protected ByydImp mapRtbImp(com.adfonic.adserver.rtb.open.v2.Imp rtbImp, ByydRequest byydRequest) throws NoBidException {
        ByydImp byydImp = super.mapRtbImp(rtbImp, byydRequest);

        MopubImpExt impExt = ((MopubImp) rtbImp).getExt();
        if (impExt != null) {
            // Mopub OpenRTB 2.3 extension
            if (Constant.ONE.equals(impExt.getBrsrclk())) {
                byydImp.setNativeBrowserClick(true);
            }
        }

        if (byydImp.getContentFormWhiteList() != null) {
            String displaymanagerver = rtbImp.getDisplaymanagerver();
            if (displaymanagerver != null) {
                String platformName = getPlatformNameFromDevice(byydRequest.getDevice());
                // temporary suffix to enable blanket blocking for v2 requests alone
                byydImp.setIntegrationTypeDeriver(new OverridingCustomRangeITDeriver(displaymanagerver, platformName != null ? platformName : "default"));
            }
        }

        return byydImp;
    }

}
