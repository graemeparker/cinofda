package com.adfonic.domain.cache;

import static com.adfonic.domain.cache.ext.util.DbUtil.nullableDate;
import static com.adfonic.domain.cache.ext.util.DbUtil.nullableLong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.Publication;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignBidDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDataFeeDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublicationDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherDto;
import com.adfonic.domain.cache.dto.datacollector.publication.PublisherRevShareDto;

/**
 * DataCollector Domain Cache Loader
 */
public class DataCollectorDomainCacheLoader {

    private static final transient Logger LOG = Logger.getLogger(DataCollectorDomainCacheLoader.class.getName());

    // Construct JDBC SQL versions that quote the sets of statuses
    private static final String QUOTED_PUBLICATION_STATUSES;
    private static final String QUOTED_CAMPAIGN_STATUSES;

    static {
        // Statuses for Publications that have been active at one point or another.
        // NOTE: we don't need to bother with PENDING publications, since we'll
        // never see an AdEvent logged for those.
        Set<Publication.Status> publicationStatuses = new HashSet<Publication.Status>();
        publicationStatuses.add(Publication.Status.ACTIVE);
        QUOTED_PUBLICATION_STATUSES = "'" + StringUtils.join(publicationStatuses, "','") + "'";

        // Statuses for Campaigns that have been active at one point or another.
        // The reason we're so liberal here is that datacollector may process
        // events for campaigns that were active at the time of the event, but
        // have since stopped.  This is totally legit.  For example, there may
        // be install tracking events that come in up to two weeks after a
        // campaign has stopped.
        // The other scenario is that if we ever have a nasty backlog of events,
        // datacollector may be processing them well after the fact.  We don't
        // want to "miss" budget-updating events just because a campaign is
        // stopped by the time datacollector gets to it.
        // TODO: consider adding Campaign.statusChangedDate so we can limit
        // the scope of this query a little more intelligently/efficiently.
        Set<Campaign.Status> campaignStatuses = new HashSet<Campaign.Status>();
        campaignStatuses.add(Campaign.Status.ACTIVE);
        campaignStatuses.add(Campaign.Status.COMPLETED);
        campaignStatuses.add(Campaign.Status.PAUSED);
        campaignStatuses.add(Campaign.Status.STOPPED);
        QUOTED_CAMPAIGN_STATUSES = "'" + StringUtils.join(campaignStatuses, "','") + "'";
    }

    private final DataSource dataSource;

    public DataCollectorDomainCacheLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public synchronized DataCollectorDomainCache loadDataCollectorDomainCache() throws Exception {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Loading the DataCollectorDomainCache");
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DataCollectorDomainCacheImpl dataCollectorDomainCache = new DataCollectorDomainCacheImpl();
        loadCampaigns(dataCollectorDomainCache);
        loadPublications(dataCollectorDomainCache);

        stopWatch.stop();
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Loading DataCollectorDomainCache took " + stopWatch);
        }

        return dataCollectorDomainCache;
    }

    private void loadCampaigns(DataCollectorDomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Campaigns");
        }

        Map<Long, AdvertiserDto> advertisersById = new HashMap<Long, AdvertiserDto>();
        Map<Long, com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto> companiesById = new HashMap<Long, com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto>();
        Map<Long, CampaignDto> campaignsById = new HashMap<Long, CampaignDto>();
        ConcurrentHashMap<Long, Long> campaignsAudiences = new ConcurrentHashMap<Long, Long>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            // SC-511
            String campaignAudienceSql = "select CAMPAIGN_ID  from CAMPAIGN_AUDIENCE ";
            pst = conn.prepareStatement(campaignAudienceSql);
            rs = pst.executeQuery();
            while (rs.next()) {
                campaignsAudiences.put(rs.getLong("CAMPAIGN_ID"), rs.getLong("CAMPAIGN_ID"));
            }
            // -------

            String sql = "SELECT "
                    // Campaign
                    + "c0.ID, c0.INSTALL_TRACKING_ENABLED, c0.INSTALL_TRACKING_ADX_ENABLED, c0.CONVERSION_TRACKING_ENABLED, c0.APPLICATION_ID, c0.PRIVATE_MARKET_PLACE_DEAL_ID"
                    // Advertiser
                    + ", a0.ID, a0.ACCOUNT_ID"
                    // Company
                    + ", c1.ID, c1.DISCOUNT, IF(c1.POST_PAY_ACTIVATION_DATE IS NULL,FALSE,TRUE), c1.TAXABLE_ADVERTISER, c1.DEFAULT_TIME_ZONE, c1.BACKFILL"
                    // DirectCost
                    + " , dc.ID, dc.DIRECT_COST, dc.START_DATE, dc.END_DATE "
                    // CampaignBid
                    + ", c2.ID, c2.BID_TYPE, c2.AMOUNT, c2.START_DATE, c2.END_DATE, c2.MAXIMUM,c2.BID_MODEL_TYPE" //
                    + " FROM CAMPAIGN c0"
                    + " JOIN ADVERTISER a0 ON a0.ID=c0.ADVERTISER_ID" //
                    + " JOIN COMPANY c1 ON c1.ID=a0.COMPANY_ID" //
                    + " LEFT OUTER JOIN CAMPAIGN_BID c2 ON c2.ID=c0.CURRENT_BID_ID"
                    + " LEFT OUTER JOIN COMPANY_DIRECT_COST dc ON dc.ID=c1.CURRENT_COMPANY_DIRECT_COST_ID"
                    + " WHERE c0.STATUS IN (" + QUOTED_CAMPAIGN_STATUSES + ")"
		+ " AND (c0.DEACTIVATION_DATE IS NULL or c0.DEACTIVATION_DATE > now() - INTERVAL 30 DAY)";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;

                // Campaign
                CampaignDto campaign = new CampaignDto();
                campaign.setId(rs.getLong(idx++));
                campaign.setInstallTrackingEnabled(rs.getBoolean(idx++));
                campaign.setInstallTrackingAdXEnabled(rs.getBoolean(idx++));
                campaign.setConversionTrackingEnabled(rs.getBoolean(idx++));
                campaign.setApplicationID(rs.getString(idx++));
                campaign.setPMP(rs.getObject(idx++) != null);
                // SC-511
                campaign.setHasAudience(campaignsAudiences.contains(campaign.getId()));
                // ------
                // SC-524
                campaign.setBehavioural(campaignsAudiences.contains(campaign.getId()));
                // ------
                campaignsById.put(campaign.getId(), campaign);

                // Advertiser
                long advertiserId = rs.getLong(idx++);
                AdvertiserDto advertiser = advertisersById.get(advertiserId);
                if (advertiser == null) {
                    advertiser = new AdvertiserDto();
                    advertiser.setId(advertiserId);
                    advertiser.setAccountId(rs.getLong(idx++));
                    advertisersById.put(advertiser.getId(), advertiser);

                    // Company
                    long companyId = rs.getLong(idx++);
                    com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto company = companiesById.get(companyId);
                    if (company == null) {
                        company = new com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto();
                        company.setId(companyId);
                        company.setDiscount(rs.getBigDecimal(idx++));
                        company.setPostPay(rs.getBoolean(idx++));
                        company.setTaxableAdvertiser(rs.getBoolean(idx++));
                        company.setDefaultTimeZoneID(rs.getString(idx++));
                        company.setBackfill(rs.getBoolean(idx++));
                        
                        // DirectCost
                        Long currentCompanyDirectCostId =  nullableLong(rs, idx++);
                        if(currentCompanyDirectCostId != null) {
                        	CompanyDirectCostDto directCost = new CompanyDirectCostDto();
                        	directCost.setId(currentCompanyDirectCostId);
                        	directCost.setDirectCost(rs.getBigDecimal(idx++));
                        	directCost.setStartDate(rs.getTimestamp(idx++));
                        	directCost.setEndDate(rs.getTimestamp(idx++));
                        	company.setDirectCost(directCost);
                        } else {
                        	idx += 3;
                        }
                        
                        companiesById.put(company.getId(), company);
                    } else {
                        idx += 9;
                    }
                    advertiser.setCompany(company);
                } else {
                    idx += 11;
                }
                campaign.setAdvertiser(advertiser);

                // House ads have no current bid, so be careful (we did a LEFT OUTER JOIN)
                Long currentBidId = nullableLong(rs, idx++);
                if (currentBidId != null) {
                    // CampaignBid - we assume that they're not shared across campaigns, which
                    // means we can just blindly create a new instance without worrying about
                    // efficiency here (as opposed to the advertisersById and companiesById
                    // and what not above).
                    CampaignBidDto currentBid = new CampaignBidDto();
                    currentBid.setId(currentBidId);
                    currentBid.setBidType(BidType.valueOf(rs.getString(idx++)));
                    currentBid.setAmount(rs.getBigDecimal(idx++));
                    currentBid.setStartDate(rs.getTimestamp(idx++));
                    currentBid.setEndDate(rs.getTimestamp(idx++));
                    currentBid.setMaximum(rs.getBoolean(idx++));
                    currentBid.setBidModelType(BidModelType.valueOf(rs.getString("BID_MODEL_TYPE")));
                    campaign.setCurrentBid(currentBid);
                } else {
                    idx += 5;
                }

                cache.campaignsById.put(campaign.getId(), campaign);
            }
            DbUtils.closeQuietly(null, pst, rs);

            String commaSeparatedCampaignIds = StringUtils.join(cache.campaignsById.keySet(), ',');
            String commaSeparatedCompanyIds = StringUtils.join(companiesById.keySet(), ',');
            loadCampaignDataFees(conn, campaignsById, commaSeparatedCampaignIds);
            loadCampaignRmAdServingFee(conn, campaignsById, commaSeparatedCampaignIds);
            loadCampaignTradingDeskMargin(conn, campaignsById, commaSeparatedCampaignIds);
            loadCompanyMediaCostMargin(conn, companiesById, commaSeparatedCompanyIds);
            loadCampaignAgencyDiscount(conn, campaignsById, commaSeparatedCampaignIds);
            loadCompanyMarginShareDSP(conn, companiesById, commaSeparatedCompanyIds);
            loadHistoricalCompanyDirectCost(conn, companiesById, commaSeparatedCompanyIds);

            // Campaign bid history
            sql = "SELECT CAMPAIGN_ID, ID, BID_TYPE, AMOUNT, START_DATE, END_DATE FROM CAMPAIGN_BID WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds
                    + ") ORDER BY START_DATE DESC";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                CampaignDto campaign = cache.campaignsById.get(rs.getLong(idx++));
                CampaignBidDto campaignBid = new CampaignBidDto();
                campaignBid.setId(rs.getLong(idx++));
                campaignBid.setBidType(BidType.valueOf(rs.getString(idx++)));
                campaignBid.setAmount(rs.getBigDecimal(idx++));
                campaignBid.setStartDate(rs.getTimestamp(idx++));
                campaignBid.setEndDate(rs.getTimestamp(idx++));
                campaign.getHistoricalBids().add(campaignBid);
            }
            DbUtils.closeQuietly(null, pst, rs);
            
            
            
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.campaignsById.size() + " Campaigns");
        }
    }

    private void loadCampaignRmAdServingFee(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.fine("Loading Campaign RM AD Servind Fee");
        final String LOAD_ALL_CAMPAIGN_RM_AD_SERVING_FEE_SQL = "SELECT CRASF.CAMPAIGN_ID, CRASF.RM_AD_SERVING_FEE FROM CAMPAIGN_RM_AD_SERVING_FEE CRASF WHERE CRASF.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and CRASF.START_DATE <= now() and ( CRASF.END_DATE >= now() or CRASF.END_DATE is null)";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_ALL_CAMPAIGN_RM_AD_SERVING_FEE_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_RM_AD_SERVING_FEE_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                campaign.setRmAdServingFee(rs.getBigDecimal("RM_AD_SERVING_FEE"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignTradingDeskMargin(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.fine("Loading Campaign Trading Desk margin");
        final String LOAD_ALL_CAMPAIGN_TRADING_DESK_MARGIN_SQL = "SELECT CTDM.CAMPAIGN_ID, CTDM.TRADING_DESK_MARGIN FROM CAMPAIGN_TRADING_DESK_MARGIN CTDM WHERE CTDM.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and CTDM.START_DATE <= now() and ( CTDM.END_DATE >= now() or CTDM.END_DATE is null)";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_ALL_CAMPAIGN_TRADING_DESK_MARGIN_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_TRADING_DESK_MARGIN_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                campaign.setTradingDeskMargin(rs.getBigDecimal("TRADING_DESK_MARGIN"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignDataFees(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.fine("Loading Campaign Data Fee and history");
        // This query is not used here, as in AD_EVENT_LOG we'll only store CAMPAIGN_DATA_FEE_ID
        // With this query you get the data wholesale and data retail used per vendor for required data fees.
        //        final String LOAD_ALL_CAMPAIGN_DATA_FEE_SQL = "SELECT CDF.CAMPAIGN_ID AS CAMPAIGN_ID, CDF.ID AS DATA_FEE_ID, CDF.AMOUNT AS DATA_FEE, CDF.START_DATE AS START_DATE, CDF.END_DATE AS END_DATE, ADF.DATA_RETAIL AS DATA_RETAIL, ADF.DATA_WHOLESALE AS DATA_WHOLESALE, DMPA.DMP_VENDOR_ID AS VENDOR_ID" +
        //                                                        " FROM CAMPAIGN_DATA_FEE CDF" +
        //                                                        " JOIN AUDIENCE_DATA_FEE ADF ON ADF.CAMPAIGN_DATA_FEE_ID = CDF.ID" +
        //                                                        " JOIN CAMPAIGN_AUDIENCE CA ON ADF.CAMPAIGN_AUDIENCE_ID = CA.ID" +
        //                                                        " JOIN AUDIENCE A ON CA.AUDIENCE_ID = A.ID" +
        //                                                        " JOIN DMP_AUDIENCE DMPA ON DMPA.AUDIENCE_ID = A.ID" +
        //                                                          " WHERE CDF.CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")" +
        //                                                          " and ADF.IS_MAXIMUM_FOR_VENDOR = 1" +
        //                                                        " ORDER BY CAMPAIGN_DATA_FEE_ID, START_DATE DESC";

        final String LOAD_ALL_CAMPAIGN_DATA_FEE_SQL = "SELECT CDF.CAMPAIGN_ID AS CAMPAIGN_ID, CDF.ID AS DATA_FEE_ID, CDF.AMOUNT AS DATA_FEE, CDF.START_DATE AS START_DATE, CDF.END_DATE AS END_DATE FROM CAMPAIGN_DATA_FEE CDF WHERE CDF.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ")";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_ALL_CAMPAIGN_DATA_FEE_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_DATA_FEE_SQL);
        ResultSet rs = pst.executeQuery();

        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                CampaignDataFeeDto dataFee = new CampaignDataFeeDto();
                dataFee.setId(rs.getLong("DATA_FEE_ID"));
                dataFee.setStartDate(rs.getDate("START_DATE"));
                dataFee.setEndDate(nullableDate(rs, "END_DATE"));
                dataFee.setAmount(rs.getBigDecimal("DATA_FEE"));
                campaign.getHistoricalDataFees().add(dataFee);
                //if end date is null means it is the current one
                if (dataFee.getEndDate() == null) {
                    campaign.setCurrentDataFee(dataFee);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignAgencyDiscount(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.info("Loading Campaign Agency Discount");
        final String LOAD_ALL_CAMPAIGN_AGENCY_DISCOUNT_SQL = "SELECT CAD.CAMPAIGN_ID, CAD.DISCOUNT FROM CAMPAIGN_AGENCY_DISCOUNT CAD WHERE CAD.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and CAD.START_DATE <= now() and ( CAD.END_DATE >= now() or CAD.END_DATE is null)";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_ALL_CAMPAIGN_AGENCY_DISCOUNT_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_AGENCY_DISCOUNT_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                campaign.setAgencyDiscount(rs.getBigDecimal("DISCOUNT"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCompanyMediaCostMargin(Connection conn, Map<Long, CompanyDto> companiesById, String commaSeparatedCompanyIds) throws SQLException {
        LOG.fine("Loading Company Media Cost Margin");
        final String LOAD_ALL_COMPANY_MEDIA_COST_MARGIN_SQL = "SELECT CMCM.COMPANY_ID, CMCM.MEDIA_COST_MARGIN FROM ADVERTISER_MEDIA_COST_MARGIN CMCM WHERE CMCM.COMPANY_ID IN ("
                + commaSeparatedCompanyIds + ") and CMCM.START_DATE <= now() and ( CMCM.END_DATE >= now() or CMCM.END_DATE is null)";
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(LOAD_ALL_COMPANY_MEDIA_COST_MARGIN_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_COMPANY_MEDIA_COST_MARGIN_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CompanyDto company = companiesById.get(rs.getLong("COMPANY_ID"));
                if (company == null) {
                    continue;
                }
                company.setMediaCostMargin(rs.getBigDecimal("MEDIA_COST_MARGIN"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    
    private void loadHistoricalCompanyDirectCost(Connection conn, Map<Long, CompanyDto> companiesById, String commaSeparatedCompanyIds) throws SQLException {
        LOG.info("Loading Historical Company Direct Cost");
        final String sql = 
        		"SELECT ID, COMPANY_ID, DIRECT_COST, START_DATE, END_DATE "//
        		+ " FROM COMPANY_DIRECT_COST WHERE COMPANY_ID IN (" + commaSeparatedCompanyIds
                + ") ORDER BY START_DATE DESC";
        
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CompanyDto company = companiesById.get(rs.getLong("COMPANY_ID"));
                if (company == null) {
                    continue;
                }

            	CompanyDirectCostDto directCost = new CompanyDirectCostDto();
            	Long id = nullableLong(rs, "ID");
                
            	directCost.setId(id);
            	directCost.setDirectCost(rs.getBigDecimal("DIRECT_COST"));
            	directCost.setStartDate(rs.getTimestamp("START_DATE"));
            	directCost.setEndDate(rs.getTimestamp("END_DATE"));
            	
            	company.getHistoricalDirectCost().add(directCost);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }
    
    private void loadCompanyMarginShareDSP(Connection conn, Map<Long, CompanyDto> companiesById, String commaSeparatedCompanyIds) throws SQLException {
    	LOG.info("Loading Company Margin Share DSP");
    	final String LOAD_ALL_COMPANY_MARGIN_SHARE_DSP_SQL = "SELECT CMSD.COMPANY_ID, CMSD.MARGIN FROM MARGIN_SHARE_DSP CMSD WHERE CMSD.COMPANY_ID IN (" + commaSeparatedCompanyIds
    			+ ") and CMSD.START_DATE <= now() and ( CMSD.END_DATE >= now() or CMSD.END_DATE is null)";
    	if (LOG.isLoggable(Level.FINER)) {
    		LOG.finer(LOAD_ALL_COMPANY_MARGIN_SHARE_DSP_SQL);
    	}
    	PreparedStatement pst = conn.prepareStatement(LOAD_ALL_COMPANY_MARGIN_SHARE_DSP_SQL);
    	ResultSet rs = pst.executeQuery();
    	try {
    		while (rs.next()) {
    			CompanyDto company = companiesById.get(rs.getLong("COMPANY_ID"));
    			if (company == null) {
    				continue;
    			}
    			company.setMarginShareDSP(rs.getBigDecimal("MARGIN"));
    		}
    	} finally {
    		DbUtils.closeQuietly(null, pst, rs);
    	}
    }

    private void loadPublications(DataCollectorDomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Publications");
        }

        Map<Long, PublisherDto> publishersById = new HashMap<Long, PublisherDto>();
        Map<Long, com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto> companiesById = new HashMap<Long, com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT "
                    // Publication
                    + "p0.ID AS PUBLICATION_ID"
                    // Publisher
                    + ", p1.ID AS PUBLISHER_ID, p1.ACCOUNT_ID AS PUBLISHER_ACCOUNT_ID, p1.BUYER_PREMIUM AS PUBLISHER_BUYER_PREMIUM"
                    // Company
                    + ", c0.ID AS COMPANY_ID, c0.TAXABLE_PUBLISHER AS COMPANY_TAXABLE_PUBLISHER, c0.DEFAULT_TIME_ZONE AS COMPANY_DEFAULT_TIME_ZONE"
                    // current PublisherRevShare
                    + ", p2.ID AS PUBLISHER_REV_SHARE_ID, p2.REV_SHARE AS PUBLISHER_REV_SHARE_REV_SHARE, p2.START_DATE AS PUBLISHER_REV_SHARE_START_DATE, p2.END_DATE AS PUBLISHER_REV_SHARE_END_DATE"
                    + " FROM PUBLICATION p0" + " JOIN PUBLISHER p1 ON p1.ID=p0.PUBLISHER_ID" + " JOIN COMPANY c0 ON c0.ID=p1.COMPANY_ID"
                    + " JOIN PUBLISHER_REV_SHARE p2 ON p2.ID=p1.CURRENT_REV_SHARE_ID" + " WHERE p0.STATUS IN (" + QUOTED_PUBLICATION_STATUSES + ")";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                // Publication
                PublicationDto publication = new PublicationDto();
                publication.setId(rs.getLong("PUBLICATION_ID"));

                // Publisher
                long publisherId = rs.getLong("PUBLISHER_ID");
                PublisherDto publisher = publishersById.get(publisherId);
                if (publisher == null) {
                    publisher = new PublisherDto();
                    publisher.setId(publisherId);
                    publisher.setAccountId(rs.getLong("PUBLISHER_ACCOUNT_ID"));
                    publisher.setBuyerPremium(rs.getBigDecimal("PUBLISHER_BUYER_PREMIUM"));
                    publishersById.put(publisher.getId(), publisher);

                    // Company
                    long companyId = rs.getLong("COMPANY_ID");
                    com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto company = companiesById.get(companyId);
                    if (company == null) {
                        company = new com.adfonic.domain.cache.dto.datacollector.publication.CompanyDto();
                        company.setId(companyId);
                        company.setTaxablePublisher(rs.getBoolean("COMPANY_TAXABLE_PUBLISHER"));
                        company.setDefaultTimeZoneID(rs.getString("COMPANY_DEFAULT_TIME_ZONE"));
                        companiesById.put(company.getId(), company);
                    }
                    publisher.setCompany(company);

                    // currentPublisherRevShare - we assume that they're not shared across publishers,
                    // which means we can just blindly create a new instance without worrying about
                    // efficiency here (as opposed to the publishersById and companiesById and what
                    // not above).
                    PublisherRevShareDto currentPublisherRevShare = new PublisherRevShareDto();
                    currentPublisherRevShare.setId(rs.getLong("PUBLISHER_REV_SHARE_ID"));
                    currentPublisherRevShare.setRevShare(rs.getBigDecimal("PUBLISHER_REV_SHARE_REV_SHARE"));
                    currentPublisherRevShare.setStartDate(rs.getTimestamp("PUBLISHER_REV_SHARE_START_DATE"));
                    currentPublisherRevShare.setEndDate(rs.getTimestamp("PUBLISHER_REV_SHARE_END_DATE"));
                    publisher.setCurrentPublisherRevShare(currentPublisherRevShare);
                }
                publication.setPublisher(publisher);

                cache.publicationsById.put(publication.getId(), publication);
            }
            DbUtils.closeQuietly(null, pst, rs);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Initially loaded " + cache.publicationsById.size() + " Publications, doing followup queries for " + publishersById.size() + " Publishers");
            }

            // Publisher.revShareHistory
            String commaSeparatedPublisherIds = StringUtils.join(publishersById.keySet(), ',');
            sql = "SELECT PUBLISHER_ID, ID, REV_SHARE, START_DATE, END_DATE FROM PUBLISHER_REV_SHARE WHERE PUBLISHER_ID IN (" + commaSeparatedPublisherIds
                    + ") ORDER BY PUBLISHER_ORDER ASC";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            int revShareCount = 0;
            while (rs.next()) {
                int idx = 1;
                PublisherDto publisher = publishersById.get(rs.getLong(idx++));
                PublisherRevShareDto publisherRevShare = new PublisherRevShareDto();
                publisherRevShare.setId(rs.getLong(idx++));
                publisherRevShare.setRevShare(rs.getBigDecimal(idx++));
                publisherRevShare.setStartDate(rs.getTimestamp(idx++));
                publisherRevShare.setEndDate(rs.getTimestamp(idx++));
                publisher.getRevShareHistory().add(publisherRevShare);
                ++revShareCount;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Loaded " + revShareCount + " PublisherRevShares across " + publishersById.size() + " Publishers");
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.publicationsById.size() + " Publications");
        }
    }
}
