package com.adfonic.adserver.rtb.open.v2.ext.pubmatic;

import com.adfonic.adserver.rtb.open.v2.Imp;

public class PubmaticBidRequest extends com.adfonic.adserver.rtb.open.v2.BidRequest<Imp> {

    private PubmaticDevice device;

    private PubmaticApp app;

    @Override
    public PubmaticApp getApp() {
        return app;
    }

    public void setApp(PubmaticApp app) {
        this.app = app;
    }

    @Override
    public PubmaticDevice getDevice() {
        return device;
    }

    public void setDevice(PubmaticDevice device) {
        this.device = device;
    }

}
