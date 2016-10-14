package com.adfonic.domainserializer.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.domain.Feature;
import com.adfonic.domain.Medium;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.ext.AdserverDomainCacheExt;
import com.adfonic.domain.cache.ext.AdserverDomainCacheImplExt;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;
import com.adfonic.domain.cache.listener.DSRejectionListener;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domainserializer.EligibilityChecker;
import com.adfonic.util.DaemonThreadFactory;

/**
 * Adserver Domain Cache Loader
 * 
 * This class should be part of domain-serializer
 */
public class AdserverDomainCacheLoader {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final DataSource toolsDataSource;
    private final AdSpaceLoader adSpaceLoader;
    private final CreativeLoader creativeLoader;

    private EligibilityChecker eligibilityChecker;

    public AdserverDomainCacheLoader(DataSource toolsDataSource, AdSpaceLoader adSpaceLoader, CreativeLoader creativeLoader,
            EligibilityChecker eligibilityChecker) {
        this.toolsDataSource = toolsDataSource;
        this.adSpaceLoader = adSpaceLoader;
        this.creativeLoader = creativeLoader;
        this.eligibilityChecker = eligibilityChecker;
    }

    public synchronized AdserverDomainCacheImplExt loadAdserverDomainCache(AdCacheBuildParams params) throws Exception {
        LOG.info("Loading the AdserverDomainCache");

        profilerWait(2000);

        // These need to be final so they can be used in closures below
        final AdserverDomainCacheImplExt adserverDomainCache = new AdserverDomainCacheImplExt(new Date());
        adserverDomainCache.setRtbEnabled(params.getShard().isRtbEnabled());

        final AdfonicStopWatch stopWatch = new AdfonicStopWatch();
        final AdCacheBuildContext bContext = new AdCacheBuildContext(stopWatch);
        bContext.debugCampaignId = params.getDebugCampaignId();
        bContext.debugCreativeId = params.getDebugCreativeId();
        bContext.debugAdSpaceId = params.getDebugAdSpaceId();
        bContext.debugPublicationId = params.getDebugPublicationId();
        DSRejectionListener eligibilityListener = params.getEligibilityListener();
        if (eligibilityListener != null) {
            bContext.setDsListener(eligibilityListener);
        }

        params.getStats().setDbSelectionStartedAt(new Date());
        // Load categories, building expanded parent/child trees and what not.
        // This has to be done prior to kicking off any concurrent loaders,
        // since they rely on us having derived expanded category trees.
        loadCategories(bContext);
        //Load countries
        loadCountries(bContext);

        //Create Local instead of at Class level
        final ExecutorService futuresExecutorService = Executors.newFixedThreadPool(5, new DaemonThreadFactory("AdCacheLoader-"));
        // Fire off three separate threads...
        // 1. AdSpaces
        Future<AdSpaceService> adspaceFuture = adSpaceLoader.loadAdSpacesConcurrently(futuresExecutorService, bContext, params.getShard());
        // 2. Creatives
        Future<CreativeService> creativesFuture = creativeLoader.loadCreativesConcurrently(futuresExecutorService, bContext);

        // 3. Everything else
        Future<?> miscFuture = futuresExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    stopWatch.start("Loading Misc");
                    // This is for RTB
                    //loadRtbEnabledPublishers(adserverDomainCache);
                    // This is also for RTB
                    loadAdserverPluginExpectedResponseTimes(adserverDomainCache);
                    // Load additional transient dependencies
                    loadAdditionalTransientDependencies(bContext);
                    //Load DS Setting for outputting the rejection reasons
                    stopWatch.stop("Loading Misc");
                } catch (java.sql.SQLException e) {
                    // We can propagate this as an unchecked exception, and the call to
                    // Future.get below will catch it
                    throw new RuntimeException("Misc cache Load failed", e);
                }
            }
        });

        // Now we wait for the futures to come back
        if (LOG.isDebugEnabled()) {
            LOG.debug("Waiting for futures...");
        }

        try {
            // Wait for the Misc Future (likely to return first)
            miscFuture.get();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Misc future returned OK");
            }

            // Wait for the AdSpaces Future (likely to return second)
            // Wait for the AdSpaces Future
            AdSpaceService adSpaceService = adspaceFuture.get();
            adserverDomainCache.setAdspaceService(adSpaceService);
            if (LOG.isDebugEnabled()) {
                LOG.debug("AdSpaces future returned OK");
            }
            // Wait for the Creatives Future
            CreativeService creativeService = creativesFuture.get();
            adserverDomainCache.setCreativeService(creativeService);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creatives future returned OK");
            }

            profilerWait(20000);

            /*
             * Add RtbAdspaces to cache
             * For quick fix, just go through all adspaces and publication addd to Rtb Cache now
             */
            if (adserverDomainCache.isRtbEnabled()) {
                for (AdSpaceDto oneAdspace : adserverDomainCache.getAllAdSpaces()) {
                    adserverDomainCache.addRtbPublicationAdSpace(oneAdspace);
                }
            }

            params.getStats().setEligibilityStartedAt(new Date());
            adserverDomainCache.setElegibilityStartedAt(new Date());
            // We're done loading all data, and now we can derive eligibility
            if (adserverDomainCache.getAllAdSpaces().length == 0) {
                LOG.info("No AdSpaces for eligibility computation");
                return adserverDomainCache;
            }
            if (adserverDomainCache.getAllCreatives().length == 0) {
                LOG.info("No Creatives for eligibility computation");
                return adserverDomainCache;
            }

            eligibilityChecker.deriveEligibleCreativesConcurrently(adserverDomainCache, bContext);

            //profilerWait(20000);
            //if (LOG.isDebugEnabled()) {
            //    bContext.getEligibilityListener().printAllRejectReasons(LOG, Level.FINE);
            //}

        } finally {
            futuresExecutorService.shutdownNow();
        }
        params.getStats().setStopWatchString(stopWatch.prettyPrint());
        LOG.info(stopWatch.prettyPrint());
        return adserverDomainCache;
    }

    private void profilerWait(long milliseconds) {
        if ("true".equals(System.getProperty("com.adfonic.profiler.enabled"))) {
            LOG.warn("*************** Profiler breakpoint, sleeping for 20 sec");
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted");
            }
        }
    }

    private void loadCountries(AdCacheBuildContext td) throws java.sql.SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading Countries");
        }
        td.startWatch("Loading Countries");
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = toolsDataSource.getConnection();
            String sql = "SELECT ID, NAME, ISO_CODE, ISO_ALPHA3 FROM COUNTRY";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            CountryDto country;
            while (rs.next()) {
                country = new CountryDto();
                country.setId(rs.getLong("ID"));
                country.setName(rs.getString("NAME"));
                country.setIsoCode(rs.getString("ISO_CODE"));
                td.allCountries.add(country);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
            td.stopWatch("Loading Countries");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded " + td.allCountries.size() + " Countries");
        }
    }

    private void loadCategories(AdCacheBuildContext td) throws java.sql.SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading Categories");
        }
        td.startWatch("Loading Categories");
        Set<Long> allCategoryIds = new HashSet<Long>();
        Map<Long, Set<Long>> childIdsByParentId = new HashMap<Long, Set<Long>>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = toolsDataSource.getConnection();

            String sql = "SELECT ID, PARENT_ID, CHANNEL_ID FROM CATEGORY";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                long categoryId = rs.getLong(1);
                long parentId = rs.getLong(2);
                long channelId = rs.getLong(3);
                allCategoryIds.add(categoryId);
                if (parentId > 0) {
                    Set<Long> siblings = childIdsByParentId.get(parentId);
                    if (siblings == null) {
                        siblings = new HashSet<Long>();
                        childIdsByParentId.put(parentId, siblings);
                    }
                    siblings.add(categoryId);
                }
                td.channelIdByCategoryId.put(categoryId, channelId);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        // Build the expanded category id cache for every category
        if (LOG.isDebugEnabled()) {
            LOG.debug("Expanding Categories");
        }
        for (long categoryId : allCategoryIds) {
            Set<Long> expanded = new HashSet<Long>();
            expandCategory(categoryId, expanded, childIdsByParentId);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Category id=" + categoryId + " expanded to " + expanded.size() + " categor" + (expanded.size() == 1 ? "y" : "ies"));
            }
            td.expandedCategoryIdsByCategoryId.put(categoryId, expanded);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done Loading and Expanding categories");
        }
        td.stopWatch("Loading Categories");
    }

    // Recursively walk the tree to produce a full inclusive set from
    // the given node onward.  This method is only used when first loading
    // and expanding all the categories.  After that, just use the cached map.
    private void expandCategory(long categoryId, Set<Long> expanded, Map<Long, Set<Long>> childIdsByParentId) {
        expanded.add(categoryId);
        Set<Long> childIds = childIdsByParentId.get(categoryId);
        if (childIds != null) {
            for (long childId : childIds) {
                expandCategory(childId, expanded, childIdsByParentId);
            }
        }
    }

    private void loadAdserverPluginExpectedResponseTimes(AdserverDomainCacheExt cache) throws java.sql.SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading AdserverPlugin expected response times");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        int totalTransientAdserverPluginExpectedResponseTimesByPluginName = 0;
        try {
            conn = toolsDataSource.getConnection();
            String sql = "SELECT SYSTEM_NAME, EXPECTED_RESPONSE_TIME_MILLIS FROM ADSERVER_PLUGIN";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cache.addTransientAdserverPluginExpectedResponseTimesByPluginName(rs.getString(1), rs.getLong(2));
                totalTransientAdserverPluginExpectedResponseTimesByPluginName++;
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded expected response times for " + totalTransientAdserverPluginExpectedResponseTimesByPluginName + " AdserverPlugins");
        }
    }

    private void loadAdditionalTransientDependencies(AdCacheBuildContext td) throws java.sql.SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading Additional Transient Dependencies");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = toolsDataSource.getConnection();

            // Features by ExtendedCreativeType id
            String sql = "SELECT EXTENDED_CREATIVE_TYPE_ID, FEATURE FROM EXTENDED_CREATIVE_TYPE_FEATURE";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long extendedCreativeTypeId = rs.getLong(1);
                Set<Feature> features = td.featuresByExtendedCreativeTypeId.get(extendedCreativeTypeId);
                if (features == null) {
                    features = new HashSet<Feature>();
                    td.featuresByExtendedCreativeTypeId.put(extendedCreativeTypeId, features);
                }
                features.add(Feature.valueOf(rs.getString(2)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Medium and defaultTrackingIdentifierType by PublicationType id
            sql = "SELECT ID, MEDIUM, DEFAULT_TRACKING_IDENTIFIER_TYPE FROM PUBLICATION_TYPE";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long publicationTypeId = rs.getLong(1);
                td.mediumsByPublicationTypeId.put(publicationTypeId, Medium.valueOf(rs.getString(2)));
                td.defaultTrackingIdentifierTypesByPublicationTypeId.put(publicationTypeId, TrackingIdentifierType.valueOf(rs.getString(3)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Vendor->Platform mappings
            sql = "SELECT m.VENDOR_ID, mp.PLATFORM_ID FROM MODEL m INNER JOIN MODEL_PLATFORM mp ON mp.MODEL_ID=m.ID GROUP BY m.VENDOR_ID, mp.PLATFORM_ID";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long vendorId = rs.getLong(1);
                long platformId = rs.getLong(2);
                Set<Long> platformIds = td.platformIdsByVendorId.get(vendorId);
                if (platformIds == null) {
                    platformIds = new HashSet<Long>();
                    td.platformIdsByVendorId.put(vendorId, platformIds);
                }
                platformIds.add(platformId);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Model->Platform mappings
            sql = "SELECT MODEL_ID, PLATFORM_ID FROM MODEL_PLATFORM";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long modelId = rs.getLong(1);
                long platformId = rs.getLong(2);
                Set<Long> platformIds = td.platformIdsByModelId.get(modelId);
                if (platformIds == null) {
                    platformIds = new HashSet<Long>();
                    td.platformIdsByModelId.put(modelId, platformIds);
                }
                platformIds.add(platformId);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // PublicationType->Platform mappings
            sql = "SELECT PUBLICATION_TYPE_ID, PLATFORM_ID FROM PUBLICATION_TYPE_PLATFORM";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long publicationTypeId = rs.getLong(1);
                long platformId = rs.getLong(2);
                Set<Long> platformIds = td.platformIdsByPublicationTypeId.get(publicationTypeId);
                if (platformIds == null) {
                    platformIds = new HashSet<Long>();
                    td.platformIdsByPublicationTypeId.put(publicationTypeId, platformIds);
                }
                platformIds.add(platformId);
            }
            DbUtils.closeQuietly(null, pst, rs);
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done Loading Additional Transient Dependencies");
        }
    }
}
