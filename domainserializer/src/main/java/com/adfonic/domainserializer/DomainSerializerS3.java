package com.adfonic.domainserializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.adfonic.domain.cache.AbstractSerializableCacheManager;
import com.adfonic.domain.cache.DataCollectorDomainCache;
import com.adfonic.domain.cache.DataCollectorDomainCacheLoader;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.DomainCacheImpl;
import com.adfonic.domain.cache.DomainCacheLoader;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.AdserverDomainCacheImplExt;
import com.adfonic.domainserializer.loader.AdCacheBuildParams;
import com.adfonic.domainserializer.loader.AdserverDomainCacheLoader;
import com.adfonic.util.ConfUtils;

@ManagedResource
public class DomainSerializerS3 {

    private static final Logger LOG = LoggerFactory.getLogger(DomainSerializerS3.class);

    private static final String DEFAULT_LABEL = "default";

    // This is used to dynamically append -yyyyMMdd.log to log file names
    // private static final FastDateFormat LOG_FILE_DATE_FORMAT = FastDateFormat.getInstance("yyyyMMdd");

    @Autowired
    private DomainCacheLoader domainCacheLoader;

    @Autowired
    private AdserverDomainCacheLoader adserverCacheLoader;

    @Autowired
    private DataCollectorDomainCacheLoader dataCollectorCacheLoader;

    @Autowired
    private DsCacheManager cacheManager;

    @Autowired
    @Qualifier("domainSerializerProperties")
    private Properties properties;

    private static boolean singleRun = BooleanUtils.toBoolean(System.getProperty("singleRun"));
    public static boolean skipS3upload = BooleanUtils.toBoolean(System.getProperty("skipS3upload"));

    // Domain cache settings
    public static final boolean omitDomainCache = BooleanUtils.toBoolean(System.getProperty("omitDomainCache"));

    // Beware that cluster name (Domain caches) must be different from shard name (Adserver caches) otherwise unwanted mutual S3 move-outs will happen
    @Value("${DomainCache.distribution.clusters}")
    private String[] domainCacheClusterNames;

    @Value("${DomainCache.reloadPeriodSeconds}")
    private int reloadDomainPeriodSeconds;

    private final List<DsCluster> activeDomainClusters = new ArrayList<DsCluster>();
    private Thread domainCacheThread;
    private volatile long lastDomainCacheReloadTime;
    private AtomicReference<String> lastHashDomainCache = new AtomicReference<>();

    // Adserver cache settings
    public static final boolean omitAdserverCache = BooleanUtils.toBoolean(System.getProperty("omitAdserverDomainCache"));

    @Value("${AdserverDomainCache.shards}")
    private String[] adserverCacheShardNames;

    @Value("${AdserverDomainCache.reloadPeriodSeconds}")
    private int reloadAdserverPeriodSeconds;

    private final List<DsShard> possibleAdServerShards = new ArrayList<DsShard>();
    private List<DsShard> activeAdserverShards = new ArrayList<DsShard>();
    private Thread adserverCacheThread;
    private volatile long lastAdserverDomainCacheReloadTime;
    private volatile String shardRunning = "NA";
    private final AtomicBoolean adserverCacheRunning = new AtomicBoolean(false);
    private AtomicReference<String> lastHashAdserverCache = new AtomicReference<>();

    // DataCollector cache settings
    public static final boolean omitCollectorCache = BooleanUtils.toBoolean(System.getProperty("omitDataCollectorDomainCache"));

    @Value("${DataCollectorDomainCache.reloadPeriodSeconds}")
    private int reloadCollectorPeriodSeconds;

    private Thread collectorCacheThread;
    private volatile long lastDataCollectorDomainCacheReloadTime;
    private AtomicReference<String> lastHashCollectorCache = new AtomicReference<>();

    private final List<CacheBuildStats> statistics = new LinkedList<CacheBuildStats>();

    @PostConstruct
    public void initialize() {

        LOG.info("JMX Port=" + System.getProperty("com.sun.management.jmxremote.port"));
        waitForPorfilerToAttach();

        if (omitCollectorCache) {
            LOG.info("Collector cache production disabled");
        }

        if (omitDomainCache) {
            LOG.info("Domain cache (cluster) production is disabled");
        } else {
            // Load possible DomainCache clusters from properties
            for (String clusterName : domainCacheClusterNames) {
                DsCluster cluster = new DsCluster(clusterName, properties);
                LOG.info("Adding DomainCache cluster: " + cluster);
                activeDomainClusters.add(cluster);
            }
            if (activeDomainClusters.isEmpty()) {
                throw new IllegalArgumentException("No DomainCache clusters defined in properties");
            }
        }

        if (omitAdserverCache) {
            LOG.info("AdServer cache (shards) production is disabled");
        } else {
            activeAdserverShards = initAdserverShards();
        }
    }

    private List<DsShard> initAdserverShards() {
        // Load possible AdserverCache shards from properties
        for (String shardName : adserverCacheShardNames) {
            DsShard shard = new DsShard(shardName, properties);
            LOG.info("Adding AdserverDomainCache shard: " + shard);
            possibleAdServerShards.add(shard);
        }
        if (possibleAdServerShards.isEmpty()) {
            throw new IllegalArgumentException("No AdserverDomainCache shards defined in properties");
        }

        // Now select active shard 
        String shardToRunFor = System.getProperty("shard");
        if (shardToRunFor == null) {
            throw new IllegalArgumentException("System property 'shard' not set. Possible shards: " + possibleAdServerShards);
        }
        String[] allShards = shardToRunFor.split(",");
        for (String shardName : allShards) {
            boolean found = false;
            for (DsShard oneShard : possibleAdServerShards) {
                if (oneShard.getName().equalsIgnoreCase(shardName)) {
                    found = true;
                    activeAdserverShards.add(oneShard);
                    break;
                }
            }
            if (found == false) {
                throw new IllegalArgumentException("Shard '" + shardName + "' not found in: " + possibleAdServerShards);
            }
        }
        if (activeAdserverShards.isEmpty()) {
            throw new IllegalArgumentException("No Active shards! " + shardToRunFor + " vs " + possibleAdServerShards);
        }
        LOG.info("Active shards: " + activeAdserverShards);
        return activeAdserverShards;
    }

    public List<CacheBuildStats> getStatistics() {
        return statistics;
    }

    public List<DsShard> getActiveAdserverShards() {
        return activeAdserverShards;
    }

    public List<DsCluster> getActiveDomainClusters() {
        return activeDomainClusters;
    }

    public int getReloadAdserverPeriodSeconds() {
        return reloadAdserverPeriodSeconds;
    }

    public int getReloadCollectorPeriodSeconds() {
        return reloadCollectorPeriodSeconds;
    }

    public int getReloadDomainPeriodSeconds() {
        return reloadDomainPeriodSeconds;
    }

    private CacheBuildStats newCacheBuildStats(String label) {
        CacheBuildStats cacheBuildStats = new CacheBuildStats();
        cacheBuildStats.setLabel(label);
        cacheBuildStats.setDbSelectionStartedAt(new Date());
        statistics.add(0, cacheBuildStats);
        if (statistics.size() > 100) { // limit number of entries...
            statistics.remove(statistics.size() - 1);
        }
        return cacheBuildStats;
    }

    private void startCollectoCacheThread() {
        LOG.info("Starting CollectorCache Thread");
        collectorCacheThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    long startTimestampMillis = System.currentTimeMillis();
                    try {
                        reloadDataCollectorCache();
                    } catch (Exception x) {
                        LOG.warn("CollectorCache population failed", x);
                    }
                    pauseThread(startTimestampMillis, reloadCollectorPeriodSeconds * 1000);
                }
            }
        };
        collectorCacheThread.setName("CollectorCache");
        collectorCacheThread.setDaemon(true);
        collectorCacheThread.start();
    }

    private void startDomainCacheThread() {
        LOG.info("Starting DomainCache Thread");
        domainCacheThread = new Thread() {

            @Override
            public void run() {

                while (true) {
                    long startTimestampMillis = System.currentTimeMillis();
                    try {
                        reloadDomainCache();
                    } catch (Exception x) {
                        LOG.warn("DomainCache population failed", x);
                    }
                    pauseThread(startTimestampMillis, reloadDomainPeriodSeconds * 1000);
                }
            };
        };
        domainCacheThread.setName("DomainCache");
        domainCacheThread.setDaemon(true);
        domainCacheThread.start();
    }

    private void startAdServerCacheThread() {
        LOG.info("Starting AdServerCache Thread");
        adserverCacheThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    long startTimestampMillis = System.currentTimeMillis();
                    try {
                        reloadAdserverDomainCache();
                    } catch (Exception x) {
                        LOG.warn("AdserverDomainCache population failed", x);
                    }
                    pauseThread(startTimestampMillis, reloadAdserverPeriodSeconds * 1000);
                }
            };
        };
        adserverCacheThread.setName("AdServerCache");
        adserverCacheThread.setDaemon(true);
        adserverCacheThread.start();
    }

    /**
     * Only production of AdServer cache takes significant time (about 12 minutes) 
     * and only this cache we want to be produced as quickly as we can, without delays between them. 
     * 
     * Other production caches (and Dev or QA caches) are generated very quickly (few seconds). 
     * We must throttle pace for quickly produced caches to not overload DB by frequest queries.
     */
    private void pauseThread(long workStartMillis, int minimalPeriodMillis) {
        long workMillis = System.currentTimeMillis() - workStartMillis;
        long pacingMillis = minimalPeriodMillis - workMillis;
        if (pacingMillis > 0) {
            LOG.info("Cache population time: " + (workMillis / 1000) + " seconds. Going to sleep for " + (pacingMillis / 1000) + " seconds");
            try {
                Thread.sleep(pacingMillis);
            } catch (InterruptedException ix) {
                LOG.warn("Sleep interrupted. Going back to work");
            }
        } else {
            LOG.info("No sleep as cache population time: " + (workMillis / 1000) + " seconds is more than minimal period: " + (minimalPeriodMillis / 1000));
        }
    }

    /**
     * Reload the DomainCache. Invoked by the scheduler.
     */
    public void reloadDomainCache() {
        // Track this since we expose it for JMX-based monitoring
        lastDomainCacheReloadTime = System.currentTimeMillis();
        CacheBuildStats stats = newCacheBuildStats(activeDomainClusters.get(0).getName());

        DomainCacheImpl domainCache = null;
        try {
            domainCache = domainCacheLoader.loadDomainCache();
        } catch (Exception x) {
            stats.setException(x);
            LOG.error("Failed to populate DomainCache", x);
            return;
        }

        stats.setContectStatsString(domainCache.getStatsString());
        domainCache.logCounts("", java.util.logging.Logger.getLogger(getClass().getName()), java.util.logging.Level.INFO);

        Date dateGenerated = new Date();
        domainCache.setSerializationStartedAt(dateGenerated);
        // Invoke the distribution script for the main cache for all clusters
        for (DsCluster cluster : activeDomainClusters) {
            // Generate a cluster-specific "batch id"
            String batchId = AbstractSerializableCacheManager.generateBatchId(DomainCache.class, cluster.getName(), dateGenerated);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Generated batchId: " + batchId);
            }
            try {
                cacheManager.distribute(domainCache, batchId, cluster.getName(), lastHashDomainCache, stats);
            } catch (Exception x) {
                stats.setException(x);
                LOG.error("Failed to distribute DomainCache for cluster: " + cluster.getName(), x);
                return;
            }
        }

    }

    /**
     * Reload the AdserverDomainCache. Invoked by the scheduler.
     */
    public void reloadAdserverDomainCache() {
        LOG.info("Building Adserver Cache for : " + activeAdserverShards);
        try {
            for (DsShard oneShard : activeAdserverShards) {
                shardRunning = oneShard.getName();
                createAdserverDomainCacheForShard(oneShard);
            }
        } finally {
            shardRunning = "None-Waiting for some time";
        }

    }

    private void createAdserverDomainCacheForShard(DsShard shard) {
        // Track this since we expose it for JMX-based monitoring
        lastAdserverDomainCacheReloadTime = System.currentTimeMillis();
        CacheBuildStats stats = newCacheBuildStats(shard.getName());

        if (adserverCacheRunning.getAndSet(true)) {
            LOG.info("Adserver Domain Cache already running so skipping this time");
            return;
        }

        try {
            AdserverDomainCacheImplExt adserverDomainCache = adserverCacheLoader.loadAdserverDomainCache(new AdCacheBuildParams(shard, stats));

            Date dateGenerated = new Date();
            String cacheBatchId = AbstractSerializableCacheManager.generateBatchId(AdserverDomainCache.class, shard.getName(), dateGenerated);
            stats.setLabel(cacheBatchId);

            adserverDomainCache.beforeSerialization();
            // This must be world stupidest way of logging...
            adserverDomainCache.logCounts("Master", java.util.logging.Logger.getLogger(getClass().getName()), java.util.logging.Level.INFO);

            CapturingLogger clogger = new CapturingLogger();
            adserverDomainCache.logCounts("Master", clogger, java.util.logging.Level.INFO);
            stats.setContectStatsString(clogger.getCaptured());

            cacheManager.distribute(adserverDomainCache, cacheBatchId, shard.getName(), lastHashAdserverCache, stats);

        } catch (Exception x) {
            LOG.error("Error occured while loading Adserver cache", x);
            stats.setException(x);
            return;
        } finally {
            adserverCacheRunning.set(false);
        }
    }

    /**
     * Reload the DataCollectorDomainCache. Invoked by the scheduler.
     */
    public void reloadDataCollectorCache() {
        // Track this since we expose it for JMX-based monitoring
        lastDataCollectorDomainCacheReloadTime = System.currentTimeMillis();
        CacheBuildStats stats = newCacheBuildStats(DEFAULT_LABEL);

        DataCollectorDomainCache dataCollectorDomainCache;
        try {
            dataCollectorDomainCache = dataCollectorCacheLoader.loadDataCollectorDomainCache();
        } catch (Exception x) {
            stats.setException(x);
            LOG.error("Failed to populate DataCollectorDomainCache", x);
            return;
        }

        dataCollectorDomainCache.logCounts("", java.util.logging.Logger.getLogger(getClass().getName()), java.util.logging.Level.INFO);

        // Generate a batch id for the DataCollectorDomainCache
        String batchId = AbstractSerializableCacheManager.generateBatchId(DataCollectorDomainCache.class, DEFAULT_LABEL, new Date());
        // Serialize the DataCollectorDomainCache
        try {
            cacheManager.distribute(dataCollectorDomainCache, batchId, DEFAULT_LABEL, lastHashCollectorCache, stats);
        } catch (Exception x) {
            stats.setException(x);
            LOG.error("Failed to serialize DataCollectorDomainCache", x);
            return;
        }
    }

    private static class CapturingLogger extends java.util.logging.Logger {

        private StringBuilder builder = new StringBuilder();

        protected CapturingLogger() {
            super("name", null);
        }

        @Override
        public void log(java.util.logging.Level level, String msg) {
            builder.append(msg).append('\n');
        }

        public String getCaptured() {
            return builder.toString();
        }

    }

    @ManagedAttribute(currencyTimeLimit = 3)
    public long getTimeSinceLastDomainCacheReload() {
        return (System.currentTimeMillis() - lastDomainCacheReloadTime) / 1000;
    }

    @ManagedAttribute(currencyTimeLimit = 3)
    public long getTimeSinceLastAdserverDomainCacheReload() {
        return (System.currentTimeMillis() - lastAdserverDomainCacheReloadTime) / 1000;
    }

    @ManagedAttribute(currencyTimeLimit = 3)
    public long getTimeSinceLastDataCollectorDomainCacheReload() {
        return (System.currentTimeMillis() - lastDataCollectorDomainCacheReloadTime) / 1000;
    }

    @ManagedAttribute(currencyTimeLimit = 3)
    public String getShardRunning() {
        return shardRunning;
    }

    /*
        @ManagedOperation
        @ManagedOperationParameters({
                @ManagedOperationParameter(name = "shardName", description = "Shard Name, e.g. default,shard-rtb,mobclix,smaato"),
                @ManagedOperationParameter(name = "publisherIds", description = "PublisherIds,seprated by commas"),
                @ManagedOperationParameter(name = "shardMode", description = "shardMode,included or excluded.Default will be taken from shard configuration.e.g. for default it will be excluded and for rtb it will be included") })
        public String generateAdserverCacheOnDemand(String shardName, String commaSepratedPublisherIds, String shardMode) {
            try {
                if (adserverCacheRunning.get()) {
                    throw new Exception("Adserver Domain Cahce already being generated so skipping this time");
                }
                if (StringUtils.isEmpty(shardName.trim())) {
                    throw new Exception("Shard name can not be empty,available shard names are : " + StringUtils.join(shardsToRunFor, ','));
                }
                Shard shard = getShardByName(shardName);
                if (shard == null) {
                    throw new Exception("No such shard configured '" + shardName + "',available shard names are : " + StringUtils.join(shardsToRunFor, ','));
                }
                ShardMode jmxRequestedShardMode = null;
                if (!StringUtils.isEmpty(shardMode.trim())) {
                    if (ShardMode.include.name().equalsIgnoreCase(shardMode)) {
                        jmxRequestedShardMode = ShardMode.include;
                    }
                    if (ShardMode.exclude.name().equalsIgnoreCase(shardMode)) {
                        jmxRequestedShardMode = ShardMode.exclude;
                    }
                }
                final Shard jmxRequestedShard = new Shard(shard, jmxRequestedShardMode);

                if (!StringUtils.isEmpty(commaSepratedPublisherIds)) {
                    // If publisher Ids passed from jmx call then use them and
                    // remove existing ones
                    jmxRequestedShard.getPublisherIds().clear();
                    String[] publisherIdArray = commaSepratedPublisherIds.split(",");
                    for (String onePublisherId : publisherIdArray) {
                        jmxRequestedShard.getPublisherIds().add(Long.parseLong(onePublisherId.trim()));
                    }
                }
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        createAdserverDomainCacheForShard(jmxRequestedShard);

                    }
                };

                Thread t = new Thread(runnable);
                t.start();
                commaSepratedPublisherIds = StringUtils.join(jmxRequestedShard.getPublisherIds(), ',');
                return "Cache generation task for Shard=" + shard + " and publisher Ids included in " + commaSepratedPublisherIds + " submitted succesfully ";
            } catch (Exception e) {
                return e.getMessage();
            }
        }
    */
    public static void main(String[] args) {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LOG.info("Initialized SLF4J Java Util Logging Bridge");

        // Instract Spring Boot to use our legacy externaly located property file
        String configFile = "file:" + System.getProperty(ConfUtils.CONFIG_DIR_PROPERTY, ConfUtils.CONFIG_DIR_DEFAULT) + "/" + "adfonic-domainserializer.properties";
        System.setProperty("spring.config.location", configFile);

        ConfigurableApplicationContext context = SpringApplication.run(DsSpringBootConfig.class, args);
        DomainSerializerS3 serializer = context.getBean(DomainSerializerS3.class);
        if (singleRun) {
            // Run only once
            if (!omitDomainCache) {
                serializer.reloadDomainCache();
            }
            if (!omitAdserverCache) {
                serializer.reloadAdserverDomainCache();
            }
            if (!omitCollectorCache) {
                serializer.reloadDataCollectorCache();
            }
            context.close(); // Closing spring context also stops embedded Tomcat
        } else {
            // Normal DS operation
            if (!omitDomainCache) {
                serializer.startDomainCacheThread();
            }
            if (!omitAdserverCache) {
                serializer.startAdServerCacheThread();
            }
            if (!omitCollectorCache) {
                serializer.startCollectoCacheThread();
            }
        }
        LOG.info("Leaving main method");

    }

    private void waitForPorfilerToAttach() {
        if (BooleanUtils.toBoolean(System.getProperty("DomainSerializer.debug"))) {
            LOG.info("Sleeping for 20 sec to let the debugger attach to this process");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted");
            }
        }
    }

}
