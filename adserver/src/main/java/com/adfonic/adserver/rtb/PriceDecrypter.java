package com.adfonic.adserver.rtb;

import java.io.Serializable;
import java.math.BigDecimal;

public interface PriceDecrypter extends Serializable {

    BigDecimal decodePrice(String encodedPrice);

    String encodePrice(BigDecimal price);
}
