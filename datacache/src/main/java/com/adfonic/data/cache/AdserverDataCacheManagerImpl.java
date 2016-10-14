package com.adfonic.data.cache;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.loader.EcpmDataCacheLoader;
import com.adfonic.data.cache.ecpm.repository.EcpmRepositoryIncremental;
import com.adfonic.data.cache.loaders.DataCacheCurrencyLoader;
import com.adfonic.data.cache.util.Properties;
import com.adfonic.data.cache.util.PropertiesFactory;
import com.adfonic.data.cache.ws.EcpmDataAdapter;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;
import com.adfonic.domain.cache.service.WeightageServices;

/**
 * TODO revisit after successful deployment and delete commented out code  
 *
 */
public class AdserverDataCacheManagerImpl implements AdserverDataCacheManager {

    private static final transient Logger LOG = Logger.getLogger(AdserverDataCacheManagerImpl.class.getName());

    private final AdserverDomainCacheManager adserverDomainCacheManager;

    private final EcpmDataCacheLoader ecpmDataCacheLoader;

    private final DataCacheCurrencyLoader currencyLoader;

    private final PropertiesFactory propertiesFactory;

    //private final DataCacheMainLoader dataCacheMainLoader;
    //private final boolean rtbIdServiceEnable;
    //private final ShardMode shardMode;
    //private final Set<Long> dataCachePublishers;

    private final CountDownLatch latchDataCache = new CountDownLatch(1);
    private final AtomicReference<AdserverDataCache> adserverDataCache = new AtomicReference<>();
    private AdfonicStopWatch stopWatch = new AdfonicStopWatch();
    private Map<Long, CreativeDto> domainCacheCreatives;
    private Map<Long, AdSpaceDto> domainCacheAdSpaces;

    /*
    public AdserverDataCacheManagerImpl(ShardMode shardMode, boolean rtbIdServiceEnable, Set<Long> dataCachePublishers, AdserverDomainCacheManager adserverDomainCacheManager,
            EcpmDataCacheLoader ecpmDataCacheLoader, DataCacheMainLoader dataCacheMainLoader, PropertiesFactory propertiesFactory) {
        Objects.requireNonNull(shardMode);
        this.shardMode = shardMode;
        this.rtbIdServiceEnable = rtbIdServiceEnable;
        Objects.requireNonNull(dataCachePublishers);
        this.dataCachePublishers = dataCachePublishers;
        Objects.requireNonNull(adserverDomainCacheManager);
        this.adserverDomainCacheManager = adserverDomainCacheManager;
        Objects.requireNonNull(ecpmDataCacheLoader);
        this.ecpmDataCacheLoader = ecpmDataCacheLoader;
        Objects.requireNonNull(dataCacheMainLoader);
        this.dataCacheMainLoader = dataCacheMainLoader;
        Objects.requireNonNull(propertiesFactory);
        this.propertiesFactory = propertiesFactory;

        // Initialize now as another components need database properties during their's setup
        process();
    }
    */

    public AdserverDataCacheManagerImpl(AdserverDomainCacheManager adserverDomainCacheManager, EcpmDataCacheLoader ecpmDataCacheLoader, DataCacheCurrencyLoader currencyLoader,
            PropertiesFactory propertiesFactory) {
        this.currencyLoader = currencyLoader;
        Objects.requireNonNull(adserverDomainCacheManager);
        this.adserverDomainCacheManager = adserverDomainCacheManager;
        Objects.requireNonNull(ecpmDataCacheLoader);
        this.ecpmDataCacheLoader = ecpmDataCacheLoader;
        Objects.requireNonNull(propertiesFactory);
        this.propertiesFactory = propertiesFactory;

        // Initialize now as another components need database properties during their's setup
        process();
    }

    public void process() {
        Properties properties = this.propertiesFactory.getProperties();
        boolean loadDataCacheEcpmComputation = Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.ecpm.compute"));
        boolean loadDataCacheCategories = false;//Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.categories"));
        boolean loadDataCacheCreatives = false;//Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.creatives"));
        boolean loadDataCacheAdSpaces = false;//Boolean.parseBoolean(properties.getProperty("load.cacheDelegator.adspaces"));
        boolean useDataCacheEcpmComputation = Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.ecpm.compute"));
        boolean useDataCacheCategories = false;//Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.categories"));
        boolean useDataCacheCreatives = false;//Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.creatives"));
        boolean useDataCacheAdSpaces = false;//Boolean.parseBoolean(properties.getProperty("use.cacheDelegator.adspaces"));

        LOG.fine("DataCache delegator load DataCacheEcpmComputation: " + loadDataCacheEcpmComputation + "\n" + "DataCache delegator load DataCacheCategories     : "
                + loadDataCacheCategories + "\n" + "DataCache delegator load DataCacheCreatives      : " + loadDataCacheCreatives + "\n"
                + "DataCache delegator load DataCacheAdSpaces       : " + loadDataCacheAdSpaces + "\n" + "DataCache delegator use DataCacheEcpmComputation : "
                + useDataCacheEcpmComputation + "\n" + "DataCache delegator use DataCacheCategories      : " + useDataCacheCategories + "\n"
                + "DataCache delegator use DataCacheCreatives       : " + useDataCacheCreatives + "\n" + "DataCache delegator use DataCacheAdSpaces        : "
                + useDataCacheAdSpaces);

        LOG.info("DataCache (DB) FLUSHING CACHE...");
        AdserverDataCacheImpl cache = null;
        EcpmDataRepository dataCacheRepository = new EcpmRepositoryIncremental();
        try {
            ecpmDataCacheLoader.loadDefaultSystemVariables(dataCacheRepository, stopWatch);
            cache = new AdserverDataCacheImpl(dataCacheRepository);
        } catch (SQLException e) {
            LOG.severe("DataCache System Variables Loading Error :" + e.getMessage());
        }

        if (loadDataCacheEcpmComputation || loadDataCacheCategories || loadDataCacheCreatives) {

            stopWatch = new AdfonicStopWatch();
            try {
                LOG.info("Loading Domain Cache Dependencies");
                loadDomainCacheDependencies();
            } catch (SQLException e) {
                LOG.severe("Domain Cache Dependencies Loading Error :" + e.getMessage());
            }

            if (loadDataCacheEcpmComputation && cache != null) {
                try {
                    LOG.info("DataCache loading ECPM data.");
                    ecpmDataCacheLoader.loadECPMData(dataCacheRepository, stopWatch, this.domainCacheCreatives, this.domainCacheAdSpaces);
                } catch (SQLException e) {
                    LOG.severe("DataCache ECPM loading Error :" + e.getMessage());
                }
            }
            /*
            if (loadDataCacheCategories && cache != null) {
                try {
                    LOG.info("DataCache loading CATEGORIES data.");
                    dataCacheMainLoader.loadDataCacheCategories(cache, stopWatch);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "DataCache CATEGORIES loading Error : ", e);
                }
            }

            if (loadDataCacheCreatives && cache != null) {
                try {
                    LOG.info("DataCache loading CREATIVES data.");
                    dataCacheMainLoader.loadDataCacheCreatives(cache, this.domainCacheCreatives, stopWatch);
                } catch (Exception e) {
                    LOG.severe("DataCache CREATIVES loading Error :" + e.getMessage());
                }
            }

            if (loadDataCacheAdSpaces && cache != null) {
                try {
                    LOG.info("DataCache loading ADSPACES data.");
                    dataCacheMainLoader.loadDataCacheAdSpaces(cache, shardMode, dataCachePublishers, rtbIdServiceEnable, stopWatch);
                } catch (Exception e) {
                    LOG.severe("DataCache ADSPACES loading Error :" + e.getMessage());
                }
            }
            */
            LOG.info("DataCache (DB) load cache complete...Time needed: " + stopWatch.prettyPrint());
        }

        try {
            if (cache != null) {
                LOG.info("DataCache loading CURRENCY data.");
                currencyLoader.loadCurrencyData(cache, stopWatch);
            }
        } catch (SQLException e) {
            LOG.severe("DataCache CURRENCY loading Error :" + e.getMessage());
        }

        if (cache != null) {
            cache.setProperties(properties);
            adserverDataCache.set(cache);
            latchDataCache.countDown();
        }
    }

    private void loadDomainCacheDependencies() throws SQLException {

        AdserverDomainCache adserverDomainCache = adserverDomainCacheManager.getCache();

        // This map has to contain all the AdSpaces we want to load in the cache
        this.domainCacheCreatives = new ConcurrentHashMap<>();
        for (CreativeDto creativeDto : adserverDomainCache.getAllCreatives())
            domainCacheCreatives.put(creativeDto.getId(), creativeDto);
        this.domainCacheAdSpaces = new ConcurrentHashMap<>();
        for (AdSpaceDto adSpaceDto : adserverDomainCache.getAllAdSpaces())
            this.domainCacheAdSpaces.put(adSpaceDto.getId(), adSpaceDto);
    }

    @Override
    public WeightageServices getEcpmDataCacheAsWS() {
        try {
            latchDataCache.await();
        } catch (InterruptedException e) {
            LOG.severe("DataCache (DB) Error :" + e.getMessage());
        }
        if (this.adserverDataCache.get() != null)
            return new EcpmDataAdapter(this.adserverDataCache.get());
        else
            return null;
    }

    @Override
    public AdserverDataCache getCache() {
        try {
            latchDataCache.await();
        } catch (InterruptedException e) {
            LOG.severe("DataCache (DB) Error :" + e.getMessage());
        }
        return this.adserverDataCache.get();
    }

}
