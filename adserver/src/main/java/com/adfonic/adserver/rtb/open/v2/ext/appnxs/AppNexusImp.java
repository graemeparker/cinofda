package com.adfonic.adserver.rtb.open.v2.ext.appnxs;

import java.math.BigDecimal;

public class AppNexusImp extends com.adfonic.adserver.rtb.open.v2.Imp {

    private BigDecimal reserve_price;
    
    private static final BigDecimal NEAR_ZERO=new BigDecimal(0.0000001d);

    public BigDecimal getReserve_price() {
        return reserve_price != null ? reserve_price : NEAR_ZERO;
    }

    public void setReserve_price(BigDecimal reserve_price) {
        this.reserve_price = reserve_price;
    }
}