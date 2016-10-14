package com.adfonic.data.cache.ecpm.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.domain.cache.DomainCacheLoader;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.service.WeightageServices;
import com.adfonic.domain.cache.service.WeightageServicesImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-ecpm-cache-loader-context.xml" })
public class SelfLoadingTest {

    private static final transient Logger LOG = Logger.getLogger(SelfLoadingTest.class.getName());

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EcpmDataCacheLoader ecpmDataCacheLoader;
    private AdserverDataCache ecpmDataCache;

    @Autowired
    private DomainCacheLoader domainCacheLoader;

    private WeightageServices weightageServices = new WeightageServicesImpl();

    @Ignore
    @Test
    public void compareEcpmTest() throws Exception {
        /*
        DomainCache domainCache = domainCacheLoader.loadDomainCache();

        TransientData td = loadCategories();
        CreativeLoader creativeLoader = new CreativeLoader(dataSource, new CampaignAudienceLoader());
        CreativeService creativeService = creativeLoader.loadCreatives(td, new AdfonicStopWatch());

        Map<Long, CreativeDto> creatives = new HashMap<>();
        for (CreativeDto creativeDto : creativeService.getAllCreatives())
            creatives.put(creativeDto.getId(), creativeDto);

        Set<Long> publisherIds = new HashSet<Long>();
        AdSpaceLoader adSpaceLoader = new AdSpaceLoader(dataSource);
        AdSpaceService adSpaceService = adSpaceLoader.loadAdspaces(td, ShardMode.all, publisherIds, false, new AdfonicStopWatch());
        Map<Long, AdSpaceDto> adSpaces = new HashMap<>();
        for (AdSpaceDto adSpaceDto : adSpaceService.getAllAdSpaces())
            adSpaces.put(adSpaceDto.getId(), adSpaceDto);

        loadCampaignRunningTargetCtr(weightageServices, creativeService.getAllCreatives());
        loadCampaignRunningTargetCvr(weightageServices, creativeService.getAllCreatives());

        for (AdSpaceDto adSpace : adSpaceService.getAllAdSpaces()) {
            for (CreativeDto creativeDto : creativeService.getAllCreatives()) {
                for (PlatformDto platformDto : domainCache.getPlatforms()) {

                    // Old Computation
                    EcpmData result1 = new EcpmData();
                    weightageServices.computeEcpmInfo(adSpace, creativeDto, platformDto, 1, BigDecimal.ZERO, result1.getEcpmInfo());

                    // New Computation
                    EcpmDataRepository repositoryEcpm = new EcpmRepositoryIncremental();
                    AdfonicStopWatch adfonicStopWatch = new AdfonicStopWatch();
                    // This map has to contain all the AdSpaces we want to load in the cache

                    ecpmDataCacheLoader.loadECPMData(repositoryEcpm, adfonicStopWatch, creatives, adSpaces);
                    ecpmDataCache = new AdserverDataCacheImpl(repositoryEcpm);
                    EcpmData result2 = new EcpmData();
                    ecpmDataCache.computeEcpmInfo(adSpace, creativeDto, platformDto, 1, BigDecimal.ZERO, result2.getEcpmInfo());

                    // Comparison
                    assertEquals(result1.getEcpmInfo().getExpectedRevenue(), result2.getEcpmInfo().getExpectedRevenue(), 0);
                    assertEquals(result1.getEcpmInfo().getExpectedSettlementPrice(), result2.getEcpmInfo().getExpectedSettlementPrice(), 0);
                    assertEquals(result1.getEcpmInfo().getExpectedProfit(), result2.getEcpmInfo().getExpectedProfit(), 0);
                    assertEquals(result1.getEcpmInfo().getBidPrice(), result2.getEcpmInfo().getBidPrice(), 0);
                    assertEquals(result1.getEcpmInfo().getWeight(), result2.getEcpmInfo().getWeight(), 0);
                    assertEquals(result1.getEcpmInfo().getWinningProbability(), result2.getEcpmInfo().getWinningProbability(), 0);
                }
            }
        }
        */

    }

    private void loadCampaignRunningTargetCtr(WeightageServices weightageServices, CreativeDto[] creatives) throws SQLException {

        //First create a String for SQL query for all campaign for which we need to query the country weighting
        Set<Long> allCampaigns = new HashSet<Long>();
        for (CreativeDto oneCreative : creatives) {
            allCampaigns.add(oneCreative.getCampaign().getId());
        }

        String commaSeparatedCampaignIds = StringUtils.join(allCampaigns, ',');
        final String LOAD_RUNNING_CAMPAIGN_TARGET_CTR_SQL = "SELECT CAMPAIGN_ID, TARGET_CTR, CURRENT_CTR FROM RUNNING_CAMPAIGN_TARGET_CTR WHERE CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ")";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_RUNNING_CAMPAIGN_TARGET_CTR_SQL);
        }
        Connection conn = null;
        long campaignId;
        double targetCtr;
        double currentCtr;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(LOAD_RUNNING_CAMPAIGN_TARGET_CTR_SQL);
            rs = pst.executeQuery();
            while (rs.next()) {
                campaignId = rs.getLong("CAMPAIGN_ID");
                targetCtr = rs.getDouble("TARGET_CTR");
                currentCtr = rs.getDouble("CURRENT_CTR");
                weightageServices.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }
    }

    private void loadCampaignRunningTargetCvr(WeightageServices weightageServices, CreativeDto[] creatives) throws SQLException {

        //First create a String for SQL query for all campaign for which we need to query the country weighting
        Set<Long> allCampaigns = new HashSet<Long>();
        for (CreativeDto oneCreative : creatives) {
            allCampaigns.add(oneCreative.getCampaign().getId());
        }
        String commaSeparatedCampaignIds = StringUtils.join(allCampaigns, ',');
        final String LOAD_RUNNING_CAMPAIGN_TARGET_CVR_SQL = "SELECT CAMPAIGN_ID, TARGET_CVR, CURRENT_CVR FROM RUNNING_CAMPAIGN_TARGET_CVR WHERE CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ")";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_RUNNING_CAMPAIGN_TARGET_CVR_SQL);
        }
        Connection conn = null;
        long campaignId;
        double targetCvr;
        double currentCvr;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            pst = conn.prepareStatement(LOAD_RUNNING_CAMPAIGN_TARGET_CVR_SQL);
            rs = pst.executeQuery();
            while (rs.next()) {
                campaignId = rs.getLong("CAMPAIGN_ID");
                targetCvr = rs.getDouble("TARGET_CVR");
                currentCvr = rs.getDouble("CURRENT_CVR");
                weightageServices.addCampaignRunningCvr(campaignId, targetCvr, currentCvr);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }
    }

    /*
        private TransientData loadCategories() throws SQLException {
            TransientData td = new TransientData();
            Set<Long> allCategoryIds = new HashSet<Long>();
            Map<Long, Set<Long>> childIdsByParentId = new HashMap<Long, Set<Long>>();

            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                conn = dataSource.getConnection();

                String sql = "SELECT ID, PARENT_ID, CHANNEL_ID FROM CATEGORY";
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer(sql);
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
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Expanding Categories");
            }
            for (long categoryId : allCategoryIds) {
                Set<Long> expanded = new HashSet<Long>();
                expandCategory(categoryId, expanded, childIdsByParentId);
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("Category id=" + categoryId + " expanded to " + expanded.size() + " categor" + (expanded.size() == 1 ? "y" : "ies"));
                }
                td.expandedCategoryIdsByCategoryId.put(categoryId, expanded);
            }
            return td;
        }
    */
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

}
