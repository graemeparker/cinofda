package com.adfonic.adserver.rtb.mapper;

import com.adfonic.adserver.rtb.nativ.ByydBid;

public class NexageV1Mapper extends OpenRTBv1QuickNdirty {

    @Override
    protected com.adfonic.adserver.rtb.open.v1.Bid buildBid(ByydBid from) {
        com.adfonic.adserver.rtb.open.v1.Bid to = super.buildBid(from);
        String txtIurl = from.getTxtIUrl();
        if (txtIurl != null && to.getIurl() == null) {
            to.setIurl(txtIurl);
        }
        return to;
    }

}
