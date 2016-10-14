package com.adfonic.adserver.spring.config;

import net.citrusleaf.CitrusleafClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.adfonic.adserver.FrequencyCounter;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.ParallelModeCacheService;
import com.adfonic.adserver.impl.icache.NamespaceAwareFrequencyCounter;
import com.adfonic.adserver.impl.icache.NamespaceAwareImpressionService;
import com.adfonic.adserver.impl.icache.NamespaceAwareParallelModeCacheService;
import com.adfonic.adserver.impl.icache.NamespaceAwareRtbCacheService;
import com.adfonic.adserver.rtb.RtbCacheService;
import com.adfonic.adserver.rtb.util.ConditionalOnSystemProperty;
import com.adfonic.cache.CacheManager;
import com.adfonic.cache.citrusleaf.CitrusleafCacheManagerImpl;
import com.adfonic.util.stats.CounterManager;

/**
 * 
 * @author mvanek
 * 
 * What used to be in adfonic-adserver-citrusleaf.xml
 */
@Configuration
@ConditionalOnSystemProperty(name = "adserver.impcache.implementation", value = ConditionalOnSystemProperty.PROPERTY_IS_NULL)
public class ImpcacheCitrusleafConfig {

    @Bean(destroyMethod = "close")
    public CitrusleafClient citrusleafClient(@Value("${Citrusleaf.hostName}") String hostname, @Value("${Citrusleaf.port}") int port) {
        return new CitrusleafClient(hostname, port);
    }

    @Bean
    public CacheManager cacheManager(CitrusleafClient citrusleafClient, @Value("${Citrusleaf.hostName}") String hostname, @Value("${Citrusleaf.port}") int port,
                                     @Value("${Citrusleaf.namespace.cache}") String namespace, @Value("${Citrusleaf.connectTimeoutMs}") int connectTimeout, 
                                     @Value("${Citrusleaf.operationTimeoutMs}") int operationTimeout) {
        return new CitrusleafCacheManagerImpl(citrusleafClient, hostname, port, namespace, connectTimeout, operationTimeout);
    }

    @Bean
    public ImpressionService impressionService(CacheManager cacheManager, KryoManager kryoManager) {
        return new NamespaceAwareImpressionService(cacheManager, kryoManager);
    }

    @Bean
    public FrequencyCounter frequencyCounter(CacheManager cacheManager, CounterManager counterManager) {
        return new NamespaceAwareFrequencyCounter(cacheManager, counterManager);
    }

    @Bean
    public RtbCacheService rtbCacheService(CacheManager cacheManager, KryoManager kryoManager, @Value("${cache.RtbBidDetails.ttlSeconds}") int ttlSeconds) {
        return new NamespaceAwareRtbCacheService(cacheManager, kryoManager, ttlSeconds);
    }

    @Bean
    public ParallelModeCacheService parallelModeCacheService(CacheManager cacheManager, KryoManager kryoManager, @Value("${cache.ParallelModeBidDetails.ttlSeconds}") int ttlSeconds) {
        return new NamespaceAwareParallelModeCacheService(cacheManager, kryoManager, ttlSeconds);
    }

}
