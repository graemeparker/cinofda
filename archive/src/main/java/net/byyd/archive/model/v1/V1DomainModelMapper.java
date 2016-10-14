package net.byyd.archive.model.v1;

import java.lang.reflect.InvocationTargetException;

import net.byyd.archive.mapping.DomainModelMapper;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.adserver.Impression;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;

public class V1DomainModelMapper implements DomainModelMapper<AdEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(V1DomainModelMapper.class);

    @Override
    public AdEvent map(com.adfonic.adserver.AdEvent event) {
        AdEvent ret = new AdEvent();

        ret.setHost(event.getHost());
        ret.setEventTime(event.getEventTime());
        ret.setCreativeId(event.getCreativeId());
        ret.setCampaignId(event.getCampaignId());
        ret.setAdSpaceId(event.getAdSpaceId());
        ret.setPublicationId(event.getPublicationId());
        ret.setModelId(event.getModelId());
        ret.setCountryId(event.getCountryId());
        ret.setOperatorId(event.getOperatorId());
        ret.setGeotargetId(event.getGeotargetId());
        ret.setIntegrationTypeId(event.getIntegrationTypeId());
        ret.setTestMode(event.isTestMode());
        ret.setIpAddress(event.getIpAddress());
        ret.setUserAgentHeader(event.getUserAgentHeader());
        ret.setTrackingIdentifier(event.getTrackingIdentifier());
        ret.setRtbSettlementPrice(event.getRtbSettlementPrice());
        ret.setRtbBidPrice(event.getRtbBidPrice());
        ret.setPostalCodeId(event.getPostalCodeId());
        ret.setActionValue(event.getActionValue());
        ret.setDateOfBirth(event.getDateOfBirth());
        ret.setRtb(event.isRtb() ? 1L : 0L);
        ret.setUserTimeId(event.getUserTimeId());

        ret.setLatitude(event.getLatitude());
        ret.setLongitude(event.getLongitude());
        ret.setLocationSource(event.getLocationSource());

        if (event.getDeviceIdentifiers() != null && !event.getDeviceIdentifiers().isEmpty()) {
            ret.setDeviceIdentifiers(event.getDeviceIdentifiers());
        }

        // translate deviations and enums
        translateEnum(ret, event, "adAction", AdAction.class);
        translateEnum(ret, event, "gender", Gender.class);
        translateEnum(ret, event, "unfilledReason", UnfilledReason.class);
        if (event.getAgeRange() != null) {
            ret.setAgeFrom(event.getAgeRange().getStart());
            ret.setAgeTo(event.getAgeRange().getEnd());
            ret.setAgeIntegral(event.getAgeRange().isIntegral());
        }

        return ret;
    }

    @Override
    public AdEvent map(com.adfonic.adserver.Impression event) {
        AdEvent ret = new AdEvent();

        ret.setAdAction(AdAction.IMPRESSION);
        ret.setHost(event.getHost());
        ret.setEventTime(event.getCreationTime());
        ret.setAdSpaceId(event.getAdSpaceId());
        ret.setModelId(event.getModelId());
        ret.setCountryId(event.getCountryId());
        ret.setOperatorId(event.getOperatorId());
        ret.setGeotargetId(event.getGeotargetId());
        ret.setIntegrationTypeId(event.getIntegrationTypeId());
        ret.setTestMode(event.isTestMode());
        ret.setTrackingIdentifier(event.getTrackingIdentifier());
        ret.setRtbSettlementPrice(event.getRtbSettlementPrice());
        ret.setRtbBidPrice(event.getRtbBidPrice());
        ret.setPostalCodeId(event.getPostalCodeId());
        ret.setDateOfBirth(event.getDateOfBirth());
        ret.setImpressionExternalID(event.getExternalID());
        
        if (event.getUserTimeZone() != null) {
            ret.setUserTimeId(DateUtils.getTimeID(event.getCreationTime(), event.getUserTimeZone()));
        }

        ret.setLatitude(event.getLatitude());
        ret.setLongitude(event.getLongitude());
        ret.setLocationSource(event.getLocationSource());

        if (event.getDeviceIdentifiers() != null && !event.getDeviceIdentifiers().isEmpty()) {
            ret.setDeviceIdentifiers(event.getDeviceIdentifiers());
        }
        // translate deviations and enums
        translateEnum(ret, event, "gender", Gender.class);
        if (event.getAgeRange() != null) {
            ret.setAgeFrom(event.getAgeRange().getStart());
            ret.setAgeTo(event.getAgeRange().getEnd());
            ret.setAgeIntegral(event.getAgeRange().isIntegral());
        }

        ret.setSslRequired(event.getSslRequired());
        ret.setpDestinationurl(event.getPdDestinationUrl());

        return ret;
    }

    public Impression getImpressionFromAdEvent(AdEvent ae) {
        Impression impression = new Impression();
        impression.setCreationTime(ae.getEventTime());
        impression.setDeviceIdentifiers(ae.getDeviceIdentifiers());
        impression.setExternalID(ae.getImpressionExternalID());
        impression.setTestMode(ae.isTestMode());
        impression.setTrackingIdentifier(ae.getTrackingIdentifier());
        impression.setAdSpaceId(ae.getAdSpaceId());
        impression.setCreativeId(ae.getCreativeId());
        impression.setModelId(ae.getModelId());
        impression.setCountryId(ae.getCountryId());
        impression.setOperatorId(ae.getOperatorId());
        if (ae.getAgeFrom() != null && ae.getAgeTo() != null) {
            impression.setAgeRange(new Range<Integer>(ae.getAgeFrom(), ae.getAgeTo()));
        }
        if (ae.getGender() != null) {
            if (ae.getGender().equals(Gender.FEMALE)) {
                impression.setGender(com.adfonic.domain.Gender.FEMALE);
            } else if (ae.getGender() != null && ae.getGender().equals(Gender.MALE)) {
                impression.setGender(com.adfonic.domain.Gender.FEMALE);
            }
        }
        impression.setGeotargetId(ae.getGeotargetId());
        impression.setIntegrationTypeId(ae.getIntegrationTypeId());
        impression.setRtbSettlementPrice(ae.getRtbSettlementPrice());
        impression.setPdDestinationUrl(ae.getpDestinationurl());
        impression.setPostalCodeId(ae.getPostalCodeId());
        impression.setHost(ae.getHost());
//        if (ae.getUserTimeId() != null) {
//            impression.setUserTimeZoneId(Integer.toString(ae.getUserTimeId()()));
//        }
        impression.setRtbBidPrice(ae.getRtbBidPrice());
        impression.setStrategy(ae.getStrategy());
        impression.setDateOfBirth(ae.getDateOfBirth());
        impression.setLatitude(ae.getLatitude());
        impression.setLongitude(ae.getLongitude());
        impression.setLocationSource(ae.getLocationSource());
        impression.setCampaignDataFeeId(ae.getCampaignHistoryDataFeeId());
        impression.setSslRequired(ae.getSslRequired());

        return impression;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void translateEnum(AdEvent ret, Object event, String property, Class<? extends Enum> clazz) {
        try {
            Object orig = PropertyUtils.getProperty(event, property);
            if (orig != null) {
                PropertyUtils.setProperty(ret, property, Enum.valueOf(clazz, orig.toString()));
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException ex) {
            LOG.warn("Unable to write enum: " + property + " " + ex.getMessage());
        }
    }
}
