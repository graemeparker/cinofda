package com.adfonic.domainserializer.loader;

import static com.adfonic.domain.cache.ext.util.DbUtil.nullableDouble;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.domain.BidType;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BiddingStrategy;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.ConnectionType;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.PrivateMarketPlaceDeal.AuctionType;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.domain.Segment.SegmentSafetyLevel;
import com.adfonic.domain.cache.dto.adserver.LocationTargetDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.BidDeductionDto;
import com.adfonic.domain.cache.dto.adserver.creative.BidSeatDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignTimePeriodDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDirectCostDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.domain.cache.dto.adserver.creative.PrivateMarketPlaceDealDto;
import com.adfonic.domain.cache.dto.adserver.creative.PublicationListDto;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.util.DbUtil;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.CreativeServiceImpl;
import com.adfonic.domainserializer.loader.AdCacheBuildContext.PublisherAuditedCreativeValue;
import com.adfonic.util.Subnet;

public class CreativeLoader {

    private final Logger LOG = LoggerFactory.getLogger(CreativeLoader.class.getName());

    private final DataSource dataSource;

    private final CampaignAudienceLoader campaignAudienceLoader;

    public CreativeLoader(DataSource dataSource, CampaignAudienceLoader campaignAudienceLoader) {
        this.dataSource = dataSource;
        this.campaignAudienceLoader = campaignAudienceLoader;
    }

    private static final String CREATIVES_QUERY = "SELECT "
            // Creative
            + "cre.ID AS CREATIVE_ID, cre.EXTERNAL_ID AS CREATIVE_EXTERNAL_ID, cre.NAME AS CREATIVE_NAME, cre.PLUGIN_BASED AS CREATIVE_PLUGIN_BASED, cre.PRIORITY AS CREATIVE_PRIORITY, cre.END_DATE AS CREATIVE_END_DATE, cre.STATUS AS CREATIVE_STATUS, cre.FORMAT_ID AS CREATIVE_FORMAT_ID, cre.LANGUAGE_ID AS CREATIVE_LANGUAGE_ID, cre.EXTENDED_CREATIVE_TYPE_ID AS CREATIVE_EXTENDED_CREATIVE_TYPE_ID, cre.CLOSED_MODE as CREATIVE_CLOSED_MODE, cre.ALLOW_EXTERNAL_AUDIT as CREATIVE_ALLOW_EXTERNAL_AUDIT, cre.SSL_COMPLIANT, cre.CREATION_TIME as CREATIVE_CREATION_TIME"
            // Campaign
            + ", cam.ID AS CAMPAIGN_ID, cam.NAME AS CAMPAIGN_NAME, cam.EXTERNAL_ID AS CAMPAIGN_EXTERNAL_ID, cam.START_DATE AS CAMPAIGN_START_DATE, cam.END_DATE AS CAMPAIGN_END_DATE, cam.STATUS AS CAMPAIGN_STATUS, cam.REFERENCE as CAMPAIGN_REFERENCE, cam.DISABLE_LANGUAGE_MATCH AS CAMPAIGN_DISABLE_LANGUAGE_MATCH, cam.BOOST_FACTOR AS CAMPAIGN_BOOST_FACTOR, cam.CAP_IMPRESSIONS AS CAMPAIGN_CAP_IMPRESSION, cam.CAP_PERIOD_SECONDS AS CAMPAIGN_PERIOD_SECONDS, cam.CAP_PER_CAMPAIGN AS CAMPAIGN_CAP_PER_CAMPAIGN, cam.APPLICATION_ID AS CAMPAIGN_APPLICATION_ID, cam.INSTALL_TRACKING_ENABLED AS CAMPAIGN_INSTALL_TRACKING_ENABLED, cam.INSTALL_TRACKING_ADX_ENABLED AS CAMPAIGN_INSTALL_TRACKING_ADX_ENABLED, cam.CONVERSION_TRACKING_ENABLED AS CAMPAIGN_CONVERSION_TRACKING_ENABLED, cam.HOUSE_AD AS CAMPAIGN_HOUSE_AD, cam.THROTTLE AS CAMPAIGN_THROTTLE, cam.ADVERTISER_DOMAIN AS CAMPAIGN_ADVERTISER_DOMAIN, cam.CATEGORY_ID AS CAMPAIGN_CATEGORY_ID, cam.PUBLICATION_LIST_ID as CAMPAIGN_PUBLICATION_LIST_ID, cam.BUDGET_MANAGER_ENABLED as CAMPAIGN_BUDGET_MANAGER_ENABLED, cam.DAILY_BUDGET_IMPRESSIONS as CAMPAIGN_DAILY_BUDGET_IMPRESSIONS, cam.OVERALL_BUDGET_IMPRESSIONS as CAMPAIGN_OVERALL_BUDGET_IMPRESSIONS, cam.DAILY_BUDGET_CLICKS as CAMPAIGN_DAILY_BUDGET_CLICKS, cam.OVERALL_BUDGET_CLICKS as CAMPAIGN_OVERALL_BUDGET_CLICKS, cam.MAX_BID_THRESHOLD as MAX_BID_THRESHOLD " 
            // Advertiser
            + ", adv.ID AS ADVERTISER_ID, adv.EXTERNAL_ID AS ADVERTISER_EXTERNAL_ID, adv.ENABLE_RTB_BID_SEAT as ADVERTISER_ENABLE_RTB_BID_SEAT"
            // Company
            + ", com.ID AS COMPANY_ID, com.DISCOUNT AS COMPANY_DISCOUNT, com.BACKFILL AS COMPANY_BACKFILL,com.PUBLICATION_WHITE_LIST_ID as COMPANY_PUBLICATION_WHITE_LIST_ID, com.PUBLICATION_BLACK_LIST_ID as COMPANY_PUBLICATION_BLACK_LIST_ID, com.ENABLE_RTB_BID_SEAT as COMPANY_ENABLE_RTB_BID_SEAT"
            // COMPANY_DIRECT_COST
            + ", cdc.ID AS COMPANY_DIRECT_COST_ID, cdc.DIRECT_COST as COMPANY_DIRECT_COST"
            // Campaign.currentBid
            + ", cbi.ID AS CAMPAIGN_BID_ID, cbi.BID_TYPE AS CAMPAIGN_BID_TYPE, cbi.AMOUNT AS CAMPAIGN_BID_AMOUNT, cbi.MAXIMUM AS MAXIMUM, cbi.BID_MODEL_TYPE AS BID_MODEL_TYPE"
            // Campaign.currentAgencyDiscount
            + ", cd.ID AS CAMPAIGN_AGENCY_DISCOUNT_ID, cd.DISCOUNT AS CAMPAIGN_AGENCY_DISCOUNT, cd.START_DATE AS CAMPAIGN_AGENCY_DISCOUNT_START_DATE, cd.END_DATE AS CAMPAIGN_AGENCY_DISCOUNT_END_DATE"
            // Company.marginShareDsp
            + ", mas.ID AS MARGIN_SHARE_DSP_ID, mas.MARGIN AS COMPANY_MARGIN_SHARE_DSP, mas.START_DATE AS COMPANY_MARGIN_SHARE_START_DATE, mas.END_DATE AS COMPANY_MARGIN_SHARE_END_DATE"
            // Segment
            + ", seg.ID AS SEGMENT_ID, seg.GENDER_MIX AS SEGMENT_GENDER_MIX, seg.MIN_AGE AS SEGMENT_MIN_AGE, seg.MAX_AGE AS SEGMENT_MAX_AGE, seg.CONNECTION_TYPE AS SEGMENT_CONNECTION_TYPE, seg.MOBILE_OPERATOR_LIST_IS_WHITELIST AS SEGMENT_MOBILE_OPERATOR_LIST_IS_WHITELIST, seg.ISP_OPERATOR_LIST_IS_WHITELIST AS SEGMENT_ISP_OPERATOR_LIST_IS_WHITELIST, seg.COUNTRY_LIST_IS_WHITELIST AS SEGMENT_COUNTRY_LIST_IS_WHITELIST, seg.INCLUDE_ADFONIC_NETWORK AS INCLUDE_ADFONIC_NETWORK, seg.IP_ADDRESS_LIST_IS_WHITELIST AS SEGMENT_IP_ADDRESS_LIST_IS_WHITELIST"
            // Additional Segment attributes
            + ", seg.INCENTIVIZED_ALLOWED AS SEGMENT_INCENTIVIZED_ALLOWED, seg.MEDIUM as SEGMENT_MEDIUM, seg.EXPLICIT_GPS_ENABLED as EXPLICIT_GPS_ENABLED, seg.SAFETY_LEVEL as SEGMENT_SAFETY_LEVEL"
            // Destination
            + ", dst.ID AS DESTINATION_ID, dst.DESTINATION_TYPE AS DESTINATION_TYPE, dst.DATA AS DESTINATION_DATA, dst.IS_DATA_FINAL_DESTINATION AS DESTINATION_IS_DATA_FINAL_DESTINATION, dst.FINAL_DESTINATION AS DESTINATION_FINAL_DESTINATION"
            + " FROM CREATIVE cre JOIN CAMPAIGN cam ON cam.ID=cre.CAMPAIGN_ID" // 
            + " JOIN SEGMENT seg ON seg.ID=cre.SEGMENT_ID"//
            + " JOIN DESTINATION dst ON dst.ID=cre.DESTINATION_ID" //
            + " JOIN ADVERTISER adv ON adv.ID=cam.ADVERTISER_ID" //
            + " JOIN ACCOUNT ON ACCOUNT.ID=adv.ACCOUNT_ID" //
            + " JOIN COMPANY com ON com.ID=adv.COMPANY_ID" //
            + " LEFT OUTER JOIN CAMPAIGN_BID cbi ON cbi.ID=cam.CURRENT_BID_ID" //
            + " LEFT OUTER JOIN COMPANY_DIRECT_COST cdc ON cdc.ID=com.CURRENT_COMPANY_DIRECT_COST_ID" //
            + " LEFT OUTER JOIN CAMPAIGN_AGENCY_DISCOUNT cd ON cd.ID=cam.CAMPAIGN_AGENCY_DISCOUNT_ID" //
            + " LEFT OUTER JOIN MARGIN_SHARE_DSP mas ON mas.ID=com.MARGIN_SHARE_DSP_ID"//
            + " LEFT OUTER JOIN EXTENDED_CREATIVE_TYPE ect ON ect.ID=cre.EXTENDED_CREATIVE_TYPE_ID" //
            + " WHERE (cre.STATUS=? OR cre.CLOSED_MODE) "//
            + " AND (cam.STATUS=? OR (cam.STATUS=? AND cam.id IN (SELECT CAMPAIGN_ID FROM CAMPAIGN_TRIGGER WHERE DELETED = 0)))"
            // Make sure the creative's endDate hasn't passed yet
            + " AND (cre.END_DATE IS NULL OR cre.END_DATE > CURRENT_TIMESTAMP OR cre.CLOSED_MODE)"
            // We need to allow campaigns that haven't started yet to be included.
            // Now we use Campaign.isCurrentlyActive at targeting time to see if
            // there's an active time period.
            //+ " AND (cam.START_DATE IS NULL OR cam.START_DATE <= CURRENT_TIMESTAMP)"
            // Exclude campaigns that have ended already
            + " AND (cam.END_DATE IS NULL OR cam.END_DATE > CURRENT_TIMESTAMP)"
            // One of the following must be true:
            // - The advertiser has a positive balance
            // - The advertiser's company is marked backfill
            // - The campaign is a house ad
            + " AND (ACCOUNT.BALANCE > 0 OR com.BACKFILL OR cam.HOUSE_AD)";

    public Future<CreativeService> loadCreativesConcurrently(ExecutorService futuresExecutorService, final AdCacheBuildContext td) {

        Future<CreativeService> creativeServiceFuture = futuresExecutorService.submit(new Callable<CreativeService>() {

            @Override
            public CreativeService call() throws Exception {
                CreativeService creativeService = null;
                try {
                    creativeService = loadCreatives(td);
                } catch (Exception e) {
                    // We can propagate this as an unchecked exception, and the call to
                    // Future.get below will catch it
                    throw new RuntimeException(e);
                }
                return creativeService;
            }
        });
        return creativeServiceFuture;

    }

    public CreativeService loadCreatives(AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Creatives");
        // Query all non-deleted AdSpaces for all eligible Publications
        LOG.debug("Loading creatives started");
        CreativeService creativeService = new CreativeServiceImpl();
        // We'll start with this collection, and some of these will get
        // eliminated and not cached in a subsequent step below
        Map<Long, CreativeDto> creativesById = new HashMap<Long, CreativeDto>();

        PreparedStatement sqlStatement = null;
        ResultSet sqlResultSet = null;
        try (Connection sqlConnection = dataSource.getConnection()) {

            Set<Long> extendedCreativeIds = new HashSet<Long>();
            Map<Long, CampaignDto> campaignsById = new HashMap<Long, CampaignDto>();
            Map<Long, SegmentDto> segmentsById = new HashMap<Long, SegmentDto>();
            Map<Long, AdvertiserDto> advertisersById = new HashMap<Long, AdvertiserDto>();
            Map<Long, com.adfonic.domain.cache.dto.adserver.creative.CompanyDto> companiesById = new HashMap<Long, com.adfonic.domain.cache.dto.adserver.creative.CompanyDto>();

            String sql = CREATIVES_QUERY + getExtraConditions(td);
            LOG.debug(sql);

            sqlStatement = sqlConnection.prepareStatement(sql);
            sqlStatement.setString(1, Creative.Status.ACTIVE.name());
            sqlStatement.setString(2, Campaign.Status.ACTIVE.name());
            sqlStatement.setString(3, Campaign.Status.PAUSED.name());
            sqlResultSet = sqlStatement.executeQuery();
            while (sqlResultSet.next()) {

                // Creative
                //"
                CreativeDto creative = loadCreative(sqlResultSet);
                if (creative.getExtendedCreativeTypeId() != null) {
                    // We're going to need to load this creative's extended data
                    extendedCreativeIds.add(creative.getId());
                }

                // Campaign
                long campaignId = sqlResultSet.getLong("CAMPAIGN_ID");
                CampaignDto campaign = campaignsById.get(campaignId);
                if (campaign == null) {
                    campaign = loadCampaign(campaignsById, sqlResultSet, td);

                    // Advertiser
                    long advertiserId = sqlResultSet.getLong("ADVERTISER_ID");
                    AdvertiserDto advertiser = advertisersById.get(advertiserId);
                    if (advertiser == null) {
                        advertiser = loadAdvertiser(advertisersById, sqlResultSet);

                        // Company
                        long companyId = sqlResultSet.getLong("COMPANY_ID");
                        CompanyDto company = companiesById.get(companyId);
                        if (company == null) {
                            company = loadCompany(companiesById, sqlResultSet, td);
                        }
                        advertiser.setCompany(company);
                    }
                    campaign.setAdvertiser(advertiser);

                    // Campaign.currentBid
                    CampaignBidDto currentBid = loadCampaignCurrentBid(sqlResultSet);
                    campaign.setCurrentBid(currentBid);
                    campaign.getCurrentBid().setBudgetType(campaign.inferBudgetType());

                }
                creative.setCampaign(campaign);

                // Segment
                long segmentId = sqlResultSet.getLong("SEGMENT_ID");
                SegmentDto segment = segmentsById.get(segmentId);
                if (segment == null) {
                    segment = loadSegment(segmentsById, sqlResultSet);
                }
                creative.setSegment(segment);

                // Additional Segment attributes
                td.safetyLevelBySegmentIdMap.put(creative.getSegment().getId(), SegmentSafetyLevel.valueOf(sqlResultSet.getString("SEGMENT_SAFETY_LEVEL")));
                td.incentivizedAllowedBySegmentId.put(creative.getSegment().getId(), sqlResultSet.getBoolean("SEGMENT_INCENTIVIZED_ALLOWED"));

                // Destination
                DestinationDto destination = loadDestination(sqlResultSet);
                creative.setDestination(destination);

                // Add it to our "all possible" collection for now
                creativesById.put(creative.getId(), creative);
            }
            DbUtils.closeQuietly(null, sqlStatement, sqlResultSet);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Initially loaded " + creativesById.size() + " creatives.");
            }
            td.stopWatch("Loading Creatives");

            if (creativesById.isEmpty()) {
                // This can happen probably only on QA or Dev  
                // If there are no creatives, follow-up queries will fail miserably, so quit right now  
                LOG.info("No Creatives found. Leaving prematurely without followup queriess");
                return creativeService;
            }

            td.startWatch("Creatives Follow up Data/Queries");

            //Load all targetted Publisher.
            loadAllTargettedPublishers(sqlConnection, td);

            // We'll need the various sets of ids in comma-separated lists so we can use
            // those to constraint subsequent followup queries via " WHERE ..._ID IN (...)"
            String commaSeparatedCreativeIds = StringUtils.join(creativesById.keySet(), ',');
            String commaSeparatedCampaignIds = StringUtils.join(campaignsById.keySet(), ',');
            String commaSeparatedSegmentIds = StringUtils.join(segmentsById.keySet(), ',');
            String commaSeparatedAdvertiserIds = StringUtils.join(advertisersById.keySet(), ',');
            String commaSeparatedCompanyIds = StringUtils.join(companiesById.keySet(), ',');

            loadCreativesAssets(sqlConnection, creativesById, commaSeparatedCreativeIds);

            // Creative.getExtendedCreativeTemplates
            loadExtendedCreativeTemplates(sqlConnection, creativesById, commaSeparatedCreativeIds);

            // Creative.extendedData
            loadCreativeExpandedData(sqlConnection, creativesById, commaSeparatedCreativeIds);

            // open-rtb creative attribtues
            loadCreativeAttribtues(sqlConnection, creativesById, commaSeparatedCreativeIds);

            // Creative.removedPublications
            loadCreativeRemovedPublications(sqlConnection, td, commaSeparatedCreativeIds);

            // Campaign.removedPublications
            loadCampaignRemovedPublications(sqlConnection, td, commaSeparatedCampaignIds);

            loadCampaignRmAdServingFee(sqlConnection, campaignsById, commaSeparatedCampaignIds);

            loadCampaignTradingDeskMargin(sqlConnection, campaignsById, commaSeparatedCampaignIds);

            loadCampaignDataFee(sqlConnection, campaignsById, commaSeparatedCampaignIds);
            loadCampaignBidDeductions(sqlConnection, campaignsById, commaSeparatedCampaignIds);

            loadCompanyMediaCostMargin(sqlConnection, companiesById, commaSeparatedCompanyIds);
            // Campaign.timePeriods
            loadCampaignTimePeriod(sqlConnection, campaignsById, commaSeparatedCampaignIds);

            // Campaign.transparentNetworks
            loadCampaignTransparentNetowrk(sqlConnection, td, commaSeparatedCampaignIds);

            // Campaign.deviceIdentifierTypes
            loadCampaignDeviceIdentifierTypes(sqlConnection, campaignsById, commaSeparatedCampaignIds);

            // Segment.countries
            loadSegmentCountries(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.operators
            loadSegmentOperators(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.vendors
            loadSegmentVendor(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.models
            loadSegmentModels(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            //Segment.DeviceGroups
            loadSegmentDeviceGroups(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.browsers
            loadSegmentBrowsers(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.platforms
            loadSegmentPlatforms(sqlConnection, segmentsById, commaSeparatedSegmentIds);
            // Segment.capabilityMap
            loadSegmentCapabilityMap(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.geotargets
            loadSegmentGeotargets(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            //Segment.Locationtargets
            loadSegmentLocationTargets(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.ipAddresses
            loadSegmentIpAddress(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            // Segment.excludedModels
            loadSegmentExcludedModels(sqlConnection, segmentsById, commaSeparatedSegmentIds);
            // Segment.adSpaces
            loadSegmentAdspaces(sqlConnection, segmentsById, td, commaSeparatedSegmentIds);

            // Segment.channels
            loadSegmentChannels(sqlConnection, segmentsById, td, commaSeparatedSegmentIds);

            // Segment.excludedCategories...expanded
            loadSegmentExcludedCategories(sqlConnection, segmentsById, td, commaSeparatedSegmentIds);

            // Segment.includedCategories...expanded
            loadSegmentIncludedCategories(sqlConnection, segmentsById, td, commaSeparatedSegmentIds);

            // AdvertiserStoppage
            loadAdvertiserStoppage(sqlConnection, td, commaSeparatedAdvertiserIds);

            // CampaignStoppage
            loadCampaignStoppage(sqlConnection, td, commaSeparatedCampaignIds);

            // Segment Target Publishers
            loadSegmentTargettedPublishers(sqlConnection, td, commaSeparatedSegmentIds);

            // Segment Day Parting
            loadSegmentDayParting(sqlConnection, segmentsById, commaSeparatedSegmentIds);

            //Load Publication Lists
            loadPublicationList(sqlConnection, td);

            //Load Advertiser Bid Seats
            loadAdvertiserPMPBidSeats(sqlConnection, advertisersById, commaSeparatedAdvertiserIds);

            //Load Campaign Private Market place Deals
            loadCampaignPrivateMarketPlaceDeals(sqlConnection, campaignsById, commaSeparatedCampaignIds);

            //externally audited creatives
            loadPublisherAuditedCreatives(sqlConnection, creativesById, td, commaSeparatedCreativeIds);

            //Load Campaign Behaviour flags
            loadCampaignBehaviouralFlags(sqlConnection, campaignsById);

            //Load beacon urls for destination
            loadBeaconUrls(sqlConnection, creativesById, commaSeparatedCreativeIds);

            // Load campaign audiences
            campaignAudienceLoader.loadCampaignAudiences(sqlConnection, campaignsById);

            // Load Campaign Bidding Strategies
            loadCampaignBiddingStrategies(sqlConnection, campaignsById, commaSeparatedCampaignIds);
            
            // Load Company RTB Bid Seats
            loadCompanyRtbBidSeats(sqlConnection, companiesById, commaSeparatedCompanyIds);
            
            // Load Advertiser RTB Bid Seats
            loadAdvertiserRtbBidSeats(sqlConnection, advertisersById, commaSeparatedAdvertiserIds);
        }

        // Now that we have all the data we need, go through and eliminate
        // whatever creatives won't be eligible.
        int totalLoadedCreatives = 0;
        for (CreativeDto creative : creativesById.values()) {
            // Wrap a try/catch around everything we do here.  This will
            // hopefully catch oddball scenarios like creative.campaign
            // being null due to a dead-end reference.  It won't catch
            // everything, but it's better than nothing, and it should at
            // least allow us to persevere when one creative is troublesome.
            try {
                long campaignId = creative.getCampaign().getId();
                long advertiserId = creative.getCampaign().getAdvertiser().getId();

                if (td.recentlyStoppedCampaignIds.contains(campaignId) || td.recentlyStoppedAdvertiserIds.contains(advertiserId)) {
                    // The campaign was recently stopped, so we need to include the creative
                    // in our special "recently stopped" collection.
                    creativeService.addRecentlyStoppedCreative(creative);
                }

                // It's still TBD (below) whether or not the creative will be targeted
                // at all, based on whether or not the stoppage is effectively permanent.
                // i.e. a campaign might hit its daily budget just before midnight
                // advertiser time...in which case the campaign is stopped, but it's due
                // to be reactivated soon.  That's a scenario in which we need to include
                // the creative as eligible and just let adserver do real-time stoppage
                // checks between now and when it gets reactivated.

                if (td.effectivelyPermanentlyStoppedCampaignIds.contains(campaignId)) {
                    // The campaign and/or advertiser is stopped "effectively permanently"
                    // from our standpoint, so this creative is not and will not soon be
                    // eligible for targeting.  Eliminate it.
                    td.getDsListener().reject(creative, "Stoppage for campaing: " + campaignId);
                    continue;
                }
                if (td.effectivelyPermanentlyStoppedAdvertiserIds.contains(advertiserId)) {
                    td.getDsListener().reject(creative, "Stoppage for advertiser: " + advertiserId);
                    continue;
                }

                // The advertiser/campaign are either not stopped, or they may be due for an
                // unstoppage soon.  Let the creative through, and adserver will do stoppage
                // checks in real time.

                creativeService.addCreativeToCache(creative);
                totalLoadedCreatives++;
                // Store the creative by format as well...which helps us during
                // the deriveEligibleCreatives phase.
                List<CreativeDto> creativesForThisFormatId = td.creativesByFormatId.get(creative.getFormatId());
                if (creativesForThisFormatId == null) {
                    creativesForThisFormatId = new ArrayList<CreativeDto>();
                    td.creativesByFormatId.put(creative.getFormatId(), creativesForThisFormatId);
                }
                creativesForThisFormatId.add(creative);

                // Pre-parse the creative's segment's ipAddresses if we haven't
                // done so already
                SegmentDto segment = creative.getSegment();
                Set<Subnet> subnets = creativeService.getSubnetsBySegmentId(segment.getId());
                if (subnets == null) {
                    // We haven't done this Segment yet...do it now
                    subnets = new LinkedHashSet<Subnet>();
                    if (segment.getIpAddresses() != null) {
                        for (String cidr : segment.getIpAddresses()) {
                            subnets.add(new Subnet(cidr));
                        }
                    }
                    creativeService.addSegmentSubnets(segment.getId(), subnets);
                }
            } catch (Exception e) {
                LOG.error("Error while evaluating Creative id=" + creative.getId(), e);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading creatives completed: " + totalLoadedCreatives);
        }
        td.stopWatch("Creatives Follow up Data/Queries");
        return creativeService;
    }

    private String getExtraConditions(AdCacheBuildContext td) {

        // Debuging single campaign/creative eligibility - not a normal DS run
        StringBuilder sb = new StringBuilder();
        if (td.debugCreativeId != null) {
            LOG.debug("Debugging creativeId property found: " + td.debugCreativeId);
            sb.append(" AND cre.ID=").append(td.debugCreativeId);
        }

        if (td.debugCampaignId != null) {
            LOG.debug("Debugging campaignId property found: " + td.debugCampaignId);
            sb.append(" AND cam.ID=").append(td.debugCampaignId);
        }
        return sb.toString();
    }

    private CreativeDto loadCreative(ResultSet rs) throws SQLException {
        CreativeDto creative = new CreativeDto();
        creative.setId(rs.getLong("CREATIVE_ID"));
        creative.setExternalID(rs.getString("CREATIVE_EXTERNAL_ID"));
        creative.setName(rs.getString("CREATIVE_NAME"));
        creative.setPluginBased(rs.getBoolean("CREATIVE_PLUGIN_BASED"));
        creative.setPriority(rs.getInt("CREATIVE_PRIORITY"));
        creative.setEndDate(rs.getTimestamp("CREATIVE_END_DATE"));
        creative.setStatus(Creative.Status.valueOf(rs.getString("CREATIVE_STATUS")));
        creative.setFormatId(rs.getLong("CREATIVE_FORMAT_ID"));
        creative.setLanguageId(rs.getLong("CREATIVE_LANGUAGE_ID"));
        creative.setExtendedCreativeTypeId(DbUtil.nullableLong(rs, "CREATIVE_EXTENDED_CREATIVE_TYPE_ID"));
        creative.setClosedMode(rs.getBoolean("CREATIVE_CLOSED_MODE"));
        creative.setAllowExternalAudit(rs.getBoolean("CREATIVE_ALLOW_EXTERNAL_AUDIT"));
        creative.setSslCompliant(rs.getBoolean("SSL_COMPLIANT"));
        creative.setCreationDate(rs.getDate("CREATIVE_CREATION_TIME"));
        return creative;

    }

    private CampaignDto loadCampaign(Map<Long, CampaignDto> campaignsById, ResultSet rs, AdCacheBuildContext td) throws SQLException {
        // Campaign
        long campaignId = rs.getLong("CAMPAIGN_ID");
        CampaignDto campaign = campaignsById.get(campaignId);
        if (campaign == null) {
            campaign = new CampaignDto();
            campaign.setId(campaignId);
            campaign.setName(rs.getString("CAMPAIGN_NAME"));
            campaign.setExternalID(rs.getString("CAMPAIGN_EXTERNAL_ID"));
            campaign.setStartDate(rs.getTimestamp("CAMPAIGN_START_DATE"));
            campaign.setEndDate(rs.getTimestamp("CAMPAIGN_END_DATE"));
            campaign.setStatus(Campaign.Status.valueOf(rs.getString("CAMPAIGN_STATUS")));
            campaign.setReference(rs.getString("CAMPAIGN_REFERENCE"));
            campaign.setDisableLanguageMatch(rs.getBoolean("CAMPAIGN_DISABLE_LANGUAGE_MATCH"));
            //SC-151 and SC-247, campaign boost factor can have max value as 10
            double campaignBoostFactor = rs.getDouble("CAMPAIGN_BOOST_FACTOR");
            if (campaignBoostFactor > 10.0) {
                campaignBoostFactor = 10.0;
            }
            campaign.setBoostFactor(campaignBoostFactor);
            campaign.setCapImpressions(DbUtil.nullableInt(rs, "CAMPAIGN_CAP_IMPRESSION"));
            campaign.setCapPeriodSeconds(DbUtil.nullableInt(rs, "CAMPAIGN_PERIOD_SECONDS"));
            campaign.setCapPerCampaign(rs.getBoolean("CAMPAIGN_CAP_PER_CAMPAIGN"));
            campaign.setApplicationID(rs.getString("CAMPAIGN_APPLICATION_ID"));
            campaign.setInstallTrackingEnabled(rs.getBoolean("CAMPAIGN_INSTALL_TRACKING_ENABLED"));
            campaign.setInstallTrackingAdXEnabled(rs.getBoolean("CAMPAIGN_INSTALL_TRACKING_ADX_ENABLED"));
            campaign.setConversionTrackingEnabled(rs.getBoolean("CAMPAIGN_CONVERSION_TRACKING_ENABLED"));
            campaign.setHouseAd(rs.getBoolean("CAMPAIGN_HOUSE_AD"));
            campaign.setThrottle(rs.getInt("CAMPAIGN_THROTTLE"));
            campaign.setAdvertiserDomain(rs.getString("CAMPAIGN_ADVERTISER_DOMAIN"));
            campaign.setCategoryId(rs.getLong("CAMPAIGN_CATEGORY_ID"));
            campaign.setAgencyDiscount(rs.getDouble("CAMPAIGN_AGENCY_DISCOUNT"));
            campaign.setBudgetManagerEnabled(rs.getBoolean("CAMPAIGN_BUDGET_MANAGER_ENABLED"));

            campaign.setDailyBudgetImpressions(rs.getBigDecimal("CAMPAIGN_DAILY_BUDGET_IMPRESSIONS"));
            campaign.setOverallBudgetImpressions(rs.getBigDecimal("CAMPAIGN_OVERALL_BUDGET_IMPRESSIONS"));
            campaign.setDailyBudgetClicks(rs.getBigDecimal("CAMPAIGN_DAILY_BUDGET_CLICKS"));
            campaign.setOverallBudgetClicks(rs.getBigDecimal("CAMPAIGN_OVERALL_BUDGET_CLICKS"));
            campaign.setBudgetManagerEnabled(rs.getBoolean("CAMPAIGN_BUDGET_MANAGER_ENABLED"));
            
            campaign.setMaxBidThreshold(rs.getBigDecimal("MAX_BID_THRESHOLD"));

            campaignsById.put(campaign.getId(), campaign);

            if (rs.getObject("CAMPAIGN_PUBLICATION_LIST_ID") != null) {
                td.campaignPublicationListId.put(campaign.getId(), rs.getLong("CAMPAIGN_PUBLICATION_LIST_ID"));
            }
        }
        return campaign;
    }

    private AdvertiserDto loadAdvertiser(Map<Long, AdvertiserDto> advertisersById, ResultSet rs) throws SQLException {
        long advertiserId = rs.getLong("ADVERTISER_ID");
        AdvertiserDto advertiser = advertisersById.get(advertiserId);
        if (advertiser == null) {
            advertiser = new AdvertiserDto();
            advertiser.setId(advertiserId);
            advertiser.setExternalID(rs.getString("ADVERTISER_EXTERNAL_ID"));
            advertiser.setEnableRtbBidSeat(rs.getBoolean("ADVERTISER_ENABLE_RTB_BID_SEAT"));
            advertisersById.put(advertiser.getId(), advertiser);
        }
        return advertiser;
    }

    private CompanyDto loadCompany(Map<Long, CompanyDto> companiesById, ResultSet rs, AdCacheBuildContext td) throws SQLException {
        // Company
        long companyId = rs.getLong("COMPANY_ID");
        CompanyDto company = companiesById.get(companyId);
        if (company == null) {
            company = new CompanyDto();
            company.setId(companyId);
            company.setMarginShareDSP(rs.getDouble("COMPANY_MARGIN_SHARE_DSP"));
            company.setBackfill(rs.getBoolean("COMPANY_BACKFILL"));
            companiesById.put(company.getId(), company);
            
            Long directCostId = DbUtil.nullableLong(rs, "COMPANY_DIRECT_COST_ID");
            if(directCostId != null) {
            	CompanyDirectCostDto companyDirectCostDto = new CompanyDirectCostDto();
            	companyDirectCostDto.setId(directCostId);
            	companyDirectCostDto.setDirectCost(rs.getDouble("COMPANY_DIRECT_COST"));
            	company.setDirectCost(companyDirectCostDto);
            }
            if (rs.getObject("COMPANY_PUBLICATION_WHITE_LIST_ID") != null) {
                td.companyWhiteListPublicationListId.put(company.getId(), rs.getLong("COMPANY_PUBLICATION_WHITE_LIST_ID"));
            }
            if (rs.getObject("COMPANY_PUBLICATION_BLACK_LIST_ID") != null) {
                td.companyBlackListPublicationListId.put(company.getId(), rs.getLong("COMPANY_PUBLICATION_BLACK_LIST_ID"));
            }
            company.setEnableRtbBidSeat(rs.getBoolean("COMPANY_ENABLE_RTB_BID_SEAT"));
        }
        return company;
    }

    private CampaignBidDto loadCampaignCurrentBid(ResultSet rs) throws SQLException {
        CampaignBidDto currentBid = null;
        Long campaignBidId = DbUtil.nullableLong(rs, "CAMPAIGN_BID_ID");
        if (campaignBidId != null) {
            currentBid = new CampaignBidDto();
            currentBid.setId(campaignBidId);
            currentBid.setBidType(BidType.valueOf(rs.getString("CAMPAIGN_BID_TYPE")));
            currentBid.setAmount(rs.getDouble("CAMPAIGN_BID_AMOUNT"));
            currentBid.setMaximum(rs.getBoolean("MAXIMUM"));
            currentBid.setBidModelType(BidModelType.valueOf(rs.getString("BID_MODEL_TYPE")));
        }
        return currentBid;
    }

    private SegmentDto loadSegment(Map<Long, SegmentDto> segmentsById, ResultSet rs) throws SQLException {
        // Segment
        long segmentId = rs.getLong("SEGMENT_ID");
        SegmentDto segment = segmentsById.get(segmentId);
        if (segment == null) {
            segment = new SegmentDto();
            segment.setId(segmentId);
            segment.setGenderMix(rs.getBigDecimal("SEGMENT_GENDER_MIX"));
            segment.setMinAge(rs.getInt("SEGMENT_MIN_AGE"));
            segment.setMaxAge(rs.getInt("SEGMENT_MAX_AGE"));
            segment.setConnectionType(ConnectionType.valueOf(rs.getString("SEGMENT_CONNECTION_TYPE")));
            segment.setMobileOperatorListIsWhitelist(rs.getBoolean("SEGMENT_MOBILE_OPERATOR_LIST_IS_WHITELIST"));
            segment.setIspOperatorListIsWhitelist(rs.getBoolean("SEGMENT_ISP_OPERATOR_LIST_IS_WHITELIST"));
            segment.setCountryListIsWhitelist(rs.getBoolean("SEGMENT_COUNTRY_LIST_IS_WHITELIST"));
            segment.setIncludeAdfonicNetwork(rs.getBoolean("INCLUDE_ADFONIC_NETWORK"));
            segment.setIpAddressesListWhitelist(rs.getBoolean("SEGMENT_IP_ADDRESS_LIST_IS_WHITELIST"));

            String targetedMedium = rs.getString("SEGMENT_MEDIUM");
            if (targetedMedium != null) {
                segment.setMedium(Medium.valueOf(targetedMedium));
            }
            segment.setExplicitGPSEnabled(rs.getBoolean("EXPLICIT_GPS_ENABLED"));
            segmentsById.put(segment.getId(), segment);
        }
        return segment;
    }

    private DestinationDto loadDestination(ResultSet rs) throws SQLException {
        DestinationDto destination = new DestinationDto();
        destination.setId(rs.getLong("DESTINATION_ID"));
        destination.setDestinationType(DestinationType.valueOf(rs.getString("DESTINATION_TYPE")));
        destination.setData(rs.getString("DESTINATION_DATA"));
        destination.setDataIsFinalDestination(rs.getBoolean("DESTINATION_IS_DATA_FINAL_DESTINATION"));
        destination.setFinalDestination(rs.getString("DESTINATION_FINAL_DESTINATION"));
        return destination;
    }

    private void loadBeaconUrls(Connection conn, Map<Long, CreativeDto> creativesById, String commaSeparatedCreativeIds) throws SQLException {
        final String LOAD_BEACONS_FOR_DESTINATION = "SELECT b.URL as URL, c.ID as CREATIVE_ID FROM BEACON_URL b " + " JOIN DESTINATION d ON b.DESTINATION_ID=d.ID "
                + " JOIN CREATIVE c ON c.DESTINATION_ID=d.ID " + " WHERE c.ID IN (" + commaSeparatedCreativeIds + ")";

        PreparedStatement pst = conn.prepareStatement(LOAD_BEACONS_FOR_DESTINATION);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CreativeDto creative = creativesById.get(rs.getLong("CREATIVE_ID"));
                creative.getDestination().getBeaconUrls().add(rs.getString("URL"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCreativesAssets(Connection conn, Map<Long, CreativeDto> creativesById, String commaSeparatedCreativeIds) throws SQLException {
        final String LOAD_CREATIVE_ASSETS_SQL = "SELECT a0.CREATIVE_ID AS ASSET_BUNDLE_CREATIVE_ID, a0.DISPLAY_TYPE_ID AS ASSET_BUNDLE_DISPLAY_TYPE_ID "
                + ", a1.COMPONENT_ID AS ASSET_BUNDLE_ASSET_MAP_COMPONENT_ID"
                + ", a2.ID AS ASSET_ID, a2.EXTERNAL_ID AS ASSET_EXTERNAL_ID"
                // If it's the "image" Component, we can omit the asset's data.  We simply
                // don't need image asset data, and it causes bloat.  Otherwise the asset
                // is for the "text" or "icon" component, or something else that we'll end
                // up needing when rendering the creative in ad responses.
                + ", IF(COMPONENT.SYSTEM_NAME='image',NULL,a2.DATA) AS ASSET_DATA"
                // This extra column fetch lets us know if the asset is animated
                + ", c0.ID AS CONTENT_TYPE_ID, c0.ANIMATED AS CONTENT_TYPE_ANIMATED" + " FROM ASSET_BUNDLE a0" + " JOIN ASSET_BUNDLE_ASSET_MAP a1 ON a1.ASSET_BUNDLE_ID=a0.ID"
                + " JOIN ASSET a2 ON a2.ID=a1.ASSET_ID" + " JOIN COMPONENT ON COMPONENT.ID=a1.COMPONENT_ID" + " JOIN CONTENT_TYPE c0 ON c0.ID=a2.CONTENT_TYPE_ID"
                + " WHERE a0.CREATIVE_ID IN (" + commaSeparatedCreativeIds + ")";

        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_CREATIVE_ASSETS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_CREATIVE_ASSETS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CreativeDto creative = creativesById.get(rs.getLong("ASSET_BUNDLE_CREATIVE_ID"));
                long displayTypeId = rs.getLong("ASSET_BUNDLE_DISPLAY_TYPE_ID");
                long componentId = rs.getLong("ASSET_BUNDLE_ASSET_MAP_COMPONENT_ID");
                long contentTypeId = rs.getLong("CONTENT_TYPE_ID");
                AssetDto asset = new AssetDto();
                asset.setId(rs.getLong("ASSET_ID"));
                asset.setExternalID(rs.getString("ASSET_EXTERNAL_ID"));
                asset.setData(rs.getBytes("ASSET_DATA"));
                creative.setAsset(displayTypeId, componentId, asset, contentTypeId);
                // Set animated=true if *any* one of the assets is animated
                if (rs.getBoolean("CONTENT_TYPE_ANIMATED")) {
                    creative.setAnimated(true);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCreativeExpandedData(Connection conn, Map<Long, CreativeDto> creativesById, String commaSeparatedCreativeIds) throws SQLException {
        // Creative.extendedData
        final String LOAD_ALL_CREATIVE_EXPANDED_DATA_SQL = "SELECT CREATIVE_ID, NAME, VALUE FROM EXTENDED_CREATIVE_DATA WHERE CREATIVE_ID IN (" + commaSeparatedCreativeIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CREATIVE_EXPANDED_DATA_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CREATIVE_EXPANDED_DATA_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CreativeDto creative = creativesById.get(rs.getLong("CREATIVE_ID"));
                creative.getExtendedData().put(rs.getString("NAME"), rs.getString("VALUE"));
            }

        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCreativeAttribtues(Connection conn, Map<Long, CreativeDto> creativesById, String commaSeparatedCreativeIds) throws SQLException {
        // Creative.openRtbCreativeAttributes
        final String sql = "SELECT c0.CREATIVE_ID AS CREATIVE_ID, c1.OPEN_RTB_ID AS CREATIVE_OPENRTB_ATTR_ID"
                + " FROM CREATIVE_CREATIVE_ATTRIBUTE c0 JOIN CREATIVE_ATTRIBUTE c1 ON c0.CREATIVE_ATTRIBUTE_ID=c1.ID" + " WHERE c0.CREATIVE_ID IN (" + commaSeparatedCreativeIds
                + ")";

        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }

        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CreativeDto creative = creativesById.get(rs.getLong("CREATIVE_ID"));
                creative.addCreativeAttribute(rs.getInt("CREATIVE_OPENRTB_ATTR_ID"));
            }

        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadPublisherAuditedCreatives(Connection conn, Map<Long, CreativeDto> creativesById, AdCacheBuildContext td, String commaSeparatedCreativeIds) throws SQLException {
        final String LOAD_PUB_AUDTD_CREATS_SQL = "SELECT CREATIVE_ID, PUBLISHER_ID, EXTERNAL_REFERENCE, STATUS, LATEST_FETCH_TIME FROM PUBLISHER_AUDITED_CREATIVE WHERE CREATIVE_ID IN ("
                + commaSeparatedCreativeIds + ")";
        PreparedStatement pst = conn.prepareStatement(LOAD_PUB_AUDTD_CREATS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                Long creativeId = rs.getLong("CREATIVE_ID");
                Long publisherId = rs.getLong("PUBLISHER_ID");
                Map<Long, PublisherAuditedCreativeValue> publisher2auditedCreative = td.creative2PublisherAudited.get(creativeId);
                if (publisher2auditedCreative == null) {
                    td.creative2PublisherAudited.put(creativeId, publisher2auditedCreative = new HashMap<>());
                }

                String statusString = rs.getString("STATUS");
                PublisherAuditedCreative.Status status;
                if ("".equals(statusString)) {
                    // This nasty empty status happens rarely but it prevents cache production
                    status = Status.MISC_UNMAPPED;
                    LOG.warn("Empty PUBLISHER_AUDITED_CREATIVE.STATUS found for creative: " + creativeId + " publisher: " + publisherId + " Using " + status + " instead");
                } else {
                    status = PublisherAuditedCreative.Status.valueOf(statusString);
                }
                String externalReference = rs.getString("EXTERNAL_REFERENCE");
                Date latestFetchTime = rs.getTimestamp("LATEST_FETCH_TIME");
                PublisherAuditedCreativeValue auditedCreative = new PublisherAuditedCreativeValue(status, latestFetchTime, externalReference);

                publisher2auditedCreative.put(publisherId, auditedCreative);

            }

        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }

    }

    private void loadExtendedCreativeTemplates(Connection conn, Map<Long, CreativeDto> creativesById, String commaSeparatedCreativeIds) throws SQLException {
        // Creative.getExtendedCreativeTemplates
        final String LOAD_ALL_CREATIVE_DYNAMIC_TEMPLATES_SQL = "SELECT CREATIVE_ID, CONTENT_FORM, TEMPLATE_PREPROCESSED FROM EXTENDED_CREATIVE_TEMPLATE WHERE CREATIVE_ID IN ("
                + commaSeparatedCreativeIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CREATIVE_DYNAMIC_TEMPLATES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CREATIVE_DYNAMIC_TEMPLATES_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CreativeDto creative = creativesById.get(rs.getLong("CREATIVE_ID"));
                creative.getExtendedCreativeTemplates().put(ContentForm.valueOf(rs.getString("CONTENT_FORM")), rs.getString("TEMPLATE_PREPROCESSED"));
            }

        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCreativeRemovedPublications(Connection conn, AdCacheBuildContext td, String commaSeparatedCreativeIds) throws SQLException {
        // Creative.removedPublications
        // Be careful not to include any "UNREMOVED" ones.
        final String LOAD_ALLCREATIVE_REMOVED_PUBLICATION_SQL = "SELECT DISTINCT map.CREATIVE_ID AS CREATIVE_REMOVED_PUBLICATION_MAP_CREATIVE_ID, map.PUBLICATION_ID AS CREATIVE_REMOVED_PUBLICATION_MAP_PUBLICATION_ID"
                + " FROM CREATIVE_REMOVED_PUBLICATION_MAP map"
                + " INNER JOIN REMOVAL_INFO ON REMOVAL_INFO.ID=map.REMOVAL_INFO_ID"
                + " WHERE REMOVAL_INFO.REMOVAL_TYPE != 'UNREMOVED'" + " AND map.CREATIVE_ID IN (" + commaSeparatedCreativeIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALLCREATIVE_REMOVED_PUBLICATION_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALLCREATIVE_REMOVED_PUBLICATION_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                td.addCreativeRemovedPublication(rs.getLong("CREATIVE_REMOVED_PUBLICATION_MAP_CREATIVE_ID"), rs.getLong("CREATIVE_REMOVED_PUBLICATION_MAP_PUBLICATION_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignRemovedPublications(Connection conn, AdCacheBuildContext td, String commaSeparatedCampaignIds) throws SQLException {
        // Campaign.removedPublications
        // Be careful not to include any "UNREMOVED" ones.
        final String SQL = "SELECT DISTINCT map.CAMPAIGN_ID AS CAMPAIGN_ID, map.PUBLICATION_ID AS PUBLICATION_ID" + " FROM CAMPAIGN_REMOVED_PUBLICATION_MAP map"
                + " INNER JOIN REMOVAL_INFO ON REMOVAL_INFO.ID=map.REMOVAL_INFO_ID" + " WHERE REMOVAL_INFO.REMOVAL_TYPE != 'UNREMOVED'" + " AND map.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(SQL);
        }
        PreparedStatement pst = conn.prepareStatement(SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                td.addCampaignRemovedPublication(rs.getLong("CAMPAIGN_ID"), rs.getLong("PUBLICATION_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignRmAdServingFee(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.debug("Loading Campaign RM AD Servind Fee");
        final String LOAD_ALL_CAMPAIGN_RM_AD_SERVING_FEE_SQL = "SELECT CRASF.CAMPAIGN_ID, CRASF.RM_AD_SERVING_FEE FROM CAMPAIGN_RM_AD_SERVING_FEE CRASF WHERE CRASF.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and CRASF.START_DATE <= now() and ( CRASF.END_DATE >= now() or CRASF.END_DATE is null)";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CAMPAIGN_RM_AD_SERVING_FEE_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_RM_AD_SERVING_FEE_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                campaign.setRmAdServingFee(rs.getDouble("RM_AD_SERVING_FEE"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignTradingDeskMargin(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.debug("Loading Campaign Trading Desk margin");
        final String LOAD_ALL_CAMPAIGN_TRADING_DESK_MARGIN_SQL = "SELECT CTDM.CAMPAIGN_ID, CTDM.TRADING_DESK_MARGIN FROM CAMPAIGN_TRADING_DESK_MARGIN CTDM WHERE CTDM.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and CTDM.START_DATE <= now() and ( CTDM.END_DATE >= now() or CTDM.END_DATE is null)";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CAMPAIGN_TRADING_DESK_MARGIN_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_TRADING_DESK_MARGIN_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                campaign.setTradingDeskMargin(rs.getDouble("TRADING_DESK_MARGIN"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignBidDeductions(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.debug("Loading Campaign Bid Deductions");
        final String LOAD_ALL_BID_DEDUCTIONS_SQL = "SELECT BD.BID_DEDUCTION_ID, BD.CAMPAIGN_ID, BD.PAYER_IS_BYYD, BD.THIRD_PARTY_VENDOR_ID, BD.THIRD_PARTY_VENDOR_FREE_TEXT, BD.AMOUNT FROM BID_DEDUCTION BD WHERE BD.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and BD.START_DATE <= now() and ( BD.END_DATE >= now() or BD.END_DATE is null)";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_BID_DEDUCTIONS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_BID_DEDUCTIONS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                BidDeductionDto bd = new BidDeductionDto();
                bd.setId(rs.getLong("BID_DEDUCTION_ID"));
                bd.setPayerIsByyd(rs.getBoolean("PAYER_IS_BYYD"));
                bd.setThirdPartyVendorId(rs.getLong("THIRD_PARTY_VENDOR_ID"));
                bd.setThirdPartyVendorFreeText(rs.getString("THIRD_PARTY_VENDOR_FREE_TEXT"));
                bd.setAmount(rs.getBigDecimal("AMOUNT"));
                campaign.getBidDeductions().add(bd);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    
    private void loadCampaignDataFee(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        LOG.debug("Loading Campaign Data Fee");
        final String LOAD_ALL_CAMPAIGN_DATA_FEE_SQL = "SELECT CDF.CAMPAIGN_ID, CDF.AMOUNT, CDF.ID FROM CAMPAIGN_DATA_FEE CDF WHERE CDF.CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ") and CDF.START_DATE <= now() and ( CDF.END_DATE >= now() or CDF.END_DATE is null)";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CAMPAIGN_DATA_FEE_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_DATA_FEE_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                if (campaign == null) {
                    continue;
                }
                campaign.setDataFee(rs.getDouble("AMOUNT"));
                campaign.setDataFeeId(rs.getLong("ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCompanyMediaCostMargin(Connection conn, Map<Long, CompanyDto> companiesById, String commaSeparatedCompanyIds) throws SQLException {
        LOG.debug("Loading Company Media Cost Margin");
        final String LOAD_ALL_COMPANY_MEDIA_COST_MARGIN_SQL = "SELECT CMCM.COMPANY_ID, CMCM.MEDIA_COST_MARGIN FROM ADVERTISER_MEDIA_COST_MARGIN CMCM WHERE CMCM.COMPANY_ID IN ("
                + commaSeparatedCompanyIds + ") and CMCM.START_DATE <= now() and ( CMCM.END_DATE >= now() or CMCM.END_DATE is null)";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_COMPANY_MEDIA_COST_MARGIN_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_COMPANY_MEDIA_COST_MARGIN_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CompanyDto company = companiesById.get(rs.getLong("COMPANY_ID"));
                if (company == null) {
                    continue;
                }
                company.setMediaCostMargin(rs.getDouble("MEDIA_COST_MARGIN"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignTimePeriod(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        // Campaign.timePeriods
        final String LOAD_ALL_CAMPAIGN_TIME_PERIOD_SQL = "SELECT CAMPAIGN_ID, ID, START_DATE, END_DATE FROM CAMPAIGN_TIME_PERIOD WHERE CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CAMPAIGN_TIME_PERIOD_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_TIME_PERIOD_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                CampaignTimePeriodDto ctp = new CampaignTimePeriodDto();
                ctp.setId(rs.getLong("ID"));
                ctp.setStartDate(rs.getTimestamp("START_DATE"));
                ctp.setEndDate(rs.getTimestamp("END_DATE"));
                campaign.getTimePeriods().add(ctp);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignTransparentNetowrk(Connection conn, AdCacheBuildContext td, String commaSeparatedCampaignIds) throws SQLException {
        String sql = "SELECT CAMPAIGN_ID, TRANSPARENT_NETWORK_ID FROM CAMPAIGN_TRANSPARENT_NETWORK WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long campaignId = rs.getLong("CAMPAIGN_ID");
                Set<Long> transparentNetworkIds = td.transparentNetworkIdsByCampaignId.get(campaignId);
                if (transparentNetworkIds == null) {
                    transparentNetworkIds = new HashSet<Long>();
                    td.transparentNetworkIdsByCampaignId.put(campaignId, transparentNetworkIds);
                }
                transparentNetworkIds.add(rs.getLong("TRANSPARENT_NETWORK_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadPublicationList(Connection conn, AdCacheBuildContext td) throws SQLException {
        String sql = "select PL.ID PUBLICATION_LIST_ID, PL.WHITE_LIST PL_WHITE_LIST, PL.SNAPSHOT_DATE_TIME PL_SNAPSHOT_DATE_TIME, PLP.PUBLICATION_ID PL_PUBLICATION_ID from PUBLICATION_LIST PL,PUBLICATION_LIST_PUBLICATION PLP where PLP.PUBLICATION_LIST_ID=PL.ID";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        // This query is is quite slow... 
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        int count = 0;
        try {
            while (rs.next()) {
                ++count;
                long publicationListId = rs.getLong("PUBLICATION_LIST_ID");
                PublicationListDto publicationList = td.publicationLists.get(publicationListId);
                if (publicationList == null) {
                    publicationList = new PublicationListDto();
                    publicationList.setPublicationListId(rs.getLong("PUBLICATION_LIST_ID"));
                    publicationList.setSnapshotDateTime(rs.getTimestamp("PL_SNAPSHOT_DATE_TIME"));
                    publicationList.setWhiteList(rs.getBoolean("PL_WHITE_LIST"));
                    td.publicationLists.put(publicationListId, publicationList);
                }
                publicationList.getPublicationIds().add(rs.getLong("PL_PUBLICATION_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
        LOG.debug("Loaded " + td.publicationLists.size() + "  publication lists with " + count + " publications");
    }

    private void loadCampaignDeviceIdentifierTypes(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        String sql = "SELECT CAMPAIGN_ID, DEVICE_IDENTIFIER_TYPE_ID FROM CAMPAIGN_DEVICE_IDENTIFIER_TYPE WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                CampaignDto campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                campaign.getDeviceIdentifierTypeIds().add(rs.getLong("DEVICE_IDENTIFIER_TYPE_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentCountries(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_COUNTRIES_SQL = "SELECT SEGMENT_ID, COUNTRY_ID FROM SEGMENT_COUNTRY WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_COUNTRIES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_COUNTRIES_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getCountryIds().add(rs.getLong("COUNTRY_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentTargettedPublishers(Connection conn, AdCacheBuildContext td, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_COUNTRIES_SQL = "SELECT SEGMENT_ID, PUBLISHER_ID FROM SEGMENT_PUBLISHER WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_COUNTRIES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_COUNTRIES_SQL);
        ResultSet rs = pst.executeQuery();
        Long publisherId;
        Long segmentId;
        try {
            while (rs.next()) {
                segmentId = rs.getLong("SEGMENT_ID");
                publisherId = rs.getLong("PUBLISHER_ID");
                td.addSegmentTargettedPublisher(segmentId, publisherId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadAllTargettedPublishers(Connection conn, AdCacheBuildContext td) throws SQLException {
        String LOAD_ALL_SEGMENT_COUNTRIES_SQL = "SELECT PUBLISHER_ID FROM TARGET_PUBLISHER";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_COUNTRIES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_COUNTRIES_SQL);
        ResultSet rs = pst.executeQuery();
        Long publisherId;
        try {
            while (rs.next()) {
                publisherId = rs.getLong("PUBLISHER_ID");
                td.addTargettedPublisher(publisherId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentOperators(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        // Segment.operators
        String LOAD_ALL_SEGMENT_OPERATORS_SQL = "SELECT SEGMENT_ID, OPERATOR_ID " + "FROM SEGMENT_OPERATOR, OPERATOR " + "WHERE SEGMENT_OPERATOR.OPERATOR_ID=OPERATOR.ID AND "
                + "      OPERATOR.MOBILE_OPERATOR = ? AND " + "      SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_OPERATORS_SQL);
        }

        // Loading Mobile operators
        PreparedStatement mobilePst = conn.prepareStatement(LOAD_ALL_SEGMENT_OPERATORS_SQL);
        mobilePst.setLong(1, 1); // MOBILE_OPERATOR=0 Mobile connections
        ResultSet mobileRs = mobilePst.executeQuery();
        try {
            while (mobileRs.next()) {
                SegmentDto segment = segmentsById.get(mobileRs.getLong("SEGMENT_ID"));
                segment.getMobileOperatorIds().add(mobileRs.getLong("OPERATOR_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, mobilePst, mobileRs);
        }

        // Loading ISP operators
        PreparedStatement ispPst = conn.prepareStatement(LOAD_ALL_SEGMENT_OPERATORS_SQL);
        ispPst.setLong(1, 0); // MOBILE_OPERATOR=0 ISP connections
        ResultSet ispRs = ispPst.executeQuery();
        try {
            while (ispRs.next()) {
                SegmentDto segment = segmentsById.get(ispRs.getLong("SEGMENT_ID"));
                segment.getIspOperatorIds().add(ispRs.getLong("OPERATOR_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, ispPst, ispRs);
        }
    }

    private void loadSegmentVendor(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_VENDOR_SQL = "SELECT SEGMENT_ID, VENDOR_ID FROM SEGMENT_VENDOR WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_VENDOR_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_VENDOR_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getVendorIds().add(rs.getLong("VENDOR_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentModels(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_MODELS_SQL = "SELECT SEGMENT_ID, MODEL_ID FROM SEGMENT_MODEL WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_MODELS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_MODELS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getModelIds().add(rs.getLong("MODEL_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentDeviceGroups(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_MODELS_SQL = "SELECT SEGMENT_ID, DEVICE_GROUP_ID FROM SEGMENT_DEVICE_GROUP WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_MODELS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_MODELS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getDeviceGroupIds().add(rs.getLong("DEVICE_GROUP_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentBrowsers(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_BROWSER_SQL = "SELECT SEGMENT_ID, BROWSER_ID FROM SEGMENT_BROWSER WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_BROWSER_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_BROWSER_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getBrowserIds().add(rs.getLong("BROWSER_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentPlatforms(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_PLATFORM_SQL = "SELECT SEGMENT_ID, PLATFORM_ID FROM SEGMENT_PLATFORM WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_PLATFORM_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_PLATFORM_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getPlatformIds().add(rs.getLong("PLATFORM_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentCapabilityMap(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_CAPABILITY_MAP_SQL = "SELECT SEGMENT_ID, CAPABILITY_ID, VALUE FROM SEGMENT_CAPABILITY_MAP WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_CAPABILITY_MAP_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_CAPABILITY_MAP_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getCapabilityIdMap().put(rs.getLong("CAPABILITY_ID"), rs.getBoolean("VALUE"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentLocationTargets(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String sql = "SELECT slt.SEGMENT_ID, lt.ID, lt.NAME, lt.LATITUDE, lt.LONGITUDE, lt.RADIUS_MILES FROM SEGMENT_LOCATION_TARGET slt"
                + " INNER JOIN LOCATION_TARGET lt on slt.LOCATION_TARGET_ID = lt.ID WHERE slt.SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                int idx = 1;
                SegmentDto segment = segmentsById.get(rs.getLong(idx++));
                LocationTargetDto locationTarget = new LocationTargetDto();
                locationTarget.setId(rs.getLong(idx++));
                locationTarget.setName(rs.getString(idx++));
                locationTarget.setLatitude(rs.getDouble(idx++));
                locationTarget.setLongitude(rs.getDouble(idx++));
                locationTarget.setRadius(nullableDouble(rs, idx++));

                segment.getLocationTargets().add(locationTarget);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentGeotargets(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_GEOTARGETS_SQL = "SELECT SEGMENT_ID, GEOTARGET_ID FROM SEGMENT_GEOTARGET WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_GEOTARGETS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_GEOTARGETS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getGeotargetIds().add(rs.getLong("GEOTARGET_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentIpAddress(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_IP_ADDRESS_SQL = "SELECT SEGMENT_ID, IP_ADDRESS FROM SEGMENT_IP_ADDRESS WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_IP_ADDRESS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_IP_ADDRESS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getIpAddresses().add(rs.getString("IP_ADDRESS"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentExcludedModels(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_EXCLUDED_MODELS_SQL = "SELECT SEGMENT_ID, MODEL_ID FROM SEGMENT_EXCLUDED_MODEL WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_EXCLUDED_MODELS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_EXCLUDED_MODELS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.getExcludedModelIds().add(rs.getLong("MODEL_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentAdspaces(Connection conn, Map<Long, SegmentDto> segmentsById, AdCacheBuildContext td, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGEMNT_AD_SPACES_SQL = "SELECT DISTINCT SEGMENT_ID, AD_SPACE_ID FROM SEGMENT_AD_SPACE WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGEMNT_AD_SPACES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGEMNT_AD_SPACES_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                Set<Long> adSpaceIds = td.adSpaceIdsBySegmentId.get(segment.getId());
                if (adSpaceIds == null) {
                    adSpaceIds = new HashSet<Long>();
                    td.adSpaceIdsBySegmentId.put(segment.getId(), adSpaceIds);
                }
                adSpaceIds.add(rs.getLong("AD_SPACE_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentChannels(Connection conn, Map<Long, SegmentDto> segmentsById, AdCacheBuildContext td, String commaSeparatedSegmentIds) throws SQLException {
        String sql = "SELECT SEGMENT_ID, CHANNEL_ID FROM SEGMENT_CHANNEL WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery(sql);
        try {
            while (rs.next()) {
                long segmentId = rs.getLong("SEGMENT_ID");
                long channelId = rs.getLong("CHANNEL_ID");
                Set<Long> channelIds = td.channelIdsBySegmentId.get(segmentId);
                if (channelIds == null) {
                    channelIds = new HashSet<Long>();
                    td.channelIdsBySegmentId.put(segmentId, channelIds);
                }
                channelIds.add(channelId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentExcludedCategories(Connection conn, Map<Long, SegmentDto> segmentsById, AdCacheBuildContext td, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL = "SELECT SEGMENT_ID, CATEGORY_ID FROM SEGMENT_EXCLUDED_CATEGORY WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL);
        ResultSet rs = pst.executeQuery(LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL);
        try {
            while (rs.next()) {
                long segmentId = rs.getLong("SEGMENT_ID");
                long categoryId = rs.getLong("CATEGORY_ID");
                Set<Long> expanded = td.expandedSegmentExcludedCategoryIds.get(segmentId);
                if (expanded == null) {
                    expanded = new HashSet<Long>();
                    td.expandedSegmentExcludedCategoryIds.put(segmentId, expanded);
                }
                // Since it's an exclusion we associate not only the category
                // itself, but also all of its descendants.
                expanded.addAll(td.expandedCategoryIdsByCategoryId.get(categoryId));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentIncludedCategories(Connection conn, Map<Long, SegmentDto> segmentsById, AdCacheBuildContext td, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL = "SELECT SEGMENT_ID, CATEGORY_ID FROM SEGMENT_INCLUDED_CATEGORY WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL);
        ResultSet rs = pst.executeQuery(LOAD_ALL_SEGMENT_EXCLUDED_CATEGORIES_SQL);
        try {
            while (rs.next()) {
                long segmentId = rs.getLong("SEGMENT_ID");
                long categoryId = rs.getLong("CATEGORY_ID");
                Set<Long> expanded = td.expandedSegmentIncludedCategoryIds.get(segmentId);
                if (expanded == null) {
                    expanded = new HashSet<Long>();
                    td.expandedSegmentIncludedCategoryIds.put(segmentId, expanded);
                }
                // Since it's an exclusion we associate not only the category
                // itself, but also all of its descendants.
                expanded.addAll(td.expandedCategoryIdsByCategoryId.get(categoryId));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadAdvertiserStoppage(Connection conn, AdCacheBuildContext td, String commaSeparatedAdvertiserIds) throws SQLException {
        String LOAD_ALL_ADVERTISER_STOPPAGE_SQL = "SELECT ADVERTISER_ID, TIMESTAMP, REACTIVATE_DATE" + " FROM ADVERTISER_STOPPAGE" + " WHERE ADVERTISER_ID IN ("
                + commaSeparatedAdvertiserIds + ")" + " AND REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP" + " ORDER BY TIMESTAMP";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_ADVERTISER_STOPPAGE_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_ADVERTISER_STOPPAGE_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long advertiserId = rs.getLong("ADVERTISER_ID");
                Date timestamp = rs.getTimestamp("TIMESTAMP");
                Date reactivateDate = rs.getTimestamp("REACTIVATE_DATE");
                // First, check the timestamp to see if the stoppage happened
                // recently.
                // We need to track this so that we can pass recently stopped
                // creatives
                // along in special collection in the cache.
                if (isStoppageRecent(timestamp)) {
                    td.recentlyStoppedAdvertiserIds.add(advertiserId);
                }
                // Also check reactivateDate to determine if the stoppage is going
                // to
                // end soon. Stopped creatives that are going to be un-stopped
                // shortly
                // need to go out in the cache as well, so that they can pick right
                // up
                // when the stoppage ends.
                if (isStoppageGoingToEndSoon(reactivateDate)) {
                    // We can let creatives for this advertiser through, and just
                    // let
                    // adserver do real-time stoppage filtering.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Not enforcing stoppage for Advertiser id=" + advertiserId + ", ends soon (" + reactivateDate + ")");
                    }
                } else {
                    // The stoppage is effectively permanent from our standpoint, so
                    // don't let any creatives through for this advertiser.
                    td.effectivelyPermanentlyStoppedAdvertiserIds.add(advertiserId);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }

    }

    private void loadAdvertiserPMPBidSeats(Connection conn, Map<Long, AdvertiserDto> advertisersById, String commaSeparatedAdvertiserIds) throws SQLException {
        String LOAD_ALL_ADVERTISER_BID_SEATS_SQL = "SELECT BS.ID AS BID_SEAT_ID, BS.SEAT_ID AS SEAT_ID, A.ID AS ADVERTISER_ID" + 
                                                   " FROM BID_SEAT BS,ADVERTISER A " +
                                                   " WHERE BS.TYPE = 'PMP' " +  // PMP seat ids currently do not have any publisher associated to 
                                                   "       AND A.BID_SEAT_ID=BS.ID" +
                                                   "       AND A.ID IN (" + commaSeparatedAdvertiserIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_ADVERTISER_BID_SEATS_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_ADVERTISER_BID_SEATS_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            long advertiserId;
            long bidSeatId;
            AdvertiserDto advertiser;
            BidSeatDto bidSeat;
            Map<Long, BidSeatDto> bidSeatsById = new HashMap<>();
            while (rs.next()) {
                bidSeatId = rs.getLong("BID_SEAT_ID");
                advertiserId = rs.getLong("ADVERTISER_ID");
                String seatId = rs.getString("SEAT_ID");
                advertiser = advertisersById.get(advertiserId);
                if (advertiser == null) {
                    LOG.warn("Advertiser Seat Id exists in BID_SEAT but no such advertiser. Advertiser Id = " + advertiserId);
                } else {
                    bidSeat = bidSeatsById.get(bidSeatId);
                    if (bidSeat == null) {
                        bidSeat = new BidSeatDto();
                        bidSeat.setSeatId(seatId);
                        bidSeat.setId(bidSeatId);
                        bidSeatsById.put(bidSeatId, bidSeat);
                    }
                    advertiser.setPmpBidSeat(bidSeat);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignPrivateMarketPlaceDeals(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        String sql = "SELECT PMD.ID AS PMD_ID,PMD.PUBLISHER_ID AS PUBLISHER_ID,PMD.DEAL_ID AS DEAL_ID,PMD.AUCTION_TYPE AS AUCTION_TYPE,"
                + "PMD.FLOOR AS FLOOR,C.ID AS CAMPAIGN_ID " + "FROM PRIVATE_MARKET_PLACE_DEAL PMD,CAMPAIGN C " + "WHERE C.PRIVATE_MARKET_PLACE_DEAL_ID=PMD.ID " + "AND C.ID IN ("
                + commaSeparatedCampaignIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            CampaignDto campaign;
            long pmdId;
            long publisherId;
            String dealId;
            AuctionType auctionType;
            BigDecimal floor;
            PrivateMarketPlaceDealDto privateMarketPlaceDeal;
            Map<Long, PrivateMarketPlaceDealDto> pmpdById = new HashMap<>();
            String auctionTypeStr;
            while (rs.next()) {
                campaign = campaignsById.get(rs.getLong("CAMPAIGN_ID"));
                pmdId = rs.getLong("PMD_ID");
                privateMarketPlaceDeal = pmpdById.get(pmdId);
                if (privateMarketPlaceDeal == null) {
                    privateMarketPlaceDeal = new PrivateMarketPlaceDealDto();
                    auctionTypeStr = rs.getString("AUCTION_TYPE");
                    if (auctionTypeStr != null && !auctionTypeStr.trim().equals("")) {
                        auctionType = AuctionType.valueOf(auctionTypeStr);
                        privateMarketPlaceDeal.setAuctionType(auctionType);
                    }
                    dealId = rs.getString("DEAL_ID");
                    privateMarketPlaceDeal.setDealId(dealId);
                    floor = rs.getBigDecimal("FLOOR");
                    privateMarketPlaceDeal.setFloor(floor);
                    privateMarketPlaceDeal.setId(pmdId);
                    publisherId = rs.getLong("PUBLISHER_ID");
                    privateMarketPlaceDeal.setPublisherId(publisherId);
                    pmpdById.put(pmdId, privateMarketPlaceDeal);
                }
                campaign.setPrivateMarketPlaceDeal(privateMarketPlaceDeal);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignBehaviouralFlags(Connection conn, Map<Long, CampaignDto> campaignsById) throws SQLException {
        // SC-511
        String campaignAudienceSql = "select CAMPAIGN_ID, 'AUDIENCE' as BEHAVIOUR_TYPE  from CAMPAIGN_AUDIENCE where DELETED = 0 ";
        if (LOG.isDebugEnabled()) {
            LOG.debug(campaignAudienceSql);
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = conn.prepareStatement(campaignAudienceSql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long campaignId = rs.getLong("CAMPAIGN_ID");
                String behaviourType = rs.getString("BEHAVIOUR_TYPE");

                CampaignDto campaign = campaignsById.get(campaignId);
                if (campaign != null) {
                    campaign.setBehavioural(true);
                    if (behaviourType.equals("AUDIENCE")) {
                        campaign.setHasAudience(true);
                    }
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private static boolean isStoppageRecent(Date timestamp) {
        if (timestamp == null) {
            return false;
        }
        // Anything within the last 60 minutes is considered "recent"
        return (System.currentTimeMillis() - timestamp.getTime()) < 3600000L;
    }

    private static boolean isStoppageGoingToEndSoon(Date reactivateDate) {
        if (reactivateDate == null) {
            return false; // it's permanent
        }
        // Anything that ends within the next 60 minutes is considered "soon"
        return (reactivateDate.getTime() - System.currentTimeMillis()) <= 3600000L;
    }

    private void loadCampaignStoppage(Connection conn, AdCacheBuildContext td, String commaSeparatedCampaignIds) throws SQLException {
        String LOAD_ALL_CAMPAIGN_STOPPAGE_SQL = "SELECT CAMPAIGN_ID, TIMESTAMP, REACTIVATE_DATE" + " FROM CAMPAIGN_STOPPAGE" + " WHERE CAMPAIGN_ID IN ("
                + commaSeparatedCampaignIds + ")" + " AND REACTIVATE_DATE IS NULL OR REACTIVATE_DATE > CURRENT_TIMESTAMP" + " ORDER BY TIMESTAMP";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_CAMPAIGN_STOPPAGE_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_CAMPAIGN_STOPPAGE_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long campaignId = rs.getLong("CAMPAIGN_ID");
                Date timestamp = rs.getTimestamp("TIMESTAMP");
                Date reactivateDate = rs.getTimestamp("REACTIVATE_DATE");
                // First, check the timestamp to see if the stoppage happened recently.
                // We need to track this so that we can pass recently stopped creatives
                // along in special collection in the cache.
                if (isStoppageRecent(timestamp)) {
                    td.recentlyStoppedCampaignIds.add(campaignId);
                }
                // Also check reactivateDate to determine if the stoppage is going to
                // end soon.  Stopped creatives that are going to be un-stopped shortly
                // need to go out in the cache as well, so that they can pick right up
                // when the stoppage ends.
                if (isStoppageGoingToEndSoon(reactivateDate)) {
                    // We can let creatives for this campaign through, and just let
                    // adserver do real-time stoppage filtering.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Not enforcing stoppage for Campaign id=" + campaignId + ", ends soon (" + reactivateDate + ")");
                    }
                } else {
                    // The stoppage is effectively permanent from our standpoint, so
                    // don't let any creatives through for this campaign.
                    td.effectivelyPermanentlyStoppedCampaignIds.add(campaignId);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadSegmentDayParting(Connection conn, Map<Long, SegmentDto> segmentsById, String commaSeparatedSegmentIds) throws SQLException {
        String LOAD_ALL_SEGMENT_DAYPARTING_SQL = "SELECT SEGMENT_ID, DAY_OF_WEEK, HOURS FROM SEGMENT_DAYPARTING WHERE SEGMENT_ID IN (" + commaSeparatedSegmentIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_SEGMENT_DAYPARTING_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_SEGMENT_DAYPARTING_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                SegmentDto segment = segmentsById.get(rs.getLong("SEGMENT_ID"));
                segment.addDayHour(rs.getString("DAY_OF_WEEK"), rs.getInt("HOURS"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }

    private void loadCampaignBiddingStrategies(Connection conn, Map<Long, CampaignDto> campaignsById, String commaSeparatedCampaignIds) throws SQLException {
        String LOAD_ALL_BIDDING_STRATEGIES_SQL = "SELECT CAMPAIGN_ID, BIDDING_STRATEGY FROM CAMPAIGN_BIDDING_STRATEGY" + " WHERE CAMPAIGN_ID IN (" + commaSeparatedCampaignIds
                + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_BIDDING_STRATEGIES_SQL);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_BIDDING_STRATEGIES_SQL);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long campaignId = rs.getLong("CAMPAIGN_ID");

                CampaignDto campaignDto = campaignsById.get(campaignId);
                if (campaignDto != null) {
                    String biddingStrategyName = rs.getString("BIDDING_STRATEGY");
                    if (biddingStrategyName != null) {
                        BiddingStrategy biddingStrategy = BiddingStrategy.valueOf(biddingStrategyName);
                        switch (biddingStrategy) {
                        case MEDIA_COST_OPTIMISATION:
                            campaignDto.setMediaCostOptimisationEnabled(true);
                            break;
                        case AVERAGE_MAXIMUM_BID:
                            break;
                        default:
                            LOG.warn("Campaign field not defined for bidding strategy " + biddingStrategyName);
                            break;
                        }
                    } else {
                        LOG.warn("Empty (null) bidding strategy name comming from CAMPAIGN_BIDDING_STRATEGY table.");
                    }
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }
    
    private void loadCompanyRtbBidSeats(Connection conn, Map<Long, CompanyDto> companiesById, String commaSeparatedCompanyIds) throws SQLException {
        LOG.debug("Loading Company Rtb Bid Seats");
        final String LOAD_ALL_COMPANY_BID_SEATS = " SELECT CBS.COMPANY_ID, BS.SEAT_ID, TP.PUBLISHER_ID" +
                                                  " FROM COMPANY_BID_SEAT CBS" +
                                                  "      INNER JOIN BID_SEAT BS ON CBS.BID_SEAT_ID = BS.ID" +
                                                  "      INNER JOIN TARGET_PUBLISHER TP ON BS.TARGET_PUBLISHER_ID = TP.ID" +
                                                  "      INNER JOIN COMPANY C ON C.ID = CBS.COMPANY_ID" +
                                                  " WHERE CBS.COMPANY_ID IN ( " + commaSeparatedCompanyIds + ") AND" +
                                                  "       C.ENABLE_RTB_BID_SEAT = 1";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_COMPANY_BID_SEATS);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_COMPANY_BID_SEATS);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                CompanyDto company = companiesById.get(rs.getLong("COMPANY_ID"));
                if (company == null || !company.isEnableRtbBidSeat()) {
                    continue;
                }
                company.getRtbBidSeats().put(rs.getLong("PUBLISHER_ID"), rs.getString("SEAT_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }
    
    private void loadAdvertiserRtbBidSeats(Connection conn, Map<Long, AdvertiserDto> advertisersById, String commaSeparatedAdvertiserIds) throws SQLException {
        LOG.debug("Loading Advertiser Rtb Bid Seats");
        final String LOAD_ALL_ADVERTISER_BID_SEATS = " SELECT ABS.ADVERTISER_ID, BS.SEAT_ID, TP.PUBLISHER_ID" +
                                                     " FROM ADVERTISER_BID_SEAT ABS" +
                                                     "      INNER JOIN BID_SEAT BS ON ABS.BID_SEAT_ID = BS.ID" +
                                                     "      INNER JOIN TARGET_PUBLISHER TP ON BS.TARGET_PUBLISHER_ID = TP.ID" +
                                                     "      INNER JOIN ADVERTISER A ON A.ID = ABS.ADVERTISER_ID" +
                                                     " WHERE ABS.ADVERTISER_ID IN ( " + commaSeparatedAdvertiserIds + ") AND" +
                                                     "       A.ENABLE_RTB_BID_SEAT = 1";
        if (LOG.isDebugEnabled()) {
            LOG.debug(LOAD_ALL_ADVERTISER_BID_SEATS);
        }
        PreparedStatement pst = conn.prepareStatement(LOAD_ALL_ADVERTISER_BID_SEATS);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                AdvertiserDto advertiser = advertisersById.get(rs.getLong("ADVERTISER_ID"));
                if (advertiser == null || !advertiser.isEnableRtbBidSeat()) {
                    continue;
                }
                advertiser.getRtbBidSeats().put(rs.getLong("PUBLISHER_ID"), rs.getString("SEAT_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
        }
    }
}
