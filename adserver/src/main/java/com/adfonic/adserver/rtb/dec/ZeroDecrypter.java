package com.adfonic.adserver.rtb.dec;

import java.math.BigDecimal;

import com.adfonic.adserver.rtb.PriceDecrypter;

public class ZeroDecrypter implements PriceDecrypter {

    @Override
    public BigDecimal decodePrice(String price) {
        return new BigDecimal(price);
    }

    @Override
    public String encodePrice(BigDecimal price) {
        return price.toPlainString();
    }

}
