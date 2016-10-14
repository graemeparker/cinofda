package com.adfonic.adserver;

import java.math.BigDecimal;

public interface ReservePot {
	
    BigDecimal getPriceBoost(BigDecimal bidPriceUSD, long id, BigDecimal maxBidThreshold);

    void deposit(long id, BigDecimal carriedOver);
}
