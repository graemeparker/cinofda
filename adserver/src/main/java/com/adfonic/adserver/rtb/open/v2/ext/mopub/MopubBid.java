package com.adfonic.adserver.rtb.open.v2.ext.mopub;

public class MopubBid extends com.adfonic.adserver.rtb.open.v2.Bid {

    private MopubBidExt ext;

    @Deprecated
    private String crtype; // Mopub OpenRtb 2.1. Moved to seatbid.bid.ext.crtype in 2.3

    public MopubBidExt getExt() {
        return ext != null ? ext : (ext = new MopubBidExt());
    }

    public void setExt(MopubBidExt ext) {
        this.ext = ext;
    }

    @Deprecated
    public String getCrtype() {
        return crtype;
    }

    @Deprecated
    public void setCrtype(String crtype) {
        this.crtype = crtype;
    }

}
