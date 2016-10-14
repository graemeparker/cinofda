package com.adfonic.domain.cache.service;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MiscCacheServiceImpl implements MiscCacheService {

    private static final long serialVersionUID = 1L;

    public MiscCacheServiceImpl() {

    }

    public MiscCacheServiceImpl(MiscCacheServiceImpl copy) {
        this.payoutByPublisherIdAndCampaignId.putAll(copy.payoutByPublisherIdAndCampaignId);
    }

    final ConcurrentHashMap<Long, ConcurrentHashMap<Long, BigDecimal>> payoutByPublisherIdAndCampaignId = new ConcurrentHashMap<Long, ConcurrentHashMap<Long, BigDecimal>>();

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "Total payoutByPublisherIdAndCampaignId = " + this.payoutByPublisherIdAndCampaignId.size());
        }

    }

    @Override
    public void cachePayout(long publisherId, long campaignId, BigDecimal payout) {
        ConcurrentHashMap<Long, BigDecimal> byCampaignId = payoutByPublisherIdAndCampaignId.get(publisherId);
        if (byCampaignId == null) {
            byCampaignId = new ConcurrentHashMap<Long, BigDecimal>();
            ConcurrentHashMap<Long, BigDecimal> prevValue = payoutByPublisherIdAndCampaignId.putIfAbsent(publisherId, byCampaignId);
            if (prevValue != null) {
                byCampaignId = prevValue;
            }
        }
        byCampaignId.putIfAbsent(campaignId, payout);
    }

    @Override
    public BigDecimal getPayout(long publisherId, long campaignId) {
        ConcurrentHashMap<Long, BigDecimal> byCampaignId = payoutByPublisherIdAndCampaignId.get(publisherId);
        return byCampaignId == null ? null : byCampaignId.get(campaignId);
    }

    @Override
    public void trimForNonRtbMode() {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterDeserialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeSerialization() {
        // TODO Auto-generated method stub

    }

}
