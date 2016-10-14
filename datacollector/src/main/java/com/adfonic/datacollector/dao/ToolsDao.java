package com.adfonic.datacollector.dao;

import static com.adfonic.domain.cache.ext.util.DbUtil.nullableLong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.domain.AdvertiserStoppage;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.CampaignStoppage;
import com.adfonic.domain.cache.dto.datacollector.campaign.AdvertiserDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignBidDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CampaignDto;
import com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDirectCostDto;

@Component
public class ToolsDao extends AbstractDao {

    private static final transient Logger LOG = Logger.getLogger(ToolsDao.class.getName());

    private static final String QUOTED_CAMPAIGN_STATUSES;

    @Autowired
    public ToolsDao(@Qualifier("accountingDataSource") DataSource dataSource) {
        super(dataSource);
    }

    static {
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

    // restrict this to just the campaignId being received.
    public CampaignDto loadCampaign(long campaignId) throws java.sql.SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        ConcurrentHashMap<Long, Long> campaignsAudiences = new ConcurrentHashMap<Long, Long>();
        Map<Long, AdvertiserDto> advertisersById = new HashMap<Long, AdvertiserDto>();
        Map<Long, com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto> companiesById = new HashMap<Long, com.adfonic.domain.cache.dto.datacollector.campaign.CompanyDto>();
        Map<Long, CampaignDto> campaignsById = new HashMap<Long, CampaignDto>();
        Connection conn = getDataSource().getConnection();
        try {
            // SC-511
            String campaignAudienceSql = "select CAMPAIGN_ID  from CAMPAIGN_AUDIENCE ";
            pst = conn.prepareStatement(campaignAudienceSql);
            rs = pst.executeQuery();
            while (rs.next()) {
                campaignsAudiences.put(rs.getLong("CAMPAIGN_ID"), rs.getLong("CAMPAIGN_ID"));
            }

            String sql = "SELECT "
                    // Campaign
                    + "c0.ID, c0.INSTALL_TRACKING_ENABLED, c0.INSTALL_TRACKING_ADX_ENABLED, c0.CONVERSION_TRACKING_ENABLED, c0.APPLICATION_ID"
                    // Advertiser
                    + ", a0.ID, a0.ACCOUNT_ID"
                    // Company
                    + ", c1.ID, c1.DISCOUNT, IF(c1.POST_PAY_ACTIVATION_DATE IS NULL,FALSE,TRUE), c1.TAXABLE_ADVERTISER, c1.DEFAULT_TIME_ZONE, c1.BACKFILL, c1.CURRENT_COMPANY_DIRECT_COST_ID"
                    // CampaignBid
                    + ", c2.ID, c2.BID_TYPE, c2.AMOUNT, c2.START_DATE, c2.END_DATE, c2.MAXIMUM,c2.BID_MODEL_TYPE" + " FROM CAMPAIGN c0"
                    + " JOIN ADVERTISER a0 ON a0.ID=c0.ADVERTISER_ID" + " JOIN COMPANY c1 ON c1.ID=a0.COMPANY_ID" + " LEFT OUTER JOIN CAMPAIGN_BID c2 ON c2.ID=c0.CURRENT_BID_ID"
                    + " WHERE c0.STATUS IN (" + QUOTED_CAMPAIGN_STATUSES + ") and c0.ID = ?";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            pst.setLong(1, campaignId);
            rs = pst.executeQuery();
            if (rs.next()) {
                int idx = 1;
                // Campaign
                CampaignDto campaign = new CampaignDto();
                campaign.setId(rs.getLong(idx++));
                campaign.setInstallTrackingEnabled(rs.getBoolean(idx++));
                campaign.setInstallTrackingAdXEnabled(rs.getBoolean(idx++));
                campaign.setConversionTrackingEnabled(rs.getBoolean(idx++));
                campaign.setApplicationID(rs.getString(idx++));
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
                        
                        Long currentCompanyDirectCostId = nullableLong(rs, idx++);
                        if(currentCompanyDirectCostId != null) {
                        	CompanyDirectCostDto companyDirectCostDto = loadCompanyDirectCost(currentCompanyDirectCostId);
                        	company.setDirectCost(companyDirectCostDto);
                        }
                        
                        companiesById.put(company.getId(), company);
                    } else {
                        idx += 6;
                    }
                    advertiser.setCompany(company);
                } else {
                    idx += 8;
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
                return campaign;
            }
            DbUtils.closeQuietly(null, pst, rs);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public CompanyDirectCostDto loadCompanyDirectCost(long companyDirectCostId) throws java.sql.SQLException {
        PreparedStatement pst = null;
        ResultSet rs = null;
        Connection conn = getDataSource().getConnection();
        try {
            String sql = "SELECT "
                    + " ID, DIRECT_COST , START_DATE ,END_DATE"
                    + " FROM COMPANY_DIRECT_COST"
                    + " WHERE ID  = ?";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            pst.setLong(1, companyDirectCostId);
            rs = pst.executeQuery();
            if (rs.next()) {
                CompanyDirectCostDto directCostDto = new CompanyDirectCostDto();
                directCostDto.setId(rs.getLong("ID"));
                directCostDto.setDirectCost(rs.getBigDecimal("DIRECT_COST"));
                directCostDto.setStartDate(rs.getTimestamp("START_DATE"));
                directCostDto.setEndDate(rs.getTimestamp("END_DATE"));
                
                return directCostDto;
            }
            DbUtils.closeQuietly(null, pst, rs);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
    
    public boolean createCampaignStoppage(long campaignId, CampaignStoppage.Reason reason, Date timestamp, Date reactivateDate) throws java.sql.SQLException {

        PreparedStatement psCampaignSelect = null;
        PreparedStatement psSelect = null;
        PreparedStatement psInsert = null;
        ResultSet rs = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(false); // use explicit transactions

            // Prepare all statements up front so we minimize what needs to
            // be done or could throw exceptions while the row is locked 
            psCampaignSelect = conn.prepareStatement("SELECT 1 FROM CAMPAIGN WHERE ID=? FOR UPDATE"); //Take row-level lock (MAX-1991)
            psSelect = conn.prepareStatement("SELECT 1 FROM CAMPAIGN_STOPPAGE WHERE CAMPAIGN_ID=? AND REACTIVATE_DATE" + (reactivateDate == null ? " IS NULL" : "=?"));
            psInsert = conn.prepareStatement("INSERT INTO CAMPAIGN_STOPPAGE(CAMPAIGN_ID,REASON,TIMESTAMP,REACTIVATE_DATE) VALUES(?,?,?,?)");

            //Setting parameters
            psCampaignSelect.setLong(1, campaignId);
            psSelect.setLong(1, campaignId);
            if (reactivateDate != null) {
                psSelect.setTimestamp(2, new java.sql.Timestamp(reactivateDate.getTime()));
            }

            try {
                psCampaignSelect.executeQuery();

                rs = psSelect.executeQuery();
                if (rs.next()) {
                    // It already exists
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Not inserting would-be-duplicate CampaignStoppage for Campaign id=" + campaignId + ", reactivateDate=" + reactivateDate);
                    }
                    return false;
                }

                // Ok, we're good to insert it
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Inserting CampaignStoppage for Campaign id=" + campaignId + ", reactivateDate=" + reactivateDate);
                }
                psInsert.setLong(1, campaignId);
                if (reason == null) {
                    psInsert.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    psInsert.setString(2, reason.name());
                }
                psInsert.setTimestamp(3, new java.sql.Timestamp(timestamp.getTime()));
                if (reactivateDate == null) {
                    psInsert.setNull(4, java.sql.Types.TIMESTAMP);
                } else {
                    psInsert.setTimestamp(4, new java.sql.Timestamp(reactivateDate.getTime()));
                }
                psInsert.executeUpdate();
                return true;
            } finally {
                conn.commit();
            }
        } finally {
            DbUtils.closeQuietly(psInsert);
            DbUtils.closeQuietly(psCampaignSelect);
            DbUtils.closeQuietly(conn, psSelect, rs);
        }
    }

    public boolean createAdvertiserStoppage(long advertiserId, AdvertiserStoppage.Reason reason, Date timestamp, Date reactivateDate) throws java.sql.SQLException {
        PreparedStatement psLock = null;
        PreparedStatement psUnlock = null;
        PreparedStatement psSelect = null;
        PreparedStatement psInsert = null;
        ResultSet rs = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(false); // use explicit transactions

            // Prepare all statements up front so we minimize what needs to
            // be done or could throw exceptions while the table is locked
            psLock = conn.prepareStatement("LOCK TABLES ADVERTISER_STOPPAGE WRITE");
            psUnlock = conn.prepareStatement("UNLOCK TABLES");
            psSelect = conn.prepareStatement("SELECT 1 FROM ADVERTISER_STOPPAGE WHERE ADVERTISER_ID=? AND REACTIVATE_DATE" + (reactivateDate == null ? " IS NULL" : "=?"));
            psInsert = conn.prepareStatement("INSERT INTO ADVERTISER_STOPPAGE(ADVERTISER_ID,REASON,TIMESTAMP,REACTIVATE_DATE) VALUES(?,?,?,?)");

            psSelect.setLong(1, advertiserId);
            if (reactivateDate != null) {
                psSelect.setTimestamp(2, new java.sql.Timestamp(reactivateDate.getTime()));
            }

            psLock.executeUpdate();
            try {
                rs = psSelect.executeQuery();
                if (rs.next()) {
                    // It already exists
                    if (LOG.isLoggable(Level.INFO)) {
                        LOG.info("Not inserting would-be-duplicate AdvertiserStoppage for Advertiser id=" + advertiserId + ", reactivateDate=" + reactivateDate);
                    }
                    return false;
                }

                // Ok, we're good to insert it
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("Inserting AdvertiserStoppage for Advertiser id=" + advertiserId + ", reactivateDate=" + reactivateDate);
                }
                psInsert.setLong(1, advertiserId);
                if (reason == null) {
                    psInsert.setNull(2, java.sql.Types.VARCHAR);
                } else {
                    psInsert.setString(2, reason.name());
                }
                psInsert.setTimestamp(3, new java.sql.Timestamp(timestamp.getTime()));
                if (reactivateDate == null) {
                    psInsert.setNull(4, java.sql.Types.TIMESTAMP);
                } else {
                    psInsert.setTimestamp(4, new java.sql.Timestamp(reactivateDate.getTime()));
                }
                psInsert.executeUpdate();
                return true;
            } finally {
                psUnlock.executeUpdate();
                conn.commit();
            }
        } finally {
            DbUtils.closeQuietly(psInsert);
            DbUtils.closeQuietly(psLock);
            DbUtils.closeQuietly(psUnlock);
            DbUtils.closeQuietly(conn, psSelect, rs);
        }
    }

    public boolean markCampaignInstallTrackingVerified(long campaignId) throws java.sql.SQLException {
        PreparedStatement ps = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(true); // use implicit transactions
            ps = conn.prepareStatement("UPDATE CAMPAIGN SET INSTALL_TRACKING_VERIFIED=1 WHERE ID=? AND INSTALL_TRACKING_VERIFIED=0");
            ps.setLong(1, campaignId);
            return ps.executeUpdate() > 0;
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }
    }

    public boolean markCampaignConversionTrackingVerified(long campaignId) throws java.sql.SQLException {
        PreparedStatement ps = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(true); // use implicit transactions
            ps = conn.prepareStatement("UPDATE CAMPAIGN SET CONVERSION_TRACKING_VERIFIED=1 WHERE ID=? AND CONVERSION_TRACKING_VERIFIED=0");
            ps.setLong(1, campaignId);
            return ps.executeUpdate() > 0;
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }
    }
}
