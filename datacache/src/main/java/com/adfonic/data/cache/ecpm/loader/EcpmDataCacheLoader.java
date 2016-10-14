package com.adfonic.data.cache.ecpm.loader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.datacache.PlatformBidTypePublicationKey;
import com.adfonic.data.cache.ecpm.key.AdSpaceCreativeKey;
import com.adfonic.data.cache.ecpm.key.CampaignCountryKey;
import com.adfonic.data.cache.ecpm.key.PlatformCreativeKey;
import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;

public class EcpmDataCacheLoader extends DBLoader {

    private static final transient Logger LOG = Logger.getLogger(EcpmDataCacheLoader.class.getName());
    private final DataSource ecpmDataSource;

    public EcpmDataCacheLoader(DataSource ecpmDataSource) {
        this.ecpmDataSource = ecpmDataSource;
    }

    public void loadECPMData(EcpmDataRepository repository, AdfonicStopWatch adfonicStopWatch, Map<Long, CreativeDto> creatives, Map<Long, AdSpaceDto> adSpaces)
            throws SQLException {
        //10/11/2012 UbertoB
        // this code has originally been copy-pasted from AdserverDomainCacheLoader.loadECPMConcurrently().
        // Any change to the logic there should be reflected here, until we remove loadECPM from domainCache

        if (ecpmDataSource == null) {
            throw new RuntimeException("EcpmDataSource not loaded!");
        }
        loadExpectedStats(repository, creatives, adSpaces, adfonicStopWatch);
        loadRunningCampaignCvr(repository, creatives.values(), adfonicStopWatch);
        loadRunningCreativeCvr(repository, creatives, adfonicStopWatch);
        loadAdspaceRunningCtr(repository, adSpaces, adfonicStopWatch);
        loadCampaignCountryWeighting(repository, creatives.values(), adfonicStopWatch);
        loadCreativeWeightedCtrIndexes(repository, creatives, adfonicStopWatch);
        loadPublicationWeightedCvrIndexes(repository, adSpaces.values(), adfonicStopWatch);
        loadCampaignRunningTargetCtr(repository, creatives.values(), adfonicStopWatch);
        loadCampaignRunningTargetCvr(repository, creatives.values(), adfonicStopWatch);
        loadCampaignMarginRecomendations(repository, adfonicStopWatch);

    }

    public void loadDefaultSystemVariables(final EcpmDataRepository repository, AdfonicStopWatch adfonicStopWatch) throws java.sql.SQLException {

        String taskName = "Default System Variables";
        //String commaSeparatedAdSpaceIds = StringUtils.join(td.allAdSpacesById.keySet(), ',');
        // I tried filtering this query with:
        // WHERE CREATIVE_ID IN (...) AND AD_SPACE_ID IN (...)
        // ...but that ended up being considerably slower than just loading the whole
        // damned friggin' table, and filtering as we fetch data (see below).

        //We dont need to query based on creatives we just need to base it on the adspace id
        //String sql = "SELECT CREATIVE_ID, AD_SPACE_ID, ECPM FROM RUNNING_ECPM where AD_SPACE_ID IN ("+commaSeparatedAdSpaceIds+")";
        String sql = "SELECT system_variable_id, system_variable_name, system_variable_int, system_variable_decimal FROM system_variable";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                SystemVariable oneSystemVariable = null;
                if (rs.getObject("system_variable_int") != null) {
                    oneSystemVariable = new SystemVariable();
                    oneSystemVariable.setName(rs.getString("system_variable_name"));
                    oneSystemVariable.setIntValue(rs.getInt("system_variable_int"));
                    oneSystemVariable.setId(rs.getLong("system_variable_id"));
                }
                if (rs.getObject("system_variable_decimal") != null) {
                    oneSystemVariable = new SystemVariable();
                    oneSystemVariable.setName(rs.getString("system_variable_name"));
                    oneSystemVariable.setDoubleValue(rs.getDouble("system_variable_decimal"));
                    oneSystemVariable.setId(rs.getLong("system_variable_id"));
                }
                LOG.fine(" var bame : + " + oneSystemVariable.getName() + " - value: " + oneSystemVariable.getDoubleValue());
                repository.addSystemVariable(oneSystemVariable);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);
    }

    private void loadPublicationWeightedCvrIndexes(final EcpmDataRepository repository, Collection<AdSpaceDto> adSpaces, AdfonicStopWatch adfonicStopWatch)
            throws java.sql.SQLException {

        String taskName = "Publication Weighted CVR indexes";
        final Set<Long> allPublicationsForShard = new HashSet<Long>(100);
        for (AdSpaceDto oneAdspace : adSpaces) {
            allPublicationsForShard.add(oneAdspace.getPublication().getId());
        }
        String sql = "SELECT PUBLICATION_ID, PLATFORM_ID,BID_TYPE,ADJUSTED_WEIGHTED_CVR_INDEX FROM ADJUSTED_WEIGHTED_CVR_INDEX";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long publicationId = rs.getLong("PUBLICATION_ID");
                if (!allPublicationsForShard.contains(publicationId)) {
                    return false; // publication isn't relevant
                }
                BidType bidType = BidType.valueOf(rs.getString("BID_TYPE"));
                long platformId = rs.getLong("PLATFORM_ID");
                double publicationWeightedCvrIndex = rs.getDouble("ADJUSTED_WEIGHTED_CVR_INDEX");
                repository.addPublicationWeightedCvrIndex(new PlatformBidTypePublicationKey(platformId, bidType, publicationId), publicationWeightedCvrIndex);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }

    private void loadCreativeWeightedCtrIndexes(final EcpmDataRepository repository, final Map<Long, CreativeDto> creatives, AdfonicStopWatch adfonicStopWatch)
            throws java.sql.SQLException {

        String taskName = "Creative Weighted CTR indexes";
        String sql = "SELECT CREATIVE_ID, PLATFORM_ID,BID_TYPE,ADJUSTED_WEIGHTED_CTR_INDEX FROM ADJUSTED_WEIGHTED_CTR_INDEX";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long creativeId = rs.getLong("CREATIVE_ID");
                CreativeDto creative = creatives.get(creativeId);
                if (creative == null) {
                    return false; // creative isn't relevant
                }
                BidType bidType = BidType.valueOf(rs.getString("BID_TYPE"));
                if (!bidType.equals(creative.getCampaign().getCurrentBid().getBidType())) {
                    //If Bid type do not match then don't cache this entry
                    return false;
                }
                long platformId = rs.getLong("PLATFORM_ID");
                double creativeWeightedCtrIndex = rs.getDouble("ADJUSTED_WEIGHTED_CTR_INDEX");
                repository.addCreativeWeightedCtrIndex(new PlatformCreativeKey(platformId, creativeId), creativeWeightedCtrIndex);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }

    private void loadCampaignCountryWeighting(final EcpmDataRepository repository, Collection<CreativeDto> creatives, AdfonicStopWatch adfonicStopWatch) throws SQLException {

        String taskName = "Country Weighting";
        //First create a String for SQL query for all campaign for which we need to query the country weighting
        Set<Long> allCampaigns = new HashSet<Long>();
        for (CreativeDto oneCreative : creatives) {
            allCampaigns.add(oneCreative.getCampaign().getId());
        }
        String commaSeparatedCampaignIds = StringUtils.join(allCampaigns, ',');
        final String sql = "SELECT CAMPAIGN_ID, COUNTRY_ID, WEIGHTING FROM CAMPAIGN_COUNTRY_WEIGHTING WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long campaignId = rs.getLong("CAMPAIGN_ID");
                long countryId = rs.getLong("COUNTRY_ID");
                double weight = rs.getDouble("WEIGHTING");
                repository.addCampaignCountryWeight(new CampaignCountryKey(campaignId, countryId), weight);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }

    private void loadAdspaceRunningCtr(final EcpmDataRepository repository, final Map<Long, AdSpaceDto> adspaces, AdfonicStopWatch adfonicStopWatch) throws java.sql.SQLException {
        String taskName = "Adspace Running CTR";
        String sql = "SELECT AD_SPACE_ID, CTR FROM RUNNING_CTR";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long adSpaceId = rs.getLong("AD_SPACE_ID");
                if (adspaces.get(adSpaceId) == null) {
                    return false;
                }
                double ctr = rs.getDouble("CTR");
                repository.addAdspaceCtr(adSpaceId, ctr);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);
    }

    private void loadRunningCreativeCvr(final EcpmDataRepository repository, final Map<Long, CreativeDto> creatives, AdfonicStopWatch adfonicStopWatch)
            throws java.sql.SQLException {
        String taskName = "Running CVR";
        String sql = "SELECT CREATIVE_ID, CVR FROM RUNNING_CREATIVE_CVR";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long creativeId = rs.getLong("CREATIVE_ID");
                if (creatives.get(creativeId) == null) {
                    return false; // creative isn't relevant
                }

                double expectedCvr = rs.getDouble("CVR");
                repository.addCreativeCvr(creativeId, expectedCvr);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }

    private void loadRunningCampaignCvr(final EcpmDataRepository repository, Collection<CreativeDto> creatives, AdfonicStopWatch adfonicStopWatch) throws java.sql.SQLException {
        //collect all the valid campaigns

        final Set<Long> allCampaigns = new HashSet<Long>();
        for (CreativeDto oneCreative : creatives) {
            allCampaigns.add(oneCreative.getCampaign().getId());
        }

        String taskName = "Running CVR for  " + allCampaigns.size() + " Campaigns";
        String sql = "SELECT CAMPAIGN_ID, CVR FROM RUNNING_CAMPAIGN_CVR";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long campaignId = rs.getLong("CAMPAIGN_ID");
                if (!allCampaigns.contains(campaignId)) {
                    return false; // campaign isn't relevant
                }

                double expectedCvr = rs.getDouble("CVR");
                repository.addCampaignCvr(campaignId, expectedCvr);
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }

    private void loadCampaignRunningTargetCtr(final EcpmDataRepository repository, final Collection<CreativeDto> creatives, final AdfonicStopWatch adfonicStopWatch)
            throws SQLException {

        String taskName = "Campaign Target CTR";

        //First create a String for SQL query for all campaign for which we need to query the country weighting
        Set<Long> allCampaigns = new HashSet<Long>();
        for (CreativeDto oneCreative : creatives) {
            allCampaigns.add(oneCreative.getCampaign().getId());
        }
        String commaSeparatedCampaignIds = StringUtils.join(allCampaigns, ',');
        final String sql = "SELECT CAMPAIGN_ID, TARGET_CTR, CURRENT_CTR FROM RUNNING_CAMPAIGN_TARGET_CTR WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(sql);
        }

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {

                repository.addCampaignRunningCtr(rs.getLong("CAMPAIGN_ID"), rs.getDouble("TARGET_CTR"), rs.getDouble("CURRENT_CTR"));
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

        //
        //        adfonicStopWatch.start("Loading Campaign Target CTR");
        //        LOG.fine("Loading Campaign Target CTR");
        //
        //        //First create a String for SQL query for all campaign for which we need to query the country weighting
        //        Set<Long> allCampaigns = new HashSet<Long>();
        //        for (CreativeDto oneCreative : creatives) {
        //            allCampaigns.add(oneCreative.getCampaign().getId());
        //        }
        //        String commaSeparatedCampaignIds = StringUtils.join(allCampaigns, ',');
        //        final String LOAD_RUNNING_CAMPAIGN_TARGET_CTR_SQL = "SELECT CAMPAIGN_ID, TARGET_CTR, CURRENT_CTR FROM RUNNING_CAMPAIGN_TARGET_CTR WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")";
        //        if (LOG.isLoggable(Level.FINER)) {
        //            LOG.finer(LOAD_RUNNING_CAMPAIGN_TARGET_CTR_SQL);
        //        }
        //        Connection conn = null;
        //        long campaignId;
        //        double targetCtr;
        //        double currentCtr;
        //        PreparedStatement pst = null;
        //        ResultSet rs = null;
        //        try {
        //            conn = ecpmDataSource.getConnection();
        //            pst = conn.prepareStatement(LOAD_RUNNING_CAMPAIGN_TARGET_CTR_SQL);
        //            rs = pst.executeQuery();
        //            while (rs.next()) {
        //                campaignId = rs.getLong("CAMPAIGN_ID");
        //                targetCtr = rs.getDouble("TARGET_CTR");
        //                currentCtr = rs.getDouble("CURRENT_CTR");
        //                repository.addCampaignRunningCtr(campaignId, targetCtr, currentCtr);
        //            }
        //        } finally {
        //            DbUtils.closeQuietly(conn, pst, rs);
        //            adfonicStopWatch.stop("Loading Campaign Target CTR");
        //        }
    }

    private void loadCampaignRunningTargetCvr(final EcpmDataRepository repository, Collection<CreativeDto> creatives, AdfonicStopWatch adfonicStopWatch) throws SQLException {

        String taskName = "Campaign Target CVR";

        //First create a String for SQL query for all campaign for which we need to query the country weighting
        Set<Long> allCampaigns = new HashSet<Long>();
        for (CreativeDto oneCreative : creatives) {
            allCampaigns.add(oneCreative.getCampaign().getId());
        }
        String commaSeparatedCampaignIds = StringUtils.join(allCampaigns, ',');
        final String sql = "SELECT CAMPAIGN_ID, TARGET_CVR, CURRENT_CVR FROM RUNNING_CAMPAIGN_TARGET_CVR WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(sql);
        }

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {

                repository.addCampaignRunningCvr(rs.getLong("CAMPAIGN_ID"), rs.getDouble("TARGET_CVR"), rs.getDouble("CURRENT_CVR"));
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }

    private void loadExpectedStats(final EcpmDataRepository repository, final Map<Long, CreativeDto> creatives, final Map<Long, AdSpaceDto> adSpaces,
            AdfonicStopWatch adfonicStopWatch) throws java.sql.SQLException {

        String taskName = "Expected Stats Data";
        //String commaSeparatedAdSpaceIds = StringUtils.join(td.allAdSpacesById.keySet(), ',');
        // I tried filtering this query with:
        // WHERE CREATIVE_ID IN (...) AND AD_SPACE_ID IN (...)
        // ...but that ended up being considerably slower than just loading the whole
        // damned friggin' table, and filtering as we fetch data (see below).

        //We dont need to query based on creatives we just need to base it on the adspace id
        //String sql = "SELECT CREATIVE_ID, AD_SPACE_ID, ECPM FROM RUNNING_ECPM where AD_SPACE_ID IN ("+commaSeparatedAdSpaceIds+")";
        String sql = "SELECT CREATIVE_ID, AD_SPACE_ID, EXPECTED_CTR, EXPECTED_CVR, EXPECTED_RGR FROM EXPECTED_STATS";

        final ExpectedStatsDto defaultExpectedStats = new ExpectedStatsDto(0.0, 0.0, 0.0);

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {

            @Override
            public boolean read(ResultSet rs) throws SQLException {
                long adSpaceId = rs.getLong("AD_SPACE_ID");
                if (adSpaces.get(adSpaceId) == null) {
                    return false; // adspace isn't relevant
                }
                long creativeId = rs.getLong("CREATIVE_ID");
                if (creatives.get(creativeId) == null) {
                    return false; // creative isn't relevant
                }

                double expectedCtr = rs.getDouble("EXPECTED_CTR");
                double expectedCVr = rs.getDouble("EXPECTED_CVR");
                double expectedRgr = rs.getDouble("EXPECTED_RGR");
                if (expectedCtr == 0.0 && expectedCVr == 0.0 && expectedRgr == 0.0) {
                    //If all are zero then use the same default instance
                    //currently out of 1.7 million records 1.1 millions have all zeros
                    repository.addExpectedStats(new AdSpaceCreativeKey(adSpaceId, creativeId), defaultExpectedStats);
                } else {
                    repository.addExpectedStats(new AdSpaceCreativeKey(adSpaceId, creativeId), new ExpectedStatsDto(expectedCtr, expectedCVr, expectedRgr));
                }
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);

    }
    
    private void loadCampaignMarginRecomendations(final EcpmDataRepository repository, AdfonicStopWatch adfonicStopWatch) throws java.sql.SQLException {
        String taskName = "Campaign Margin Recomendations";
        
        String sql = "SELECT CAMPAIGN_ID, MARGIN FROM adfonic.CAMPAIGN_MARGIN_REC";

        ReadFromRecordSet readBlock = new ReadFromRecordSet() {
            @Override
            public boolean read(ResultSet rs) throws SQLException {
                repository.addCampaignMarginRecommendation(rs.getLong("CAMPAIGN_ID"), rs.getDouble("MARGIN"));
                return true;
            }
        };

        loadEntitiesFromDb(this.ecpmDataSource, adfonicStopWatch, taskName, sql, readBlock);
    }

}
