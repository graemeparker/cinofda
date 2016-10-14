package com.adfonic.adserver.rtb.mapper;

import java.util.HashSet;
import java.util.Set;

import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.AdObject;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.AdmJson;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBid;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubBidExt.Video;
import com.adfonic.adserver.rtb.open.v2.ext.mopub.MopubCrtype;
import com.adfonic.domain.ContentForm;
import com.byyd.ortb.CreativeAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MopubRTBv1ResponseMapper extends OpenRTBv1QuickNdirty {

    // ObjectMapper IS thread safe you ThreadLocal freak!
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected com.adfonic.adserver.rtb.open.v1.Bid buildBid(ByydBid byydBid) {
        MopubBid mopubBid = new MopubBid();
        super.mapBid(byydBid, mopubBid);
        if (byydBid.getDealId() != null) {
            mopubBid.getExt().setDeal_id(byydBid.getDealId());
        }

        if (byydBid.getExt() != null) {
            try {
                // this is actualy native bid response...
                mopubBid.getExt().setAdmjson(mapper.readValue(byydBid.getExt(), AdmJson.class));
            } catch (Exception e) {
                // TODO add counter
            }
        }
        if (mopubBid.getIurl() == null) {
            mopubBid.setIurl(byydBid.getTxtIUrl());
        }

        AdObject adObject = byydBid.getImp().getAdObject();
        if (adObject == AdObject.NATIVE) {
            // MAD-1823 
            // http://mopubcom.c.presscdn.com/wp-content/uploads/2014/04/MoPub_Native_Ads_Addendum.pdf
            //    - crtype: recommended usage, string type, value = 'native'. Please code creative type to “native” for native ads. Note this is a MoPub extension of OpenRTB
            //    - adm: recommended usage, string type , value = empty string. Pass as empty string for native ads (some exchanges may choose to allow omission of this parameter)
            mopubBid.setCrtype(MopubCrtype.NATIVE.getCode());
            mopubBid.getExt().setCrtype(MopubCrtype.NATIVE.getCode());
            if (mopubBid.getAdm() == null) {
                mopubBid.setAdm("");
            }
        } else if (adObject == AdObject.VIDEO) {
            // https://dev.twitter.com/mopub-demand/ad-formats/video-best-practices#bidResponseBestPractices
            mopubBid.setCrtype(MopubCrtype.VAST_20.getCode());
            mopubBid.getExt().setCrtype(MopubCrtype.VAST_20.getCode());
            Set<Integer> attributes = mopubBid.getAttr();
            if (attributes == null) {
                attributes = new HashSet<Integer>();
                mopubBid.setAttr(attributes);
            }
            attributes.add(CreativeAttribute.IN_BANNER_VIDEO_AD_AUTO_PLAY.ordinal());
            mopubBid.getExt().setVideo(new Video(byydBid.getDuration()));
            mopubBid.getExt().setDuration(byydBid.getDuration());

        } else {
            // Assume it's banner then
            ContentForm contentForm = byydBid.getContentForm();
            if (contentForm == ContentForm.MRAID_1_0) {
                mopubBid.setCrtype(MopubCrtype.MRAID_10.getCode());
                mopubBid.getExt().setCrtype(MopubCrtype.MRAID_10.getCode());
            }
        }

        return mopubBid;
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse nativeResponse, com.adfonic.adserver.rtb.open.v1.BidResponse bidResponse) {
        return super.mapRtbResponse(nativeResponse, bidResponse);

    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v1.BidRequest bidRequest, RtbBidEventListener listener) throws NoBidException {
        throw new UnsupportedOperationException();
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse nativeResponse, ByydRequest byydRequest) {
        throw new UnsupportedOperationException();
    }
}
