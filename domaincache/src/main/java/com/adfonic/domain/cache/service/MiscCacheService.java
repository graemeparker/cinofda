package com.adfonic.domain.cache.service;

import java.math.BigDecimal;

public interface MiscCacheService extends BaseCache {

    void cachePayout(long publisherId, long campaignId, BigDecimal payout);

    BigDecimal getPayout(long publisherId, long campaignId);

    void trimForNonRtbMode();

}
