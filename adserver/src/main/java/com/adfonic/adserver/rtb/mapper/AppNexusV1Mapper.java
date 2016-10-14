package com.adfonic.adserver.rtb.mapper;

import static com.adfonic.adserver.cst.AppNexusShared.END_POINT_MACRO;
import static com.adfonic.adserver.cst.AppNexusShared.HID_COMM_CLS;
import static com.adfonic.adserver.cst.AppNexusShared.HID_COMM_OPN;
import static com.adfonic.adserver.cst.AppNexusShared.IMPRESSION_ID_MACRO;
import static com.adfonic.adserver.cst.AppNexusShared.SECONDARY_BEACONS_MACRO;

import java.util.List;

import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid;
import com.adfonic.adserver.rtb.open.v2.ext.appnxs.AppNexusBid.CustomMacro;

public class AppNexusV1Mapper extends OpenRTBv1QuickNdirty {

    private String endPointMacro;

    public AppNexusV1Mapper(String endPointMacro) {
        this.endPointMacro = endPointMacro;
    }

    @Override
    protected com.adfonic.adserver.rtb.open.v1.Bid buildBid(ByydBid byydBid) {
        AppNexusBid rtbBid = new AppNexusBid();
        List<CustomMacro> customMacros = rtbBid.getExt().getAppnexus().getCustom_macros();
        customMacros.add(new CustomMacro(IMPRESSION_ID_MACRO, byydBid.getAdid()));
        customMacros.add(new CustomMacro(SECONDARY_BEACONS_MACRO, HID_COMM_CLS + byydBid.getAdm() + HID_COMM_OPN));

        //END_POINT_MARCRO form AppNexusShared shared?
        customMacros.add(new CustomMacro(END_POINT_MACRO, endPointMacro));

        super.mapBid(byydBid, rtbBid);

        rtbBid.setAdm(null);
        rtbBid.setIurl(null);
        rtbBid.setAdid(byydBid.getPublisherCreativeId());
        return rtbBid;

    }

    @Override
    public ByydRequest mapRtbRequest(String publisherExternalId, com.adfonic.adserver.rtb.open.v1.BidRequest bidRequest, RtbBidEventListener listener) throws NoBidException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public com.adfonic.adserver.rtb.open.v1.BidResponse mapRtbResponse(ByydResponse nativeResponse, ByydRequest byydRequest) {
        throw new UnsupportedOperationException();
    }
}
