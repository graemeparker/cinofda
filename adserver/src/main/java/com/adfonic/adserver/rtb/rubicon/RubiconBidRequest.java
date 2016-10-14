package com.adfonic.adserver.rtb.rubicon;

import java.util.List;

public class RubiconBidRequest extends com.adfonic.adserver.rtb.open.v2.BidRequest<RubiconImp> {

    private List<RubiconImp> imp;

    @Override
    public List<RubiconImp> getImp() {
        return imp;
    }

    @Override
    public void setImp(List<RubiconImp> imp) {
        this.imp = imp;
    }

}
