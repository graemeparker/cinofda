package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import java.util.List;

/**
 * https://docs.google.com/document/d/1DkoL7L9GdQQRt4RDUR0wwCp4eSIqIDWqGowJDzhVFVg/view#
 *
 */
public class MopubBidRequest extends com.adfonic.adserver.rtb.open.v2.BidRequest<MopubImp> {

    private List<MopubImp> imp;

    @Override
    public List<MopubImp> getImp() {
        return imp;
    }

    @Override
    public void setImp(List<MopubImp> imp) {
        this.imp = imp;
    }

}
