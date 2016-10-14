package com.adfonic.domainserializer.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
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
import org.springframework.beans.factory.annotation.Value;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.BidType;
import com.adfonic.domain.Feature;
import com.adfonic.domain.PendingAdType;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publication.PublicationSafetyLevel;
import com.adfonic.domain.RtbConfig;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.RtbConfig.RtbImpTrackMode;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.UnfilledAction;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RateCardDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.adspace.TransparentNetworkDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache.ShardMode;
import com.adfonic.domain.cache.ext.util.DbUtil;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.AdSpaceServiceImpl;
import com.adfonic.domainserializer.DsShard;
import com.adfonic.domainserializer.loader.AdCacheBuildContext.TransientPublicationAttributes;
import com.google.common.collect.ImmutableSet;

/**
 * This class load all Adspaces dendong on whart shard we are using.
 * this class also asume atht categories are already loaded and have been saved in
 * transientData which will be passed to this class
 *
 *
 * @author ravi
 *
 */
public class AdSpaceLoader {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Set of values for Publication.Status for eligibility */
    private static final Set<Publication.Status> eligiblePublicationStatuses = ImmutableSet.of(Publication.Status.ACTIVE); //Publication.Status.PENDING, Publication.Status.PAUSED
    private static final String quotedPublicationStatuses = "'" + StringUtils.join(eligiblePublicationStatuses, "','") + "'";

    private static final Set<AdSpace.Status> ineligibleAdSpaceStatuses = ImmutableSet.of(AdSpace.Status.DELETED, AdSpace.Status.DORMANT);
    private static final String quotedIneligibleAdSpaceStatuses = "'" + StringUtils.join(ineligibleAdSpaceStatuses, "','") + "'";

    private static final String ADSPACES_QUERY = "SELECT"
            // AdSpace
            + " ads.ID AS ADSPACE_ID, ads.NAME AS ADSPACE_NAME, ads.EXTERNAL_ID AS ADSPACE_EXTERNAL_ID, ads.STATUS AS ADSPACE_STATUS, ads.UNFILLED_ACTION AS ADSPACE_UNFILLEDACTION, ads.BACKFILL_ENABLED AS ADSPACE_BACKFILL_ENABLE, ads.COLOR_SCHEME AS ADSPACE_COLOR_SCHEME"
            // Publication
            + ", pun.ID AS PUBLICATION_ID, pun.EXTERNAL_ID AS PUBLICATION_EXTERNAL_ID, pun.NAME AS PUBLICATION_NAME, pun.STATUS AS PUBLICATION_STATUS, pun.PUBLICATION_TYPE_ID AS PUBLICATION_TYPE_ID, pun.INSTALL_TRACKING_DISABLED AS PUBLICATION_INSTALL_TRACKING_DISABLED, IFNULL(pun.TRACKING_IDENTIFIER_TYPE, put.DEFAULT_TRACKING_IDENTIFIER_TYPE) AS PUBLICATION_TRACKING_IDENT_TYPE, pun.AD_REQUEST_TIMEOUT AS PUBLICATION_AD_REQUEST_TIMEOUT, pun.DEFAULT_INTEGRATION_TYPE_ID AS PUBLICATION_DEFULT_INTEGRATION_TYPE_ID, pun.RTB_ID AS PUBLICATION_RTB_ID, pun.CATEGORY_ID AS PUBLICATION_CATEGORY_ID,pun.AD_OPS_STATUS AS PUBLICATION_AD_OPS_STATUS,pun.APPROVED_DATE PUBLICATION_APPROVED_DATE"
            // Publisher
            + ", pbs.ID AS PUBLISHER_ID, pbs.PENDING_AD_TYPE AS PUBLISHER_PENDING_AD_TYPE, pbs.DEFAULT_AD_REQUEST_TIMEOUT AS PUBLISHER_DEFAULT_AD_REQUEST_AD_TYPE,pbs.BUYER_PREMIUM AS BUYER_PREMIUM, pbs.REQUIRES_REAL_DESTINATION AS PUBLISHER_REQUIRES_REAL_DESTINATION"
            // Publisher.currentRevShare
            + ", prs.REV_SHARE AS PUBLISHER_REV_SHARE_CURRENT_VALUE"
            // Company
            + ", pbs.COMPANY_ID AS PUBLISHER_COMPANY_ID, pbs.EXTERNAL_ID AS PUBLISHER_EXTERNAL_ID"
            // RtbConfig
            + ", rtc.ID AS RTB_CONFIG_ID, rtc.AD_MODE AS RTB_CONFIG_AD_MODE, rtc.WIN_NOTICE_MODE AS RTB_CONFIG_WIN_NOTICE_MODE, rtc.ADM_PROFILE AS RTB_CONFIG_ADM_PROFILE, rtc.SP_MACRO AS RTB_CONFIG_SP_MACRO, rtc.ESCD_CLICK_FORWARD_URL AS RTB_CONFIG_ESCD_CLICK_FORWARD_URL, rtc.CLCK_FWD_VALDN_PATTERN AS RTB_CONFIG_CLCK_FWD_VALDN_PATTERN, rtc.DPID_FALLBACK AS RTB_CONFIG_DPID_FALLBACK, rtc.ESCAPED_URL_PREFIX AS RTB_CONFIG_ESCAPED_URL_PREFIX, rtc.INTEGRATION_TYPE_PREFIX AS RTB_CONFIG_INTEGRATION_TYPE_PREFIX, rtc.DECRYPTION_SCHEME AS RTB_CONFIG_DECRYPTION_SCHEME, rtc.SEC_ALIAS AS RTB_CONFIG_SEC_ALIAS, rtc.BID_CURRENCY AS RTB_BID_CURRENCY, rtc.AUCTION_TYPE AS RTB_AUCTION_TYPE, rtc.BID_EXPIRY_TIME_SECONDS AS BID_EXPIRY_TIME_SECONDS, rtc.SSL_REQUIRED, rtc.IMP_TRACK_MODE as RTB_IMP_TRACK_MODE"
            // TransparentNetwork
            + ", trn.ID AS TRANSPARENT_NETWORK_ID, trn.CLOSED AS TRANSPARENT_NETWORK_CLOSED"
            // Extra attributes: PUBLICATION.SAFETY_LEVEL
            + ", pun.SAFETY_LEVEL AS PUBLICATION_SAFETY_LEVEL"
            // TransientPublicationAttributes
            + ", pun.AUTO_APPROVAL AS PUBLICATION_AUTO_APPROVAL, pun.MIN_AGE AS PUBLICATION_MIN_AGE, pun.MAX_AGE AS PUBLICATION_MAX_AGE, pun.INCENTIVIZED AS PUBLICATION_INCENTIVIZED, pun.GENDER_MIX AS PUBLICATION_GENDER_MIX"
            // Publication Bundle
            + ", bun.EXTERNAL_ID AS BUNDLE_NAME" //
            + " FROM AD_SPACE ads" //
            + " JOIN PUBLICATION pun ON pun.ID=ads.PUBLICATION_ID" //
            + " JOIN PUBLISHER pbs ON pbs.ID=pun.PUBLISHER_ID"
            + " JOIN PUBLISHER_REV_SHARE prs ON prs.ID=pbs.CURRENT_REV_SHARE_ID" //
            + " JOIN PUBLICATION_TYPE put ON put.ID=pun.PUBLICATION_TYPE_ID" //
            + " LEFT OUTER JOIN RTB_CONFIG rtc ON rtc.ID=pbs.RTB_CONFIG_ID" //
            + " LEFT OUTER JOIN TRANSPARENT_NETWORK trn ON trn.ID=pun.TRANSPARENT_NETWORK_ID"
            + " LEFT OUTER JOIN PUBLICATION_BUNDLE pbu ON pun.ID=pbu.PUBLICATION_ID LEFT OUTER JOIN BUNDLE bun ON bun.ID=pbu.BUNDLE_ID";

    @Value("${publication.default.sampling.rate:3}")
    private int publicationDefaultSamplingRate;

    private final DataSource dataSource;

    public AdSpaceLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Future<AdSpaceService> loadAdSpacesConcurrently(ExecutorService futuresExecutorService, final AdCacheBuildContext td, final DsShard shard) {

        Future<AdSpaceService> adSpacesFuture = futuresExecutorService.submit(new Callable<AdSpaceService>() {

            @Override
            public AdSpaceService call() throws Exception {
                AdSpaceService adSpaceService = null;
                try {
                    adSpaceService = loadAdspaces(td, shard);
                } catch (Exception e) {
                    // We can propagate this as an unchecked exception, and the call to Future.get below will catch it
                    throw new RuntimeException(e);
                }
                return adSpaceService;
            }
        });
        return adSpacesFuture;

    }

    public AdSpaceService loadAdspaces(AdCacheBuildContext td, DsShard shard) throws SQLException {
        td.startWatch("Loading AdSpaces");
        // Query all non-deleted AdSpaces for all eligible Publications
        ShardMode shardMode = shard.getShardMode();
        Set<Long> publisherIds = shard.getPublisherIds();
        boolean isRtbEnabled = shard.isRtbEnabled();

        LOG.debug("Loading AdSpaces shard: " + shard);

        PreparedStatement sqlStatement = null;
        ResultSet sqlResultSet = null;
        AdSpaceService adSpaceService = new AdSpaceServiceImpl();

        try (Connection sqlConnection = dataSource.getConnection()) {

            //First load all Publication attributes
            loadPublicationAttributes(sqlConnection, td);

            String sql = ADSPACES_QUERY + createWhereClause(quotedIneligibleAdSpaceStatuses, quotedPublicationStatuses, shardMode, publisherIds, td);
            LOG.debug(sql);

            sqlStatement = sqlConnection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
            sqlStatement.setFetchSize(Integer.MIN_VALUE);

            sqlResultSet = sqlStatement.executeQuery();

            Set<Long> ineligiblePublicationIds = new HashSet<Long>();
            Map<Long, PublicationDto> publicationsById = new HashMap<Long, PublicationDto>();
            Map<Long, PublisherDto> publishersById = new HashMap<Long, PublisherDto>();
            Map<Long, com.adfonic.domain.cache.dto.adserver.adspace.CompanyDto> companiesById = new HashMap<Long, com.adfonic.domain.cache.dto.adserver.adspace.CompanyDto>();
            Map<Long, TransparentNetworkDto> transparentNetworksById = new HashMap<Long, TransparentNetworkDto>();

            while (sqlResultSet.next()) {
                // AdSpace
                AdSpaceDto adSpace = new AdSpaceDto();
                adSpace.setId(sqlResultSet.getLong("ADSPACE_ID"));
                adSpace.setName(sqlResultSet.getString("ADSPACE_NAME"));
                adSpace.setExternalID(sqlResultSet.getString("ADSPACE_EXTERNAL_ID"));
                adSpace.setStatus(AdSpace.Status.valueOf(sqlResultSet.getString("ADSPACE_STATUS")));
                adSpace.setUnfilledAction(UnfilledAction.valueOf(sqlResultSet.getString("ADSPACE_UNFILLEDACTION")));
                adSpace.setBackfillEnabled(sqlResultSet.getBoolean("ADSPACE_BACKFILL_ENABLE"));
                adSpace.setColorScheme(AdSpace.ColorScheme.valueOf(sqlResultSet.getString("ADSPACE_COLOR_SCHEME")));

                // Publication
                long publicationId = sqlResultSet.getLong("PUBLICATION_ID");
                if (ineligiblePublicationIds.contains(publicationId)) {
                    // The publication isn't eligible for some reason, which means this adspace isn't eligible
                    continue;
                }

                PublicationDto publication = publicationsById.get(publicationId);
                if (publication == null) {
                    try {
                        publication = loadPublication(publicationsById, publicationId, sqlResultSet, td);
                    } catch (PublicationNotEligibleException e) {
                        // For some reason this publication isn't eligible, which means
                        // this adspace isn't eligible.  Track the ineligibility of the
                        // publication so we don't query for this publication next time.
                        ineligiblePublicationIds.add(publicationId);
                        continue;
                    }

                    // Publisher
                    long publisherId = sqlResultSet.getLong("PUBLISHER_ID");
                    PublisherDto publisher = publishersById.get(publisherId);
                    if (publisher == null) {
                        publisher = loadPublisher(publishersById, publisherId, sqlResultSet);

                        // Company
                        long companyId = sqlResultSet.getLong("PUBLISHER_COMPANY_ID");
                        CompanyDto company = loadCompany(companiesById, companyId, sqlResultSet);
                        publisher.setCompany(company);

                        // RtbConfig
                        Long rtbConfigId = DbUtil.nullableLong(sqlResultSet, "RTB_CONFIG_ID");
                        RtbConfigDto rtbConfig = loadRtbConfig(rtbConfigId, sqlResultSet);
                        publisher.setRtbConfig(rtbConfig);
                    }
                    publication.setPublisher(publisher);

                    // TransparentNetwork
                    Long transparentNetworkId = DbUtil.nullableLong(sqlResultSet, "TRANSPARENT_NETWORK_ID");
                    TransparentNetworkDto transparentNetwork = loadTransparentNetwork(transparentNetworksById, transparentNetworkId, sqlResultSet);
                    publication.setTransparentNetwork(transparentNetwork);

                    // Publication.trusted and Publisher.trusted
                    td.safetyLevelByPublicationIdMap.put(publication.getId(), PublicationSafetyLevel.valueOf(sqlResultSet.getString("PUBLICATION_SAFETY_LEVEL")));

                    // TransientPublicationAttributes for the Publication
                    loadTransientPublicationAttributes(td, publicationId, sqlResultSet);
                }
                adSpace.setPublication(publication);

                // Put the AdSpace in our transient collection irrespective of the
                // publication status.  This will be the collection we use when
                // deriving eligibility.
                td.allAdSpacesById.put(adSpace.getId(), adSpace);
            }
            DbUtils.closeQuietly(sqlResultSet);
            DbUtils.closeQuietly(sqlStatement);

            td.stopWatch("Loading AdSpaces");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initially loaded " + td.allAdSpacesById.size() + " AdSpaces");
            }

            if (td.allAdSpacesById.isEmpty()) {
                // This can happen probably only on QA or Dev  
                // If there are no creatives, follow-up queries will fail miserably, so quit right now  
                LOG.info("No AdSpaces found. Leaving prematurely without followup queries");
                return adSpaceService;
            }

            // We'll need the various sets of ids in comma-separated lists so we can use
            // those to constraint subsequent followup queries via " WHERE ..._ID IN (...)"
            String commaSeparatedAdSpaceIds = StringUtils.join(td.allAdSpacesById.keySet(), ',');
            String commaSeparatedPublicationIds = StringUtils.join(publicationsById.keySet(), ',');
            String commaSeparatedPublisherIds = StringUtils.join(publishersById.keySet(), ',');

            //loadDormantAdSpaceExternalIds(adSpaceService);

            loadAdspaceFormats(sqlConnection, commaSeparatedAdSpaceIds, td);

            loadAdspaceApprovedFeatures(sqlConnection, commaSeparatedAdSpaceIds, td);

            loadAdspaceDeniedFeatures(sqlConnection, commaSeparatedAdSpaceIds, td);

            loadPublicationLanguages(sqlConnection, commaSeparatedPublicationIds, td, publicationsById);

            loadPublicationRateMap(sqlConnection, commaSeparatedPublicationIds, publicationsById, td);

            loadPublicationMinimumEcpmRateMap(sqlConnection, commaSeparatedPublicationIds, publicationsById, td);

            overridePublicationSamplingRates(sqlConnection, commaSeparatedPublicationIds, publicationsById, td);

            loadPublisherMinimumEcpmRateMap(sqlConnection, commaSeparatedPublisherIds, publishersById, td);

            loadDefaultIntegrationTypeIdsByPublicationTypeId(sqlConnection, commaSeparatedPublisherIds, publishersById, td);

            loadPublisherDefaultRateCardMap(sqlConnection, commaSeparatedPublisherIds, publishersById, td);

            if (!transparentNetworksById.isEmpty()) {
                String commaSeparatedTransparentNetworkIds = StringUtils.join(transparentNetworksById.keySet(), ',');
                loadTransparentNetworkAdvertiser(sqlConnection, commaSeparatedTransparentNetworkIds, transparentNetworksById, td);
                loadTranparentNetworkRateCardmap(sqlConnection, commaSeparatedTransparentNetworkIds, transparentNetworksById, td);
            }

            loadPublicationThatMayViewPricing(sqlConnection, commaSeparatedPublicationIds, adSpaceService, td);

            loadPublicationApprovedCreatives(sqlConnection, commaSeparatedPublicationIds, td);

            loadPublicationDeniedCreatives(sqlConnection, commaSeparatedPublicationIds, td);

            loadPublisherAllowedExtendedCreativeTypes(sqlConnection, td);

            loadPublicationAllowedExtendedCreativeTypes(sqlConnection, commaSeparatedPublicationIds, td);

            loadPublicationExcludedCategories(sqlConnection, commaSeparatedPublicationIds, td);

            loadPublsherExcludedCategories(sqlConnection, shardMode, publisherIds, td);

            loadPublicationStatedCategories(sqlConnection, commaSeparatedPublicationIds, td);

            loadPublisherApprovedCreatives(sqlConnection, commaSeparatedPublisherIds, td);

            loadPublisherDeniedBidTypes(sqlConnection, commaSeparatedPublisherIds, td);

            loadPublicationDeniedBidTypes(sqlConnection, commaSeparatedPublicationIds, td);

            if (isRtbEnabled) {
                //If it is RTB Enabled make sure we load all required Publishers,
                //even if there are no Adspace and publication under it
                loadRtbPublishers(sqlConnection, publisherIds, adSpaceService, shardMode, publishersById, td);
            }
        } finally {
            DbUtils.closeQuietly(sqlResultSet);
            DbUtils.closeQuietly(sqlStatement);
        }
        int count = 0;
        for (AdSpaceDto adSpace : td.allAdSpacesById.values()) {
            // But only include it for adserver if the pub status is ACTIVE or PENDING.
            switch (adSpace.getPublication().getStatus()) {
            case ACTIVE:
                // It's eligible for service under adserver
                adSpaceService.addAddSpaceToCache(adSpace);
                break;
            case PENDING:
                // AF-1185 - RTB publications that are PENDING should *not* go to adserver
                // and should not serve any traffic.  Until an RTB publication is approved,
                // it needs to be considered ineligible to serve.
                if (StringUtils.isEmpty(adSpace.getPublication().getRtbId())) {
                    // It's eligible for service under adserver
                    adSpaceService.addAddSpaceToCache(adSpace);
                }
                break;
            default:
                break;
            }

            // RTB setup
            /*
            if (!StringUtils.isBlank(adSpace.getPublication().getRtbId())) {
                Map<String,AdSpaceDto> publisherRtbAdSpaces = cache.rtbAdSpaces.get(adSpace.getPublication().getPublisher().getId());
                if (publisherRtbAdSpaces == null) {
                    publisherRtbAdSpaces = new HashMap<String,AdSpaceDto>();
                    cache.rtbAdSpaces.put(adSpace.getPublication().getPublisher().getId(), publisherRtbAdSpaces);
                }
                publisherRtbAdSpaces.put(adSpace.getPublication().getRtbId(), adSpace);
            }
            */
            //TODO : need to see if we need RTB Cache seprately
            //adSpaceService.addRtbPublicationAdSpace(adSpace);
            count++;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded " + count + " AdSpaces");
        }

        return adSpaceService;
    }

    private String createWhereClause(String quotedIneligibleAdSpaceStatuses, String quotedPublicationStatuses, ShardMode shardMode, Set<Long> publishrIds, AdCacheBuildContext td) {

        String whereClause = " WHERE ads.STATUS NOT IN (" + quotedIneligibleAdSpaceStatuses + ")" + " AND pun.STATUS IN (" + quotedPublicationStatuses + ")";

        // Debuging single publication/adspace eligibility - not a normal DS run
        boolean debugQuery = false;
        if (td.debugAdSpaceId != null) {
            LOG.info("Debugging adSpaceId found: " + td.debugAdSpaceId);
            whereClause = whereClause + " AND ads.ID=" + td.debugAdSpaceId;
            debugQuery = true;
        }
        if (td.debugPublicationId != null) {
            LOG.info("Debugging publicationId found: " + td.debugPublicationId);
            whereClause = whereClause + " AND pun.ID=" + td.debugPublicationId;
            debugQuery = true;
        }
        if (debugQuery) {
            // Do not add publishers conditions when debuging as they are not really needed (reconsider if it can speedup query)
            return whereClause;
        }

        String commaSeparatedPublisherIds = StringUtils.join(publishrIds, ',');
        if (shardMode.equals(ShardMode.exclude)) {
            if (commaSeparatedPublisherIds != null && !commaSeparatedPublisherIds.trim().equals("")) {
                whereClause = whereClause + " AND pbs.ID NOT IN (" + commaSeparatedPublisherIds + ")";
            }
        }
        if (shardMode.equals(ShardMode.include)) {
            if (commaSeparatedPublisherIds != null && !commaSeparatedPublisherIds.trim().equals("")) {
                whereClause = whereClause + " AND pbs.ID IN (" + commaSeparatedPublisherIds + ")";
            } else {
                LOG.warn("Publisherids can not be null or empty while shardMode is included");
            }

        }

        return whereClause;
    }

    private PublicationDto loadPublication(Map<Long, PublicationDto> publicationsById, long publicationId, ResultSet rs, AdCacheBuildContext td) throws SQLException,
            PublicationNotEligibleException {
        PublicationDto publication = publicationsById.get(publicationId);
        if (publication == null) {
            publication = new PublicationDto();
            publication.setId(publicationId);
            publication.setExternalID(rs.getString("PUBLICATION_EXTERNAL_ID"));
            publication.setName(rs.getString("PUBLICATION_NAME"));
            publication.setStatus(Publication.Status.valueOf(rs.getString("PUBLICATION_STATUS")));
            publication.setPublicationTypeId(rs.getLong("PUBLICATION_TYPE_ID"));
            publication.setInstallTrackingDisabled(rs.getBoolean("PUBLICATION_INSTALL_TRACKING_DISABLED"));
            publication.setTrackingIdentifierType(TrackingIdentifierType.valueOf(rs.getString("PUBLICATION_TRACKING_IDENT_TYPE")));
            publication.setAdRequestTimeout(DbUtil.nullableLong(rs, "PUBLICATION_AD_REQUEST_TIMEOUT"));
            publication.setDefaultIntegrationTypeId(DbUtil.nullableLong(rs, "PUBLICATION_DEFULT_INTEGRATION_TYPE_ID"));
            publication.setRtbId(rs.getString("PUBLICATION_RTB_ID"));
            publication.setCategoryId(rs.getLong("PUBLICATION_CATEGORY_ID"));
            publication.setApproveDate(rs.getTimestamp("PUBLICATION_APPROVED_DATE"));
            publication.setSamplingRate(publicationDefaultSamplingRate);
            publication.setUseSoftFloor(td.getPublicationBooleanAttribute(publicationId, "SOFT_FLOOR", false));
            if (StringUtils.isNotEmpty(rs.getString("PUBLICATION_AD_OPS_STATUS"))) {
                publication.setAdOpsStatus(Publication.AdOpsStatus.valueOf(rs.getString("PUBLICATION_AD_OPS_STATUS")));
            }
            if (Publication.Status.PENDING.equals(publication.getStatus()) && StringUtils.isNotEmpty(publication.getRtbId())) {
                // AF-1185 - RTB pubs that are PENDING aren't eligible to serve
                throw new PublicationNotEligibleException("RTB Publications that are PENDING are not eligible");
            }
            publication.setBundleName(rs.getString("BUNDLE_NAME"));

            publicationsById.put(publication.getId(), publication);
        }
        return publication;
    }

    private PublisherDto loadPublisher(Map<Long, PublisherDto> publishersById, long publisherId, ResultSet rs) throws SQLException {
        PublisherDto publisher = publishersById.get(publisherId);
        //
        if (publisher == null) {
            publisher = new PublisherDto();
            publisher.setId(publisherId);
            publisher.setPendingAdType(PendingAdType.valueOf(rs.getString("PUBLISHER_PENDING_AD_TYPE")));
            publisher.setDefaultAdRequestTimeout(rs.getLong("PUBLISHER_DEFAULT_AD_REQUEST_AD_TYPE"));
            publisher.setCurrentRevShare(rs.getDouble("PUBLISHER_REV_SHARE_CURRENT_VALUE"));
            publisher.setExternalId(rs.getString("PUBLISHER_EXTERNAL_ID"));
            publisher.setBuyerPremium(rs.getDouble("BUYER_PREMIUM"));
            publisher.setRequiresRealDestination(rs.getBoolean("PUBLISHER_REQUIRES_REAL_DESTINATION"));
            publishersById.put(publisher.getId(), publisher);
        }
        return publisher;
    }

    private CompanyDto loadCompany(Map<Long, CompanyDto> companiesById, long companyId, ResultSet rs) throws SQLException {
        CompanyDto company = companiesById.get(companyId);
        if (company == null) {
            company = new com.adfonic.domain.cache.dto.adserver.adspace.CompanyDto();
            company.setId(companyId);
            companiesById.put(company.getId(), company);
        }
        return company;
    }

    private RtbConfigDto loadRtbConfig(Long rtbConfigId, ResultSet rs) throws SQLException {
        // RtbConfig
        //+ ", rtc.ID AS RTB_CONFIG_ID, rtc.AD_MODE AS RTB_CONFIG_AD_MODE, rtc.WIN_NOTICE_MODE AS RTB_CONFIG_WIN_NOTICE_MODE"
        if (rtbConfigId != null) {
            RtbConfigDto rtbConfig = new RtbConfigDto();
            rtbConfig.setId(rtbConfigId);
            rtbConfig.setAdMode(RtbConfig.RtbAdMode.valueOf(rs.getString("RTB_CONFIG_AD_MODE")));
            rtbConfig.setWinNoticeMode(RtbConfig.RtbWinNoticeMode.valueOf(rs.getString("RTB_CONFIG_WIN_NOTICE_MODE")));
            rtbConfig.setAdmProfile(RtbConfig.AdmProfile.valueOf(rs.getString("RTB_CONFIG_ADM_PROFILE")));
            rtbConfig.setSpMacro(rs.getString("RTB_CONFIG_SP_MACRO"));
            rtbConfig.setEscapedClickForwardURL(rs.getString("RTB_CONFIG_ESCD_CLICK_FORWARD_URL"));
            rtbConfig.setClickForwardValidationPattern(rs.getString("RTB_CONFIG_CLCK_FWD_VALDN_PATTERN"));
            rtbConfig.setDpidFallback(rs.getString("RTB_CONFIG_DPID_FALLBACK"));
            rtbConfig.setPrefixonEscapedURLs(rs.getString("RTB_CONFIG_ESCAPED_URL_PREFIX"));
            rtbConfig.setIntegrationTypePrefix(rs.getString("RTB_CONFIG_INTEGRATION_TYPE_PREFIX"));
            rtbConfig.setBidCurrency(rs.getString("RTB_BID_CURRENCY"));
            rtbConfig.setAuctionType(RtbAuctionType.valueOf(rs.getString("RTB_AUCTION_TYPE")));
            rtbConfig.setRtbLostTimeDuration(rs.getLong("BID_EXPIRY_TIME_SECONDS") * 1000);//Convert into milliseconds
            rtbConfig.setSslRequired(rs.getBoolean("SSL_REQUIRED"));
            String decryptionScheme = rs.getString("RTB_CONFIG_DECRYPTION_SCHEME");
            if (decryptionScheme != null) {
                rtbConfig.setDecryptionScheme(RtbConfig.DecryptionScheme.valueOf(decryptionScheme));
            }
            rtbConfig.setSecurityAlias(rs.getString("RTB_CONFIG_SEC_ALIAS"));
            rtbConfig.setImpTrackMode(RtbImpTrackMode.valueOf(rs.getString("RTB_IMP_TRACK_MODE")));
            return rtbConfig;
        }
        return null;
    }

    private TransparentNetworkDto loadTransparentNetwork(Map<Long, TransparentNetworkDto> transparentNetworksById, Long transparentNetworkId, ResultSet rs) throws SQLException {
        TransparentNetworkDto transparentNetwork = null;
        if (transparentNetworkId != null) {
            transparentNetwork = transparentNetworksById.get(transparentNetworkId);
            if (transparentNetwork == null) {
                transparentNetwork = new TransparentNetworkDto();
                transparentNetwork.setId(transparentNetworkId);
                transparentNetwork.setClosed(rs.getBoolean("TRANSPARENT_NETWORK_CLOSED"));
                transparentNetworksById.put(transparentNetwork.getId(), transparentNetwork);
            }
        }
        return transparentNetwork;

    }

    private void loadTransientPublicationAttributes(AdCacheBuildContext td, Long publicationId, ResultSet rs) throws SQLException {
        TransientPublicationAttributes pubAttrs = td.publicationAttributesByPublicationId.get(publicationId);
        if (pubAttrs == null) {
            //p0.MIN_AGE AS , p0.MAX_AGE AS , p0.INCENTIVIZED AS , p0.GENDER_MIX AS "
            pubAttrs = new TransientPublicationAttributes();
            pubAttrs.autoApproval = rs.getBoolean("PUBLICATION_AUTO_APPROVAL");
            pubAttrs.minAge = rs.getInt("PUBLICATION_MIN_AGE");
            pubAttrs.maxAge = rs.getInt("PUBLICATION_MAX_AGE");
            pubAttrs.incentivized = rs.getBoolean("PUBLICATION_INCENTIVIZED");
            pubAttrs.genderMix = rs.getBigDecimal("PUBLICATION_GENDER_MIX");
            td.publicationAttributesByPublicationId.put(publicationId, pubAttrs);
        }
    }

    private void loadAdspaceFormats(Connection conn, String commaSeparatedAdSpaceIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading AdSpace Formats");
        String sql = "SELECT AD_SPACE_ID, FORMAT_ID FROM AD_SPACE_FORMAT WHERE AD_SPACE_ID IN (" + commaSeparatedAdSpaceIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                AdSpaceDto adSpace = td.allAdSpacesById.get(rs.getLong("AD_SPACE_ID"));
                adSpace.getFormatIds().add(rs.getLong("FORMAT_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading AdSpace Formats");
        }
    }

    private void loadPublicationAttributes(Connection conn, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication Attributes");
        //Loading all values from this table as its going to be a samll table for now, can do where caluse later if become bigger
        String sql = "SELECT PUBLICATION_ID, NAME, VALUE FROM PUBLICATION_ATTRIBUTES_MAP";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                td.addPublicationAttribute(rs.getLong("PUBLICATION_ID"), rs.getString("NAME"), rs.getString("VALUE"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Attributes");
        }
    }

    private void loadAdspaceApprovedFeatures(Connection conn, String commaSeparatedAdSpaceIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading AdSpace Approved Features");
        String sql = "SELECT AD_SPACE_ID, FEATURE FROM AD_SPACE_APPROVED_FEATURE WHERE AD_SPACE_ID IN (" + commaSeparatedAdSpaceIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long adSpaceId = rs.getLong("AD_SPACE_ID");
                Feature feature = Feature.valueOf(rs.getString("FEATURE"));
                Set<Feature> features = td.approvedFeaturesByAdSpaceId.get(adSpaceId);
                if (features == null) {
                    features = new HashSet<Feature>();
                    td.approvedFeaturesByAdSpaceId.put(adSpaceId, features);
                }
                features.add(feature);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading AdSpace Approved Features");
        }
    }

    private void loadAdspaceDeniedFeatures(Connection conn, String commaSeparatedAdSpaceIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading AdSpace Denied Features");
        String sql = "SELECT AD_SPACE_ID, FEATURE FROM AD_SPACE_DENIED_FEATURE WHERE AD_SPACE_ID IN (" + commaSeparatedAdSpaceIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long adSpaceId = rs.getLong(1);
                Feature feature = Feature.valueOf(rs.getString(2));
                Set<Feature> features = td.deniedFeaturesByAdSpaceId.get(adSpaceId);
                if (features == null) {
                    features = new HashSet<Feature>();
                    td.deniedFeaturesByAdSpaceId.put(adSpaceId, features);
                }
                features.add(feature);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading AdSpace Denied Features");
        }
    }

    /**
     * We will load RtbPublishers upfront as there are cases when RTB Publisher Exists
     * but there is no Adspace or publication under it
     *
     * @param conn
     * @param publishrIds
     * @param adSpaceService
     * @param shardMode TODO
     * @param publishersById 
     * @param td 
     * @throws SQLException
     */
    private void loadRtbPublishers(Connection conn, Set<Long> publishrIds, AdSpaceService adSpaceService, ShardMode shardMode, Map<Long, PublisherDto> publishersById,
            AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading RTB Publishers");
        String sql = "SELECT p1.EXTERNAL_ID AS PUBLISHER_EXTERNAL_ID, p1.ID as PUBLISHER_ID,"
                + " a1.OPERATING_PUBLISHER_ID AS OPERATING_PUBLISHER_ID, a1.ASSOCIATE_REFERENCE AS OPERATING_PUBLISHER_REFERENCE"
                + " FROM PUBLISHER p1 LEFT OUTER JOIN ASSOCIATED_PUBLISHERS a1 ON a1.PUBLISHER_ID=p1.ID";
        String commaSeparatedPublisherIds = StringUtils.join(publishrIds, ',');

        if (shardMode != ShardMode.all) {
            String whereClause = " WHERE p1.ID " + (shardMode == ShardMode.exclude ? "NOT " : "") + "IN (" + commaSeparatedPublisherIds + ")";
            sql = sql + " " + whereClause;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }

        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            long publisherId;
            String publisherExternalId;
            while (rs.next()) {
                publisherExternalId = rs.getString("PUBLISHER_EXTERNAL_ID");
                publisherId = rs.getLong("PUBLISHER_ID");
                Long operatorId = DbUtil.nullableLong(rs, "OPERATING_PUBLISHER_ID");
                if (operatorId != null) {
                    adSpaceService.addAssociatePublisher(publisherId, rs.getString("OPERATING_PUBLISHER_REFERENCE"), operatorId);
                    PublisherDto publisher = publishersById.get(publisherId);
                    if (publisher != null) {
                        publisher.setOperatingPublisherId(operatorId);
                    }
                }

                adSpaceService.addPublisherByExternalId(publisherExternalId, publisherId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading RTB Publishers");
        }
    }

    private void loadPublicationLanguages(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td, Map<Long, PublicationDto> publicationsById)
            throws SQLException {
        td.startWatch("Loading Publication language");
        String sql = "SELECT PUBLICATION_ID, LANGUAGE_ID FROM PUBLICATION_LANGUAGE WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublicationDto publication = publicationsById.get(rs.getLong("PUBLICATION_ID"));
                publication.getLanguageIds().add(rs.getLong("LANGUAGE_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication language");
        }
    }

    private void loadPublicationRateMap(Connection conn, String commaSeparatedPublicationIds, Map<Long, PublicationDto> publicationsById, AdCacheBuildContext td)
            throws SQLException {
        td.startWatch("Loading Publication RateMap");
        String sql = "SELECT p0.PUBLICATION_ID AS PUBLICATION_ID, p0.BID_TYPE AS BID_TYPE" //
                + ", r0.ID AS RATE_CARD_ID, r0.DEFAULT_MINIMUM AS RATE_CARD_DEFAULT_MINIMUM" //
                + ", r1.COUNTRY_ID AS RATE_CARD_COUNTRY_ID, r1.AMOUNT AS RATE_CARD_AMOUNT" //
                + " FROM PUBLICATION_RATE_CARD_MAP p0" //
                + " JOIN RATE_CARD r0 ON r0.ID=p0.RATE_CARD_ID"
                // We use outer join here since there may not be any per-country
                // minimums, just the rate card and its default minimum
                + " LEFT OUTER JOIN RATE_CARD_MINIMUM_BID_MAP r1 ON r1.RATE_CARD_ID=r0.ID" //
                + " WHERE p0.PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublicationDto publication = publicationsById.get(rs.getLong("PUBLICATION_ID"));
                BidType bidType = BidType.valueOf(rs.getString("BID_TYPE"));
                RateCardDto rateCard = publication.getRateCardMap().get(bidType);
                if (rateCard == null) {
                    rateCard = new RateCardDto();
                    rateCard.setId(rs.getLong("RATE_CARD_ID"));
                    rateCard.setDefaultMinimum(rs.getBigDecimal("RATE_CARD_DEFAULT_MINIMUM"));
                    publication.getRateCardMap().put(bidType, rateCard);
                }
                // The rate card may or may not have per-country minimums
                if (rs.getObject("RATE_CARD_COUNTRY_ID") != null) {
                    rateCard.getMinimumBidsByCountryId().put(rs.getLong("RATE_CARD_COUNTRY_ID"), rs.getBigDecimal("RATE_CARD_AMOUNT"));
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication RateMap");
        }
    }

    private void overridePublicationSamplingRates(Connection conn, String commaSeparatedPublicationIds, Map<Long, PublicationDto> publicationsById, AdCacheBuildContext td)
            throws SQLException {
        td.startWatch("Loading Publication Sampling Rates");
        String sql = "SELECT s0.PUBLICATION_ID AS PUBLICATION_ID, s0.SAMPLING_RATE AS PUBLICATION_SAMPLING_RATE" + " FROM PUBLICATION_SAMPLINGRATE s0"
                + " WHERE s0.PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublicationDto publication = publicationsById.get(rs.getLong("PUBLICATION_ID"));
                publication.setSamplingRate(rs.getInt("PUBLICATION_SAMPLING_RATE"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Sampling Rates");
        }
    }

    private void loadPublicationMinimumEcpmRateMap(Connection conn, String commaSeparatedPublicationIds, Map<Long, PublicationDto> publicationsById, AdCacheBuildContext td)
            throws SQLException {
        td.startWatch("Loading Publication ECPM RateMap");
        String sql = "SELECT p0.ID AS PUBLICATION_ID" + ", r0.ID AS RATE_CARD_ID, r0.DEFAULT_MINIMUM AS RATE_CARD_DEFAULT_MINIMUM"
                + ", r1.COUNTRY_ID AS RATE_CARD_COUNTRY_ID, r1.AMOUNT AS RATE_CARD_AMOUNT" + " FROM PUBLICATION p0"
                // No need for outer join here
                + " JOIN RATE_CARD r0 ON r0.ID=p0.ECPM_TARGET_RATE_CARD_ID"
                // We use outer join here since there may not be any per-country
                // minimums, just the rate card and its default minimum
                + " LEFT OUTER JOIN RATE_CARD_MINIMUM_BID_MAP r1 ON r1.RATE_CARD_ID=r0.ID" + " WHERE p0.ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublicationDto publication = publicationsById.get(rs.getLong("PUBLICATION_ID"));
                RateCardDto ecpmTargetRateCard = publication.getEcpmTargetRateCard();
                if (ecpmTargetRateCard == null) {
                    ecpmTargetRateCard = new RateCardDto();
                    ecpmTargetRateCard.setId(rs.getLong("RATE_CARD_ID"));
                    ecpmTargetRateCard.setDefaultMinimum(rs.getBigDecimal("RATE_CARD_DEFAULT_MINIMUM"));
                    publication.setEcpmTargetRateCard(ecpmTargetRateCard);
                }
                // The rate card may or may not have per-country minimums
                if (rs.getObject("RATE_CARD_COUNTRY_ID") != null) {
                    ecpmTargetRateCard.getMinimumBidsByCountryId().put(rs.getLong("RATE_CARD_COUNTRY_ID"), rs.getBigDecimal("RATE_CARD_AMOUNT"));
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication ECPM RateMap");
        }
    }

    private void loadPublisherMinimumEcpmRateMap(Connection conn, String commaSeparatedPublisherIds, Map<Long, PublisherDto> publishersById, AdCacheBuildContext td)
            throws SQLException {
        td.startWatch("Loading Publisher ECPM RateMap");
        String sql = "SELECT p0.ID AS PUBLISHER_ID" + ", r0.ID AS RATE_CARD_ID, r0.DEFAULT_MINIMUM AS RATE_CARD_DEFAULT_MINIMUM"
                + ", r1.COUNTRY_ID AS RATE_CARD_COUNTRY_ID, r1.AMOUNT AS RATE_CARD_AMOUNT" + " FROM PUBLISHER p0"
                // No need for outer join here
                + " JOIN RATE_CARD r0 ON r0.ID=p0.ECPM_TARGET_RATE_CARD_ID"
                // We use outer join here since there may not be any per-country
                // minimums, just the rate card and its default minimum
                + " LEFT OUTER JOIN RATE_CARD_MINIMUM_BID_MAP r1 ON r1.RATE_CARD_ID=r0.ID" + " WHERE p0.ID IN (" + commaSeparatedPublisherIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublisherDto publisher = publishersById.get(rs.getLong("PUBLISHER_ID"));
                RateCardDto ecpmTargetRateCard = publisher.getEcpmTargetRateCard();
                if (ecpmTargetRateCard == null) {
                    ecpmTargetRateCard = new RateCardDto();
                    ecpmTargetRateCard.setId(rs.getLong("RATE_CARD_ID"));
                    ecpmTargetRateCard.setDefaultMinimum(rs.getBigDecimal("RATE_CARD_DEFAULT_MINIMUM"));
                    publisher.setEcpmTargetRateCard(ecpmTargetRateCard);
                }
                // The rate card may or may not have per-country minimums
                if (rs.getObject("RATE_CARD_COUNTRY_ID") != null) {
                    ecpmTargetRateCard.getMinimumBidsByCountryId().put(rs.getLong("RATE_CARD_COUNTRY_ID"), rs.getBigDecimal("RATE_CARD_AMOUNT"));
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publisher ECPM RateMap");
        }
    }

    private void loadDefaultIntegrationTypeIdsByPublicationTypeId(Connection conn, String commaSeparatedPublisherIds, Map<Long, PublisherDto> publishersById, AdCacheBuildContext td)
            throws SQLException {
        td.startWatch("Loading DefaultIntegrationTypeIdsByPublicationTypeId");
        String sql = "SELECT PUBLISHER_ID, PUBLICATION_TYPE_ID, INTEGRATION_TYPE_ID FROM PUBLISHER_DEFAULT_INTEGRATION_TYPE_MAP WHERE PUBLISHER_ID IN ("
                + commaSeparatedPublisherIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublisherDto publisher = publishersById.get(rs.getLong("PUBLISHER_ID"));
                publisher.getDefaultIntegrationTypeIdsByPublicationTypeId().put(rs.getLong("PUBLICATION_TYPE_ID"), rs.getLong("INTEGRATION_TYPE_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading DefaultIntegrationTypeIdsByPublicationTypeId");
        }
    }

    private void loadPublisherDefaultRateCardMap(Connection conn, String commaSeparatedPublisherIds, Map<Long, PublisherDto> publishersById, AdCacheBuildContext td)
            throws SQLException {
        td.startWatch("Loading Publisher Default Rate Card Map");
        String sql = "SELECT p0.PUBLISHER_ID AS PUBLISHER_ID, p0.BID_TYPE AS PUBLISHER_BID_TYPE" + ", r0.ID AS RATE_CARD_ID, r0.DEFAULT_MINIMUM AS RATE_CARD_DEFAULT_MINIMUM"
                + ", r1.COUNTRY_ID AS RATE_CARD_COUNTRY_ID, r1.AMOUNT AS RATE_CARD_AMOUNT" + " FROM PUBLISHER_DEFAULT_RATE_CARD_MAP p0"
                + " JOIN RATE_CARD r0 ON r0.ID=p0.RATE_CARD_ID"
                // We use outer join here since there may not be any per-country
                // minimums, just the rate card and its default minimum
                + " LEFT OUTER JOIN RATE_CARD_MINIMUM_BID_MAP r1 ON r1.RATE_CARD_ID=r0.ID" + " WHERE p0.PUBLISHER_ID IN (" + commaSeparatedPublisherIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                PublisherDto publisher = publishersById.get(rs.getLong("PUBLISHER_ID"));
                BidType bidType = BidType.valueOf(rs.getString("PUBLISHER_BID_TYPE"));
                RateCardDto rateCard = publisher.getDefaultRateCardMap().get(bidType);
                if (rateCard == null) {
                    rateCard = new RateCardDto();
                    rateCard.setId(rs.getLong("RATE_CARD_ID"));
                    rateCard.setDefaultMinimum(rs.getBigDecimal("RATE_CARD_DEFAULT_MINIMUM"));
                    publisher.getDefaultRateCardMap().put(bidType, rateCard);
                }
                // The rate card may or may not have per-country minimums
                if (rs.getObject("RATE_CARD_COUNTRY_ID") != null) {
                    rateCard.getMinimumBidsByCountryId().put(rs.getLong("RATE_CARD_COUNTRY_ID"), rs.getBigDecimal("RATE_CARD_AMOUNT"));
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publisher Default Rate Card Map");
        }
    }

    private void loadTransparentNetworkAdvertiser(Connection conn, String commaSeparatedTransparentNetworkIds, Map<Long, TransparentNetworkDto> transparentNetworksById,
            AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Transaparent Network Advertiser");
        String sql = "SELECT TRANSPARENT_NETWORK_ID, COMPANY_ID FROM COMPANY_TRANSPARENT_NETWORK WHERE TRANSPARENT_NETWORK_ID IN (" + commaSeparatedTransparentNetworkIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                TransparentNetworkDto transparentNetwork = transparentNetworksById.get(rs.getLong("TRANSPARENT_NETWORK_ID"));
                transparentNetwork.getAdvertiserCompanyIds().add(rs.getLong("COMPANY_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Transaparent Network Advertiser");
        }
    }

    private void loadTranparentNetworkRateCardmap(Connection conn, String commaSeparatedTransparentNetworkIds, Map<Long, TransparentNetworkDto> transparentNetworksById,
            AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Transaparent Network Rate Card Map");
        String sql = "SELECT p0.TRANSPARENT_NETWORK_ID AS TRANSPARENT_NETWORK_ID, p0.BID_TYPE AS TRANSPARENT_NETWORK_RATE_CARD_MAP_BID_TYPE"
                + ", r0.ID AS RATE_CARD_ID, r0.DEFAULT_MINIMUM AS RATE_CARD_DEFAULT_MINIMUM"
                + ", r1.COUNTRY_ID AS RATE_CARD_MINIMUM_BID_MAP_COUNTRY_ID, r1.AMOUNT AS RATE_CARD_MINIMUM_BID_MAP_AMOUNT" + " FROM TRANSPARENT_NETWORK_RATE_CARD_MAP p0"
                + " JOIN RATE_CARD r0 ON r0.ID=p0.RATE_CARD_ID"
                // We use outer join here since there may not be any per-country
                // minimums, just the rate card and its default minimum
                + " LEFT OUTER JOIN RATE_CARD_MINIMUM_BID_MAP r1 ON r1.RATE_CARD_ID=r0.ID" + " WHERE p0.TRANSPARENT_NETWORK_ID IN (" + commaSeparatedTransparentNetworkIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                TransparentNetworkDto transparentNetwork = transparentNetworksById.get(rs.getLong("TRANSPARENT_NETWORK_ID"));
                BidType bidType = BidType.valueOf(rs.getString("TRANSPARENT_NETWORK_RATE_CARD_MAP_BID_TYPE"));
                RateCardDto rateCard = transparentNetwork.getRateCardMap().get(bidType);
                if (rateCard == null) {
                    rateCard = new RateCardDto();
                    rateCard.setId(rs.getLong("RATE_CARD_ID"));
                    rateCard.setDefaultMinimum(rs.getBigDecimal("RATE_CARD_DEFAULT_MINIMUM"));
                    transparentNetwork.getRateCardMap().put(bidType, rateCard);
                }
                // The rate card may or may not have per-country minimums
                if (rs.getObject("RATE_CARD_MINIMUM_BID_MAP_COUNTRY_ID") != null) {
                    rateCard.getMinimumBidsByCountryId().put(rs.getLong("RATE_CARD_MINIMUM_BID_MAP_COUNTRY_ID"), rs.getBigDecimal("RATE_CARD_MINIMUM_BID_MAP_AMOUNT"));
                }
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Transaparent Network Rate Card Map");
        }
    }

    private void loadPublicationThatMayViewPricing(Connection conn, String commaSeparatedPublicationIds, AdSpaceService adSpaceService, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication That May View Pricing");
        String sql = "SELECT DISTINCT PUBLICATION.ID AS PUBLICATION_ID FROM PUBLICATION INNER JOIN PUBLISHER ON PUBLISHER.ID=PUBLICATION.PUBLISHER_ID INNER JOIN COMPANY ON COMPANY.ID=PUBLISHER.COMPANY_ID INNER JOIN USER ON USER.COMPANY_ID=COMPANY.ID INNER JOIN USER_ROLE ON USER_ROLE.USER_ID=USER.ID INNER JOIN ROLE ON ROLE.ID=USER_ROLE.ROLE_ID WHERE ROLE.NAME='MayViewPricing' AND PUBLICATION.ID IN ("
                + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                adSpaceService.addPublicationMayViewPricing(rs.getLong("PUBLICATION_ID"));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication That May View Pricing");
        }
    }

    private void loadPublicationApprovedCreatives(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication Approved Creatives");
        String sql = "SELECT PUBLICATION_ID, CREATIVE_ID FROM PUBLICATION_APPROVED_CREATIVE WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publicationId = rs.getLong("PUBLICATION_ID");
                long creativeId = rs.getLong("CREATIVE_ID");
                Set<Long> creativeIds = td.publicationApprovedCreativeIds.get(publicationId);
                if (creativeIds == null) {
                    creativeIds = new HashSet<Long>();
                    td.publicationApprovedCreativeIds.put(publicationId, creativeIds);
                }
                creativeIds.add(creativeId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Approved Creatives");
        }
    }

    private void loadPublicationDeniedCreatives(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication Denied Creatives");
        String sql = "SELECT PUBLICATION_ID, CREATIVE_ID FROM PUBLICATION_DENIED_CREATIVE WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publicationId = rs.getLong("PUBLICATION_ID");
                long creativeId = rs.getLong("CREATIVE_ID");
                Set<Long> creativeIds = td.publicationDeniedCreativeIds.get(publicationId);
                if (creativeIds == null) {
                    creativeIds = new HashSet<Long>();
                    td.publicationDeniedCreativeIds.put(publicationId, creativeIds);
                }
                creativeIds.add(creativeId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Denied Creatives");
        }
    }

    /**
     * Publisher level whitelist of extended creatives 
     */
    private void loadPublisherAllowedExtendedCreativeTypes(Connection conn, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publisher allowed extended-creatives");
        String sql = "SELECT PUBLISHER_ID, EXTENDED_CREATIVE_TYPE_ID FROM PUBLISHER_EXTENDED_CREATIVE_TYPE";
        LOG.debug(sql);
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publisherId = rs.getLong("PUBLISHER_ID");
                long extendedCreativeTypeId = rs.getLong("EXTENDED_CREATIVE_TYPE_ID");
                Set<Long> extendedCreativeTypeIds = td.publisherAllowedExtendedCreativeTypeIds.get(publisherId);
                if (extendedCreativeTypeIds == null) {
                    extendedCreativeTypeIds = new HashSet<Long>();
                    td.publisherAllowedExtendedCreativeTypeIds.put(publisherId, extendedCreativeTypeIds);
                }
                extendedCreativeTypeIds.add(extendedCreativeTypeId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publisher allowed extended-creatives");
        }
    }

    private void loadPublicationAllowedExtendedCreativeTypes(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication allowed extended-creatives");
        String sql = "SELECT PUBLICATION_ID, EXTENDED_CREATIVE_TYPE_ID FROM PUBLICATION_EXTENDED_CREATIVE_TYPE WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publicationId = rs.getLong("PUBLICATION_ID");
                long extendedCreativeTypeId = rs.getLong("EXTENDED_CREATIVE_TYPE_ID");
                Set<Long> extendedCreativeTypeIds = td.publicationAllowedExtendedCreativeTypeIds.get(publicationId);
                if (extendedCreativeTypeIds == null) {
                    extendedCreativeTypeIds = new HashSet<Long>();
                    td.publicationAllowedExtendedCreativeTypeIds.put(publicationId, extendedCreativeTypeIds);
                }
                extendedCreativeTypeIds.add(extendedCreativeTypeId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication allowed extended-creatives");
        }
    }

    private void loadPublicationExcludedCategories(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication Excluded");
        String sql = "SELECT PUBLICATION_ID, CATEGORY_ID FROM PUBLICATION_EXCLUDED_CATEGORY WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publicationId = rs.getLong("PUBLICATION_ID");
                long categoryId = rs.getLong("CATEGORY_ID");
                Set<Long> expanded = td.expandedPublicationExcludedCategoryIds.get(publicationId);
                if (expanded == null) {
                    expanded = new HashSet<Long>();
                    td.expandedPublicationExcludedCategoryIds.put(publicationId, expanded);
                }
                expanded.addAll(td.expandedCategoryIdsByCategoryId.get(categoryId));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Excluded");
        }
    }

    private void loadPublicationDeniedBidTypes(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication Denied BidType");
        String sql = "SELECT PUBLICATION_ID, BID_TYPE FROM PUBLICATION_BLOCKED_BID_TYPE WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            String bidTypeText;
            long publicationId;
            BidType bidType;
            Set<BidType> publicationBlockedBidTypes;
            int count = 0;
            while (rs.next()) {
                publicationId = rs.getLong("PUBLICATION_ID");
                bidTypeText = rs.getString("BID_TYPE");
                bidType = BidType.valueOf(bidTypeText);
                publicationBlockedBidTypes = td.publicationDeniedBidTypes.get(publicationId);
                if (publicationBlockedBidTypes == null) {
                    publicationBlockedBidTypes = new HashSet<BidType>();
                    td.publicationDeniedBidTypes.put(publicationId, publicationBlockedBidTypes);
                }
                publicationBlockedBidTypes.add(bidType);
                count++;
            }
            LOG.debug("Loaded " + count + " Denied Bid types from PUBLICATION_BLOCKED_BID_TYPE");
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Denied BidType");
        }
    }

    private void loadPublisherDeniedBidTypes(Connection conn, String commaSeparatedPublisherIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publisher Denied BidType");
        String sql = "SELECT PUBLISHER_ID, BID_TYPE FROM PUBLISHER_BLOCKED_BID_TYPE WHERE PUBLISHER_ID IN (" + commaSeparatedPublisherIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            String bidTypeText;
            long publisherId;
            BidType bidType;
            Set<BidType> publisherBlockedBidTypes;
            int count = 0;
            while (rs.next()) {
                publisherId = rs.getLong("PUBLISHER_ID");
                bidTypeText = rs.getString("BID_TYPE");
                bidType = BidType.valueOf(bidTypeText);
                publisherBlockedBidTypes = td.publisherDeniedBidTypes.get(publisherId);
                if (publisherBlockedBidTypes == null) {
                    publisherBlockedBidTypes = new HashSet<BidType>();
                    td.publisherDeniedBidTypes.put(publisherId, publisherBlockedBidTypes);
                }
                publisherBlockedBidTypes.add(bidType);
                count++;
            }
            LOG.debug("Loaded " + count + " Denied Bid types from PUBLISHER_BLOCKED_BID_TYPE");
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publisher Denied BidType");
        }
    }

    private void loadPublsherExcludedCategories(Connection conn, ShardMode shardMode, Set<Long> publisherIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publisher Excluded Categories");
        String commaSeparatedPublisherIds = StringUtils.join(publisherIds, ',');
        String sql = null;
        //Create query based on shard mode and pubisher ids provided
        if (shardMode.equals(ShardMode.exclude)) {
            if (StringUtils.isEmpty(commaSeparatedPublisherIds)) {
                sql = "SELECT PUBLISHER_ID, CATEGORY_ID FROM PUBLISHER_EXCLUDED_CATEGORY";
            } else {
                sql = "SELECT PUBLISHER_ID, CATEGORY_ID FROM PUBLISHER_EXCLUDED_CATEGORY WHERE PUBLISHER_ID NOT IN (" + commaSeparatedPublisherIds + ")";
            }

        } else {
            if (StringUtils.isEmpty(commaSeparatedPublisherIds)) {
                sql = "SELECT PUBLISHER_ID, CATEGORY_ID FROM PUBLISHER_EXCLUDED_CATEGORY";
            } else {
                sql = "SELECT PUBLISHER_ID, CATEGORY_ID FROM PUBLISHER_EXCLUDED_CATEGORY WHERE PUBLISHER_ID IN (" + commaSeparatedPublisherIds + ")";
            }

        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publisherId = rs.getLong("PUBLISHER_ID");
                long categoryId = rs.getLong("CATEGORY_ID");
                Set<Long> expanded = td.expandedPublisherExcludedCategoryIds.get(publisherId);
                if (expanded == null) {
                    expanded = new HashSet<Long>();
                    td.expandedPublisherExcludedCategoryIds.put(publisherId, expanded);
                }
                expanded.addAll(td.expandedCategoryIdsByCategoryId.get(categoryId));
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publisher Excluded Categories");
        }
    }

    private void loadPublicationStatedCategories(Connection conn, String commaSeparatedPublicationIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publication Stated Categories");
        String sql = "SELECT PUBLICATION_ID, CATEGORY_ID FROM PUBLICATION_STATED_CATEGORY WHERE PUBLICATION_ID IN (" + commaSeparatedPublicationIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publicationId = rs.getLong("PUBLICATION_ID");
                long categoryId = rs.getLong("CATEGORY_ID");
                Set<Long> statedCategoryIds = td.publicationStatedCategoryIds.get(publicationId);
                if (statedCategoryIds == null) {
                    statedCategoryIds = new HashSet<Long>();
                    td.publicationStatedCategoryIds.put(publicationId, statedCategoryIds);
                }
                statedCategoryIds.add(categoryId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publication Stated Categories");
        }
    }

    private void loadPublisherApprovedCreatives(Connection conn, String commaSeparatedPublisherIds, AdCacheBuildContext td) throws SQLException {
        td.startWatch("Loading Publisher Approved Creatives");
        String sql = "SELECT PUBLISHER_ID, CREATIVE_ID FROM PUBLISHER_APPROVED_CREATIVE WHERE PUBLISHER_ID IN (" + commaSeparatedPublisherIds + ")";
        if (LOG.isDebugEnabled()) {
            LOG.debug(sql);
        }
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        try {
            while (rs.next()) {
                long publisherId = rs.getLong("PUBLISHER_ID");
                long creativeId = rs.getLong("CREATIVE_ID");
                Set<Long> creativeIds = td.publisherApprovedCreativeIds.get(publisherId);
                if (creativeIds == null) {
                    creativeIds = new HashSet<Long>();
                    td.publisherApprovedCreativeIds.put(publisherId, creativeIds);
                }
                creativeIds.add(creativeId);
            }
        } finally {
            DbUtils.closeQuietly(null, pst, rs);
            td.stopWatch("Loading Publisher Approved Creatives");
        }

    }

    private void loadDormantAdSpaceExternalIds(AdSpaceService cache, AdCacheBuildContext td) throws java.sql.SQLException {
        td.startWatch("Loading Dormant AdSpace ExternalIds");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading DORMANT AdSpace externalIds");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        int totalLoaded = 0;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT EXTERNAL_ID FROM AD_SPACE WHERE STATUS='" + AdSpace.Status.DORMANT + "'";
            if (LOG.isDebugEnabled()) {
                LOG.debug(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cache.addDormantAdSpaceExternalId(rs.getString("EXTERNAL_ID"));
                totalLoaded++;
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
            td.stopWatch("Loading Dormant AdSpace ExternalIds");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Done Loading DORMANT AdSpace externalIds " + totalLoaded);
        }
    }

    private static final class PublicationNotEligibleException extends Exception {
        PublicationNotEligibleException(String msg) {
            super(msg);
        }
    }
}
