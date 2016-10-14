package com.adfonic.domainserializer.loader;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.domain.Audience;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignAudienceDto.AudienceType;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpAttributeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DmpSelectorDto;

public class CampaignAudienceLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String campaignAudienceSql = // 
    "select cau.ID as CAMPAIGN_AUDIENCE_ID, cmp.ID as CAMPAIGN_ID, cau.AUDIENCE_ID as AUDIENCE_ID, cau.INCLUDE as CAMPAIGN_INCLUDE, dmpa.DMP_VENDOR_ID, dmpa.USER_ENTERED_DMP_SELECTOR_EXTERNAL_ID, dmpv.DEFAULT_DATA_WHOLESALE" //
            + ", cau.NUM_DAYS_AGO_FROM, cau.NUM_DAYS_AGO_TO, cau.RECENCY_FROM, cau.RECENCY_TO" //
            + ", IF(dmpa.ID IS NOT NULL, 'DMP', fipa.TYPE) as AUDIENCE_TYPE" //
            + " from CAMPAIGN cmp" // 
            + " JOIN CAMPAIGN_AUDIENCE cau ON cmp.ID=cau.CAMPAIGN_ID" //
            + " JOIN AUDIENCE aud ON cau.AUDIENCE_ID=aud.ID" //
            + " LEFT JOIN FIRST_PARTY_AUDIENCE fipa ON aud.ID=fipa.AUDIENCE_ID" //
            + " LEFT JOIN DMP_AUDIENCE dmpa ON aud.ID=dmpa.AUDIENCE_ID" //
            + " LEFT JOIN DMP_VENDOR dmpv ON dmpa.DMP_VENDOR_ID=dmpv.ID" //
            + " WHERE cmp.status='ACTIVE' AND aud.status='ACTIVE' and cau.DELETED = 0";

    public void loadCampaignAudiences(Connection conn, Map<Long, CampaignDto> campaignsById) throws SQLException {

        if (logger.isDebugEnabled()) {
            logger.debug(campaignAudienceSql);
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(campaignAudienceSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                long campaignId = rs.getLong("CAMPAIGN_ID");
                CampaignDto campaign = campaignsById.get(campaignId);
                if (campaign != null) {
                    long caId = rs.getLong("CAMPAIGN_AUDIENCE_ID");
                    long audienceId = rs.getLong("AUDIENCE_ID");
                    boolean include = rs.getBoolean("CAMPAIGN_INCLUDE");
                    Integer numDaysAgoFrom = rs.getInt("NUM_DAYS_AGO_FROM");
                    if (rs.wasNull()) {
                        numDaysAgoFrom = null;
                    }
                    Integer numDaysAgoTo = rs.getInt("NUM_DAYS_AGO_TO");
                    if (rs.wasNull()) {
                        numDaysAgoTo = null;
                    }

                    Timestamp tsFrom = rs.getTimestamp("RECENCY_FROM");
                    Timestamp tsTo = rs.getTimestamp("RECENCY_TO");
                    Interval recencyInterval = recencyInterval(tsFrom, tsTo);

                    /**
                     * At the down of the time, only deviceid audiences existed
                     * Until MAD-2804 introduced new type of audience - location based audience
                     * Note that there is more first party audiences types in tools (UPLOAD,CLICK,...) 
                     * but all are considered deviceid based, only LOCATION is ...errr... location based 
                     * Apropos, third party audiences in DPM_AUDIENCE are so far allways deviceid based 
                     * 
                     * Anyway, we need to know on AdServer type of audience to make right checks against Bid derived audiences
                     */
                    AudienceType audienceType;
                    List<DmpAttributeDto> dmpAttributes;
                    String t2AudienceType = rs.getString("AUDIENCE_TYPE");
                    int dmpVendorId = rs.getInt("DMP_VENDOR_ID");
                    if (Audience.AudienceType.LOCATION.name().equals(t2AudienceType)) {
                        audienceType = AudienceType.LOCATION;
                        dmpAttributes = new ArrayList<DmpAttributeDto>();
                    } else if (dmpVendorId == AudienceType.ADSQUARE_DMP_VENDOR_ID) {
                        // having special audience type in tools would be nicer than rely on hardcoded vendor id... 
                        audienceType = AudienceType.ADSQUARE;
                        dmpAttributes = loadAttributesAndSelectors(conn, audienceId);
                    } else if (dmpVendorId == AudienceType.ADSQUARE_V2_DMP_VENDOR_ID) {
                        audienceType = AudienceType.ADSQUARE_V2;
                        dmpAttributes = loadAttributesAndSelectors(conn, audienceId);
                    } else if (dmpVendorId == AudienceType.FACTUAL_DMP_VENDOR_ID) {
                        audienceType = AudienceType.FACTUAL;
                        dmpAttributes = loadAttributesAndSelectors(conn, audienceId);
                    } else {
                        // Everything else is device audience
                        audienceType = AudienceType.DEVICE_ID;
                        dmpAttributes = new ArrayList<DmpAttributeDto>(); // Device ids are in MUID -> No attributes and selectors in DB
                    }

                    // Add artifitial DmpAttributeDto for selector that is user entered
                    String userExternalId = rs.getString("USER_ENTERED_DMP_SELECTOR_EXTERNAL_ID");
                    if (userExternalId != null) {
                        BigDecimal price = rs.getBigDecimal("DEFAULT_DATA_WHOLESALE");
                        if (price.intValue() == 0) {
                            logger.warn("DEFAULT_DATA_WHOLESALE == 0 for AUDIENCE_ID " + audienceId + " with USER_ENTERED_DMP_SELECTOR_EXTERNAL_ID " + userExternalId);
                        }
                        dmpAttributes.add(new DmpAttributeDto(-1l, Arrays.asList(new DmpSelectorDto(-1l, userExternalId, price))));
                    }

                    CampaignAudienceDto campaignAudience = new CampaignAudienceDto(caId, audienceId, audienceType, include, dmpAttributes, numDaysAgoFrom, numDaysAgoTo,
                            recencyInterval);
                    addAudience(campaignAudience, campaign);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static final String dmpSelectorsSql = //
    "select dsel.ID as SELECTOR_ID, dsel.EXTERNAL_ID as SELECTOR_EXTERNAL_ID, dsel.PUBLISHER_ID as SELECTOR_PUBLISHER_ID, dsel.DATA_WHOLESALE, dven.DEFAULT_DATA_WHOLESALE, datr.ID as ATTRIBUTE_ID" //
            + " from DMP_SELECTOR dsel" //
            + " join DMP_ATTRIBUTE datr ON dsel.DMP_ATTRIBUTE_ID = datr.ID" //
            + " join DMP_VENDOR dven ON datr.DMP_VENDOR_ID = dven.ID" //
            + " join DMP_AUDIENCE_DMP_SELECTOR dads ON dsel.ID = dads.DMP_SELECTOR_ID" //
            + " join DMP_AUDIENCE daud ON dads.DMP_AUDIENCE_ID = daud.ID" //
            + " where daud.AUDIENCE_ID = ?";

    private List<DmpAttributeDto> loadAttributesAndSelectors(Connection connection, long audienceId) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug(dmpSelectorsSql);
        }
        List<DmpAttributeDto> retval = new ArrayList<DmpAttributeDto>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(dmpSelectorsSql);
            ps.setLong(1, audienceId);
            rs = ps.executeQuery();
            DmpAttributeDto attribute = null;
            while (rs.next()) {
                long attributeId = rs.getLong("ATTRIBUTE_ID");

                if (attribute == null || attributeId != attribute.getId()) {
                    attribute = new DmpAttributeDto(attributeId);
                    retval.add(attribute);
                }

                long selectorId = rs.getLong("SELECTOR_ID");
                String externalId = rs.getString("SELECTOR_EXTERNAL_ID");
                Long publisherId = rs.getLong("SELECTOR_PUBLISHER_ID");
                if (rs.wasNull()) {
                    publisherId = null;
                }

                BigDecimal price = rs.getBigDecimal("DATA_WHOLESALE");
                if (price.intValue() == 0) {
                    price = rs.getBigDecimal("DEFAULT_DATA_WHOLESALE");
                    if (price.intValue() == 0) {
                        logger.warn("Both DATA_WHOLESALE and DEFAULT_DATA_WHOLESALE == 0 for SELECTOR_ID " + selectorId);
                    }
                }

                attribute.addSelector(new DmpSelectorDto(selectorId, externalId, price, publisherId));
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded for: " + audienceId + ", attributes: " + retval);
        }
        return retval;
    }

    Interval recencyInterval(Timestamp tsFrom, Timestamp tsTo) {
        if (tsFrom == null) {
            return null;
        }
        if (tsTo == null) {
            return null;
        }
        DateTime from = new DateTime(tsFrom, DateTimeZone.UTC);
        DateTime to = new DateTime(tsTo, DateTimeZone.UTC);
        return new Interval(from, to);
    }

    /**
     * Split audiences into separate sets to make AdServer's logic easier
     */
    public void addAudience(CampaignAudienceDto audience, CampaignDto campaign) {
        switch (audience.getType()) {
        case DEVICE_ID:
            campaign.addDeviceIdAudience(audience);
            break;
        case LOCATION:
            campaign.addLocationAudience(audience);
            break;
        case ADSQUARE:
        case ADSQUARE_V2:
            campaign.addAdsquareAudience(audience);
            break;
        case FACTUAL:
            // Inside single Factual audience, only one type (Proximity or Audience) can be used. They can't be mixed.
            // Factual UI allows to add Proximity subset into Audience so it makes no sense for user to create and use them separately in Tools UI on our side.
            List<DmpAttributeDto> attributes = audience.getDmpAttributes();
            Boolean isProximity = null;
            for (DmpAttributeDto attribute : attributes) {
                List<DmpSelectorDto> selectors = attribute.getSelectors();
                for (DmpSelectorDto selector : selectors) {
                    if (selector.getPublisherId() != null) {
                        if (isProximity != null && isProximity != Boolean.TRUE) {
                            logger.error("Factual Proximity selector vs Audience audience mixup. Selector: " + selector + ", Audience: " + audience);
                        } else {
                            isProximity = Boolean.TRUE;
                            campaign.addFactualAudienceAudience(audience);
                        }
                    } else {
                        if (isProximity != null && isProximity != Boolean.FALSE) {
                            logger.error("Factual Audience selector vs Proximity audience mixup. Selector: " + selector + ", Audience: " + audience);
                        } else {
                            isProximity = Boolean.FALSE;
                            campaign.addFactualProximityAudience(audience);
                        }
                    }
                }
            }
            break;
        default:
            logger.warn("Unsupported audience type: " + audience.getType() + ", audience id: " + audience.getAudienceId());
        }
    }

}
