package com.adfonic.adserver.rtb.util;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Impression;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.util.stats.CounterManager;

@Component
public class AdServerStats {

    private final CounterManager counterManager;

    private final AdserverDomainCacheManager adCacheManager;

    private RtbStats rtbStats = RtbStats.i();

    @Autowired
    public AdServerStats(CounterManager counterManager, AdserverDomainCacheManager adCacheManager) {
        Objects.requireNonNull(counterManager);
        this.counterManager = counterManager;
        Objects.requireNonNull(adCacheManager);
        this.adCacheManager = adCacheManager;
    }

    public void bid(AdSpaceDto adSpace, CreativeDto creative, BigDecimal price) {
        rtbStats.bid(creative.getId(), adSpace.getId(), price);
        increment(adSpace.getPublication().getPublisher().getId(), AsCounter.BidDeduced);
    }

    public void bidError(AdSpaceDto adSpace, CreativeDto creative, Exception x) {
        if (creative != null) {
            rtbStats.bidError(creative.getId(), adSpace.getId(), x);
        }
        Long publisherId = adSpace.getPublication().getPublisher().getId();
        increment(publisherId, AsCounter.BidError);
        increment(publisherId, String.valueOf(x.getClass().getName()));
    }

    public void loss(Impression impression, String reason) {
        Long adSpaceId = impression.getAdSpaceId();
        Long creativeId = impression.getCreativeId();
        rtbStats.loss(creativeId, adSpaceId, reason);

        AdserverDomainCache adCache = adCacheManager.getCache();
        AdSpaceDto adSpace = adCache.getAdSpaceById(adSpaceId);
        Long publisherId;
        if (adSpace != null) {
            publisherId = adSpace.getPublication().getPublisher().getId();
        } else {
            publisherId = RtbExchange.Unknown.getPublisherId();
        }

        increment(publisherId, AsCounter.RtbLoss + "." + reason);
    }

    public void winStarted(AdSpaceDto adSpace, CreativeDto creative, AsCounter counter) {
        increment(adSpace.getPublication().getPublisher().getId(), counter);
    }

    public void winCompleted(AdSpaceDto adSpace, CreativeDto creative, BigDecimal settlementPrice) {
        rtbStats.win(creative.getId(), adSpace.getId(), null);
        increment(adSpace.getPublication().getPublisher().getId(), AsCounter.WinCompleted);
    }

    public void beacon(Impression impression) {
        AdserverDomainCache adCache = adCacheManager.getCache();
        AdSpaceDto adSpace = adCache.getAdSpaceById(impression.getAdSpaceId());
        if (adSpace != null) {
            PublisherDto publisher = adSpace.getPublication().getPublisher();
            increment(publisher.getId(), AsCounter.BeaconWithImpression);
        } else {
            increment(RtbExchange.Unknown, AsCounter.BeaconWithImpression);
        }
    }

    public void beaconCompleted(AdSpaceDto adSpace, CreativeDto creative) {
        rtbStats.impression(creative.getId(), adSpace.getId());
        PublisherDto publisher = adSpace.getPublication().getPublisher();
        increment(publisher.getId(), AsCounter.BeaconCompleted);
    }

    public void click(Impression impression) {
        AdserverDomainCache adCache = adCacheManager.getCache();
        AdSpaceDto adSpace = adCache.getAdSpaceById(impression.getAdSpaceId());
        if (adSpace != null) {
            PublisherDto publisher = adSpace.getPublication().getPublisher();
            increment(publisher.getId(), AsCounter.ClickWithImpression);
        } else {
            increment(RtbExchange.Unknown, AsCounter.ClickWithImpression);
        }
    }

    public void clickCompleted(Long adSpaceId, Long creativeId) {
        rtbStats.click(creativeId, adSpaceId);

        AdserverDomainCache adCache = adCacheManager.getCache();
        AdSpaceDto adSpace = adCache.getAdSpaceById(adSpaceId);
        if (adSpace != null) {
            PublisherDto publisher = adSpace.getPublication().getPublisher();
            increment(publisher.getId(), AsCounter.ClickCompleted);
        } else {
            increment(RtbExchange.Unknown, AsCounter.ClickCompleted);
        }
    }

    /**
     * In beacon/click controller we do not know publisher at the start of processing...
     */
    public void increment(AsCounter counter) {
        counterManager.incrementCounter(counter.name());
    }

    public void increment(RtbExchange exchange, AsCounter counter) {
        counterManager.incrementCounter(counter.name());
        counterManager.incrementCounter(getCounterName(exchange, counter.name()));
    }

    /**
     * @param adSpace optional
     */
    public void increment(AdSpaceDto adSpace, AsCounter counter) {
        counterManager.incrementCounter(counter.name());
        if (adSpace != null) {
            counterManager.incrementCounter(getCounterName(adSpace.getPublication().getPublisher().getId(), counter.name()));
        } else {
            counterManager.incrementCounter(getCounterName(RtbExchange.Unknown, counter.name()));
        }
    }

    public void increment(Long publisherId, AsCounter counter) {
        counterManager.incrementCounter(counter.name());
        counterManager.incrementCounter(getCounterName(publisherId, counter.name()));
    }

    public void increment(String publisherExternalId, AsCounter counter) {
        counterManager.incrementCounter(counter.name());
        counterManager.incrementCounter(getCounterName(publisherExternalId, counter.name()));
    }

    /**
     * Only general purpose counter method. Use only when AsCounter enum entry cannot be added 
     */
    public void increment(String publisherExternalId, String counterName) {
        counterManager.incrementCounter(counterName);
        counterManager.incrementCounter(getCounterName(publisherExternalId, counterName));
    }

    /**
     * Only general purpose counter mathod. Use only when AsCounter enum entry cannot be added 
     */
    public void increment(Long publisherId, String counterName) {
        counterManager.incrementCounter(counterName);
        counterManager.incrementCounter(getCounterName(publisherId, counterName));
    }

    /**
     * Only general purpose counter mathod. Use only when AsCounter enum entry cannot be added 
     */
    public void increment(RtbExchange exchange, String counterName) {
        counterManager.incrementCounter(counterName);
        counterManager.incrementCounter(getCounterName(exchange, counterName));
    }

    /*
        public void inc(String publisherExternalId, Exception exception) {
            counterManager.incrementCounter(getCounterName(publisherExternalId, String.valueOf(exception.getClass().getName())));
        }
    */
    /*
        public void inc(String publisherExternalId, NoBidException exception) {
            counterManager.incrementCounter(getCounterName(publisherExternalId, String.valueOf(exception.getClass().getName())));
        }
    */
    private static final String getCounterName(Long publisherId, String metric) {
        RtbExchange exchange = RtbExchange.getByPublisherId(publisherId);
        StringBuilder sb = new StringBuilder(exchange != null ? String.valueOf(exchange) : String.valueOf(publisherId));
        sb.append('.').append(metric);
        return sb.toString();
    }

    private static final String getCounterName(String publisherExternalId, String metric) {
        RtbExchange exchange = RtbExchange.lookup(publisherExternalId);
        StringBuilder sb = new StringBuilder(exchange != null ? String.valueOf(exchange) : publisherExternalId);
        sb.append('.').append(metric);
        return sb.toString();
    }

    private static final String getCounterName(RtbExchange exchange, String metric) {
        StringBuilder sb = new StringBuilder(exchange.name());
        sb.append('.').append(metric);
        return sb.toString();
    }

}
