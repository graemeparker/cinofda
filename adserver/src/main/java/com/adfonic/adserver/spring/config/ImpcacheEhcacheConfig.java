package com.adfonic.adserver.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.ParallelModeCacheService;
import com.adfonic.adserver.impl.icache.SharedNamespaceFrequencyCounter;
import com.adfonic.adserver.impl.icache.SharedNamespaceImpressionService;
import com.adfonic.adserver.impl.icache.SharedNamespaceParallelModeCacheService;
import com.adfonic.adserver.impl.icache.SharedNamespaceRtbCacheService;
import com.adfonic.adserver.rtb.RtbCacheService;
import com.adfonic.adserver.rtb.util.ConditionalOnSystemProperty;
import com.adfonic.cache.CacheManager;
import com.adfonic.cache.ehcache.EhcacheCacheManagerImpl;
import com.adfonic.util.stats.CounterManager;

/**
 * 
 * @author mvanek
 *
 * Former adfonic-adserver-in-memory-ehcache.xml
 */
@Configuration
@ConditionalOnSystemProperty(name = "adserver.impcache.implementation", value = "ehcache")
public class ImpcacheEhcacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new EhcacheCacheManagerImpl(100000);
    }

    @Bean
    public ImpressionService impressionService(CacheManager cacheManager, KryoManager kryoManager) {
        return new SharedNamespaceImpressionService(cacheManager, kryoManager);
    }

    @Bean
    public FrequencyCounter frequencyCounter(CacheManager cacheManager, CounterManager counterManager) {
        return new SharedNamespaceFrequencyCounter(cacheManager, counterManager);
    }

    @Bean
    public RtbCacheService rtbCacheService(CacheManager cacheManager, KryoManager kryoManager, @Value("${cache.RtbBidDetails.ttlSeconds}") int ttlSeconds) {
        return new SharedNamespaceRtbCacheService(cacheManager, kryoManager, ttlSeconds);
    }

    @Bean
    public ParallelModeCacheService parallelModeCacheService(CacheManager cacheManager, KryoManager kryoManager, @Value("${cache.ParallelModeBidDetails.ttlSeconds}") int ttlSeconds) {
        return new SharedNamespaceParallelModeCacheService(cacheManager, kryoManager, ttlSeconds);
    }

}
