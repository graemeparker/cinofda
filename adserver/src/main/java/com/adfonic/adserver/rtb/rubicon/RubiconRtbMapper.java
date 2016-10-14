package com.adfonic.adserver.rtb.rubicon;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.mapper.OpenRTBv1QuickNdirty;
import com.adfonic.adserver.rtb.mapper.OpenRTBv2ByHandMapper;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.open.v1.Bid;
import com.adfonic.adserver.rtb.open.v1.BidResponse;
import com.adfonic.adserver.rtb.open.v1.SeatBid;
import com.adfonic.adserver.rtb.open.v2.VideoV2;
import com.adfonic.adserver.rtb.rubicon.RubiconBidReponse.RubiconBid;
import com.adfonic.adserver.rtb.rubicon.RubiconBidReponse.RubiconBidExt;
import com.adfonic.adserver.rtb.rubicon.RubiconVideo.RubiconVideoExt;

public class RubiconRtbMapper extends OpenRTBv2ByHandMapper {

    private static final RubiconRtbMapper instance = new RubiconRtbMapper();

    private RubiconRtbMapper() {
        super(new RubiconResponseMapper());
    }

    public static RubiconRtbMapper instance() {
        return instance;
    }

    @Override
    protected void mapVideoImp(VideoV2 rtbVideo, ByydImp byydImp, ByydRequest byydRequest) throws NoBidException {
        // Perform standard mapping first
        super.mapVideoImp(rtbVideo, byydImp, byydRequest);
        /**
         * Most common Rubicon video request is 320x568 or 568x320 which is ratio 12:9 
         * and it is default full screen size of the iPhone 5 but they use it for Android device requests too
         *   
         * Our platform supported formats are 320x480 (ratio 3:2) or 768x1024 (ratio 4:3)
         * Let's hack it a bit and override rudiculous value to normal
         */
        // Beware that few request have no w or h (Rubicon in iad3 shard) 
        Integer w = rtbVideo.getW();
        Integer h = rtbVideo.getH();
        if (w == null || h == null) {
            // Maybe we can bid with that somehow...
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_FIELD, "imp.w/h");
        } else if (h == 568 && w == 320) {
            byydImp.setH(480);
        } else if (w == 568 && h == 320) {
            byydImp.setW(480);
        }

        RubiconVideoExt ext = ((RubiconVideo) rtbVideo).getExt();
        if (ext != null) {
            //TODO use skipability for targeting
            boolean skippable = Constant.ONE.equals(ext.getSkip());
            Integer skipDelay = ext.getSkipDelay();
        }
    }

    @Override
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse byydResponse, ByydRequest byydRequest) {
        RubiconBidReponse rubiconResponse = new RubiconBidReponse();
        BidResponse<SeatBid<Bid>> x = v1mapper.mapRtbResponse(byydResponse, rubiconResponse);
        v2(x, byydResponse);
        return rubiconResponse;
    }

    static class RubiconResponseMapper extends OpenRTBv1QuickNdirty {

        @Override
        protected Bid buildBid(ByydBid byydBid) {
            RubiconBid rubiconBid = new RubiconBid();
            mapBid(byydBid, rubiconBid);
            RubiconBidExt ext = new RubiconBidExt();
            ext.setNt(byydBid.getAdid()); // Use Impression Id as pairing id for loss notification service
            rubiconBid.setExt(ext);
            return rubiconBid;
        }
    }
}
