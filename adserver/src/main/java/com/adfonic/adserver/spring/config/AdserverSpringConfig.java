package com.adfonic.adserver.spring.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.velocity.VelocityEngineFactory;

import com.adfonic.adresponse.AdMarkupRenderer;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BackupLoggerFilter;
import com.adfonic.adserver.Constant;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties.DcProperty;
import com.adfonic.adserver.KryoManager;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.ReservePot;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.impl.DataCacheProperties;
import com.adfonic.adserver.impl.DeviceFeaturesTargetingChecks;
import com.adfonic.adserver.impl.DeviceIdentifierTargetingChecks;
import com.adfonic.adserver.impl.DeviceLocationTargetingChecks;
import com.adfonic.adserver.impl.LocalBudgetManagerCassandra;
import com.adfonic.adserver.impl.ReservePotImpl;
import com.adfonic.adserver.impl.ReservePotDao;
import com.adfonic.adserver.impl.StoppageManagerImpl;
import com.adfonic.adserver.rtb.dec.AdXEncUtil;
import com.adfonic.adserver.rtb.dec.SecurityAlias;
import com.adfonic.adserver.rtb.impl.BidRateThrottler;
import com.adfonic.adserver.stoppages.DatabaseStoppageServiceImpl;
import com.adfonic.adserver.stoppages.StoppagesService;
import com.adfonic.data.cache.AdserverDataCacheManager;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.DomainCacheManager;
import com.adfonic.jms.JmsResource;
import com.adfonic.jms.JmsUtils;
import com.adfonic.util.ConfUtils;
import com.adfonic.util.StringUtils;
import com.adfonic.util.TimeZoneUtils;
import com.adfonic.util.stats.CounterManager;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * 
 * @author mvanek
 *
 */
@Configuration
@EnableMBeanExport
@Import({ AdserverExternalServicesConfig.class, AdserverResourcesSpringConfig.class, AdserverCacheDbSpringConfig.class, AdserverJmsSpringConfig.class,
        AdserverSchedulingConfig.class, AdserverStatusSpringConfig.class, ImpcacheCitrusleafConfig.class, ImpcacheEhcacheConfig.class })
@ComponentScan(basePackages = "com.adfonic.adserver", // 
includeFilters = @Filter(Component.class), // 
excludeFilters = { @Filter(Controller.class), @Filter(Configuration.class), @Filter(type = FilterType.REGEX, pattern = "com\\.adfonic\\.adserver\\.view\\..*") })
public class AdserverSpringConfig {

    static {
        // Initialize TimeZoneUtils, taking the hit on that prior to the first  ad request 
        TimeZoneUtils.getTimeZoneNonBlocking("Europe/London");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        String configHome = System.getProperty(ConfUtils.CONFIG_DIR_PROPERTY, ConfUtils.CONFIG_DIR_DEFAULT);
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setFileEncoding("utf-8");
        configurer.setLocation(new FileSystemResource(configHome + "/" + Constant.AS_CONFIG_FILENAME));
        return configurer;
    }

    @Bean(name = "adserverProperties")
    public Properties adserverProperties(@Value(ConfUtils.CONFIG_DIR_CONFIG) String configHome) throws IOException {
        PropertiesFactoryBean factoryBean = new PropertiesFactoryBean();
        factoryBean.setLocation(new FileSystemResource(configHome + "/" + Constant.AS_CONFIG_FILENAME));
        factoryBean.setFileEncoding("utf-8");
        factoryBean.afterPropertiesSet();
        Properties properties = factoryBean.getObject();
        // Initialize 
        SecurityAlias.rebuildCache(properties);
        return properties;
    }

    @Bean
    public DataCacheProperties dataCacheProperties(AdserverDataCacheManager adserverDataCacheManager, @Qualifier("adserverProperties") Properties adserverProperties) {
        return new DataCacheProperties(adserverDataCacheManager, adserverProperties);
    }

    @Bean
    public AdXEncUtil adXEncUtil(@Value("${Rtb.Enc.dcadx.eKey64}") String encryptionKey, @Value("${Rtb.Enc.dcadx.iKey64}") String integrityKey) {
        return new AdXEncUtil(encryptionKey, integrityKey);
    }

    @Bean
    public DeviceLocationTargetingChecks geoTargetingChecks(CounterManager counterManager, DataCacheProperties dcProperties) {
        return new DeviceLocationTargetingChecks(counterManager, dcProperties);
    }

    @Bean
    public BidRateThrottler throttler(@Value("${Rtb.cas.sampling.enable:false}") boolean casEnable, @Value("${Rtb.approx.sampling.rate:100}") int samplingRate) {
        return new BidRateThrottler(casEnable, samplingRate);
    }

    @Bean
    public DeviceIdentifierTargetingChecks deviceAudienceChecks(DataCacheProperties dcProperties) {
        Set<Long> weveCompanyIds = StringUtils.toSetOfLongs(dcProperties.getProperty(DcProperty.WeveAdvertisers), ",");
        return new DeviceIdentifierTargetingChecks(weveCompanyIds);
    }

    @Bean
    public DeviceFeaturesTargetingChecks deviceFeaturesChecks() {
        return new DeviceFeaturesTargetingChecks();
    }

    /*
        @Bean
        public BackupLogger backupLogger(@Value("${BackupLogger.enabled}") boolean enabled, @Value("${BackupLogger.fileName}") String fileName,
                @Value("${BackupLogger.datePattern:.yyyy-MM-dd-HH}") String datePattern, @Value("${BackupLogger.maxFileSize:100MB}") String maxFileSize,
                @Value("${BackupLogger.maxRollFileCount:2147483647}") int maxRollFileCount, @Value("${BackupLogger.scavengeInterval:-1}") int scavengeInterval) throws IOException {
            return new ArchiveV1BackupLoggerImpl(enabled, fileName, datePattern, maxFileSize, maxRollFileCount, scavengeInterval);
        }
    */
    @Bean(name = "backupLoggerFilter")
    public BackupLoggerFilter backupLoggerFilter(BackupLogger backupLogger) {
        return new BackupLoggerFilter(backupLogger);
    }

    @Bean(name = "budgetManager")
    public LocalBudgetManager budgetManager(Cluster cassandraCluster, @Value("${budgeting.cassandra.keyspace}") String keyspace, CounterManager counterManager,
            @Value("${click.default.ttlSeconds}") int clickTtlSec,
        @Value("${cassandra.extraLogging:false}") boolean extraLogging) {
        Session session = cassandraCluster.connect(keyspace);
        return new LocalBudgetManagerCassandra(cassandraCluster, session, counterManager, clickTtlSec, extraLogging);
    }
    
    @Bean(name = "reservePot")
    public ReservePot reservePot(Cluster cassandraCluster, @Value("${budgeting.cassandra.keyspace}") String keyspace) {
        Session session = cassandraCluster.connect(keyspace);
        ReservePotDao dao = new ReservePotDao(cassandraCluster, session);
        return new ReservePotImpl(dao);
    }

    @Bean
    public KryoManager kryoManager() {
        return new KryoManager();
    }

    //adfonic-adserver-adevent-context.xml
    @Bean
    public com.adfonic.adserver.AdEventFactory adEventFactory() {
        return new com.adfonic.adserver.AdEventFactory(kryoManager());
    }

    //adfonic-status-change-manager-context.xml
    @Bean
    public StatusChangeManager statusChangeManager() {
        return new com.adfonic.adserver.impl.StatusChangeManagerImpl();
    }

    //adfonic-stoppage-manager-context.xml
    @Bean
    public StoppageManager stoppageManager(@Qualifier(AdserverCacheDbSpringConfig.CACHEDB_DS) DataSource dataSource, @Value("${StoppageManager.lazyInit}") boolean lazyInit)
            throws IOException {
        StoppagesService stoppagesService = new DatabaseStoppageServiceImpl(dataSource);
        return new StoppageManagerImpl(lazyInit, stoppagesService);
    }

    //adfonic-domain-cache-context.xml
    @Bean
    public DomainCacheManager domainCacheManager(@Value(ConfUtils.CACHE_DIR_CONFIG) File cacheHome, @Value("${DomainCache.label}") String label,
            @Value("${DomainCache.useMemory}") boolean useMemory) {
        return new DomainCacheManager(cacheHome, label, useMemory);
    }

    @Bean
    public AdserverDomainCacheManager adserverDomainCacheManager(@Value(ConfUtils.CACHE_DIR_CONFIG) File cacheHome, @Value("${AdserverDomainCache.label}") String label,
            @Value("${AdserverDomainCache.useMemory}") boolean useMemory) {
        return new AdserverDomainCacheManager(cacheHome, label, useMemory);
    }

    @Bean
    public AdMarkupRenderer adMarkupRenderer(AdResponseLogic adResponseLogic, DisplayTypeUtils displayTypeUtils, CounterManager counterManager) {
        return new AdMarkupRenderer(adResponseLogic, displayTypeUtils, counterManager);
    }

    @Bean
    public VelocityEngine velocityEngine() throws VelocityException, IOException {
        VelocityEngineFactory factory = new VelocityEngineFactory();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("resource.loader", "class");
        properties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.put("class.resource.loader.resourceLoaderPath", "/velocity");
        factory.setVelocityPropertiesMap(properties);
        return factory.createVelocityEngine();
    }

    //adfonic-tracker-context.xml
    @Bean
    public com.adfonic.adserver.TrackerClient trackerClient(@Value("${tracker.blocked:false}") boolean blocked,
            @Qualifier(JmsResource.CENTRAL_JMS_TEMPLATE) JmsTemplate centralJmsTemplate, JmsUtils jmsUtils) {
        return new com.adfonic.adserver.TrackerClient(blocked, JmsResource.TRACKING_ACTION, jmsUtils, centralJmsTemplate);
    }

}
