package com.adfonic.adserver.rtb.open.v2.ext.nexage;

import com.adfonic.adserver.rtb.open.v2.Imp;

public class NexageBidRequest extends com.adfonic.adserver.rtb.open.v2.BidRequest<Imp> {

    private NexageDevice device;

    @Override
    public NexageDevice getDevice() {
        return device;
    }

    public void setDevice(NexageDevice device) {
        this.device = device;
    }

}
